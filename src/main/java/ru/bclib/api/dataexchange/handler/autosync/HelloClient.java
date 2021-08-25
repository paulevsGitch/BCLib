package ru.bclib.api.dataexchange.handler.autosync;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.dataexchange.handler.autosync.AutoSyncID.WithContentOverride;
import ru.bclib.api.dataexchange.handler.autosync.SyncFolderDescriptor.SubFile;
import ru.bclib.config.Configs;
import ru.bclib.gui.screens.ModListScreen;
import ru.bclib.gui.screens.ProgressScreen;
import ru.bclib.gui.screens.SyncFilesScreen;
import ru.bclib.gui.screens.WarnBCLibVersionMismatch;
import ru.bclib.util.ModUtil;
import ru.bclib.util.ModUtil.ModInfo;
import ru.bclib.util.Pair;
import ru.bclib.util.PathUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sent from the Server to the Client.
 * <p>
 * For Details refer to {@link HelloServer}
 */
public class HelloClient extends DataHandler.FromServer {
	public record OfferedModInfo(String version, int size, boolean canDownload) {
	}
	public interface IServerModMap extends Map<String, OfferedModInfo> {}
	public static class ServerModMap extends HashMap<String, OfferedModInfo> implements IServerModMap {}

	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "hello_client"), HelloClient::new, false, false);
	
	public HelloClient() {
		super(DESCRIPTOR.IDENTIFIER);
	}
	
	static String getBCLibVersion() {
		return ModUtil.getModVersion(BCLib.MOD_ID);
	}
	
	@Override
	protected boolean prepareDataOnServer() {
		if (!Configs.SERVER_CONFIG.isAllowingAutoSync()) {
			BCLib.LOGGER.info("Auto-Sync was disabled on the server.");
			return false;
		}
		
		AutoSync.loadSyncFolder();
		return true;
	}
	
	@Override
	protected void serializeDataOnServer(FriendlyByteBuf buf) {
		final String vbclib = getBCLibVersion();
		BCLib.LOGGER.info("Sending Hello to Client. (server=" + vbclib + ")");

		//write BCLibVersion (=protocol version)
		buf.writeInt(ModUtil.convertModVersion(vbclib));
		
		if (Configs.SERVER_CONFIG.isOfferingMods() || Configs.SERVER_CONFIG.isOfferingInfosForMods()) {
			List<String> mods = DataExchangeAPI.registeredMods();
			final List<String> inmods = mods;
			if (Configs.SERVER_CONFIG.isOfferingAllMods() || Configs.SERVER_CONFIG.isOfferingInfosForMods()){
				mods = new ArrayList<>(inmods.size());
				mods.addAll(inmods);
				mods.addAll(ModUtil
						.getMods()
						.entrySet()
						.stream()
						.filter(entry -> entry.getValue().metadata.getEnvironment()!= ModEnvironment.SERVER && !inmods.contains(entry.getKey()))
						.map(entry -> entry.getKey())
						.collect(Collectors.toList())
				);
			}

			//write Plugin Versions
			buf.writeInt(mods.size());
			for (String modID : mods) {
				final String ver = ModUtil.getModVersion(modID);
				int size = 0;

				final ModInfo mi = ModUtil.getModInfo(modID);
				if (mi != null) {
					try {
						size = (int) Files.size(mi.jarPath);
					} catch (IOException e) {
						BCLib.LOGGER.error("Unable to get File Size: " + e.getMessage());
					}
				}

				
				writeString(buf, modID);
				buf.writeInt(ModUtil.convertModVersion(ver));
				buf.writeInt(size);
				final boolean canDownload = size>0 && Configs.SERVER_CONFIG.isOfferingMods() && (Configs.SERVER_CONFIG.isOfferingAllMods() || inmods.contains(modID));
				buf.writeBoolean(canDownload);
				
				BCLib.LOGGER.info("    - Listing Mod " + modID + " v" + ver + " (size: " + PathUtil.humanReadableFileSize(size) + ", download="+canDownload+")");
			}
		}
		else {
			BCLib.LOGGER.info("Server will not list Mods.");
			buf.writeInt(0);
		}
		
		if (Configs.SERVER_CONFIG.isOfferingFiles() || Configs.SERVER_CONFIG.isOfferingConfigs()) {
			//do only include files that exist on the server
			final List<AutoFileSyncEntry> existingAutoSyncFiles = AutoSync.getAutoSyncFiles()
																		  .stream()
																		  .filter(e -> e.fileName.exists())
																		  .filter(e -> (e.isConfigFile() && Configs.SERVER_CONFIG.isOfferingConfigs()) || (e instanceof AutoFileSyncEntry.ForDirectFileRequest && Configs.SERVER_CONFIG.isOfferingFiles()))
																		  .collect(Collectors.toList());
			
			//send config Data
			buf.writeInt(existingAutoSyncFiles.size());
			for (AutoFileSyncEntry entry : existingAutoSyncFiles) {
				entry.serialize(buf);
				BCLib.LOGGER.info("    - Offering " + (entry.isConfigFile() ? "Config " : "File ") + entry);
			}
		}
		else {
			BCLib.LOGGER.info("Server will neither offer Files nor Configs.");
			buf.writeInt(0);
		}
		
		if (Configs.SERVER_CONFIG.isOfferingFiles()) {
			buf.writeInt(AutoSync.syncFolderDescriptions.size());
			AutoSync.syncFolderDescriptions.forEach(desc -> {
				BCLib.LOGGER.info("    - Offering Folder " + desc.localFolder + " (allowDelete=" + desc.removeAdditionalFiles + ")");
				desc.serialize(buf);
			});
		}
		else {
			BCLib.LOGGER.info("Server will not offer Sync Folders.");
			buf.writeInt(0);
		}

		buf.writeBoolean(Configs.SERVER_CONFIG.isOfferingInfosForMods());
	}
	
	String bclibVersion = "0.0.0";



	IServerModMap modVersion = new ServerModMap();
	List<AutoSync.AutoSyncTriple> autoSyncedFiles = null;
	List<SyncFolderDescriptor> autoSynFolders = null;
	boolean serverPublishedModInfo = false;
	
	@Environment(EnvType.CLIENT)
	@Override
	protected void deserializeIncomingDataOnClient(FriendlyByteBuf buf, PacketSender responseSender) {
		//read BCLibVersion (=protocol version)
		bclibVersion = ModUtil.convertModVersion(buf.readInt());
		final boolean protocolVersion_0_4_1 = ModUtil.isLargerOrEqualVersion(bclibVersion, "0.4.1");
		
		
		//read Plugin Versions
		modVersion = new ServerModMap();
		int count = buf.readInt();
		for (int i = 0; i < count; i++) {
			final String id = readString(buf);
			final String version = ModUtil.convertModVersion(buf.readInt());
			final int size;
			final boolean canDownload;
			//since v0.4.1 we also send the size of the mod-File
			if (protocolVersion_0_4_1) {
				size = buf.readInt();
				canDownload = buf.readBoolean();
			}
			else {
				size = 0;
				canDownload = true;
			}
			modVersion.put(id, new OfferedModInfo(version, size, canDownload));
		}
		
		//read config Data
		count = buf.readInt();
		autoSyncedFiles = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			//System.out.println("Deserializing ");
			AutoSync.AutoSyncTriple t = AutoFileSyncEntry.deserializeAndMatch(buf);
			autoSyncedFiles.add(t);
			//System.out.println(t.first);
		}
		
		
		autoSynFolders = new ArrayList<>(1);
		//since v0.4.1 we also send the sync folders
		if (protocolVersion_0_4_1) {
			final int folderCount = buf.readInt();
			for (int i = 0; i < folderCount; i++) {
				SyncFolderDescriptor desc = SyncFolderDescriptor.deserialize(buf);
				autoSynFolders.add(desc);
			}

			serverPublishedModInfo = buf.readBoolean();
		}
	}
	
	@Environment(EnvType.CLIENT)
	private void processAutoSyncFolder(final List<AutoSyncID> filesToRequest, final List<AutoSyncID.ForDirectFileRequest> filesToRemove) {
		if (!Configs.CLIENT_CONFIG.isAcceptingFiles()) {
			return;
		}
		
		if (autoSynFolders.size() > 0) {
			BCLib.LOGGER.info("Folders offered by Server:");
		}
		
		autoSynFolders.forEach(desc -> {
			//desc contains the fileCache sent from the server, load the local version to get hold of the actual file cache on the client
			SyncFolderDescriptor localDescriptor = AutoSync.getSyncFolderDescriptor(desc.folderID);
			if (localDescriptor != null) {
				BCLib.LOGGER.info("    - " + desc.folderID + " (" + desc.localFolder + ", allowRemove=" + desc.removeAdditionalFiles + ")");
				localDescriptor.invalidateCache();
				
				desc.relativeFilesStream()
					.filter(desc::discardChildElements)
					.forEach(subFile -> {
						BCLib.LOGGER.warning("       * " + subFile.relPath + " (REJECTED)");
					});
				
				
				if (desc.removeAdditionalFiles) {
					List<AutoSyncID.ForDirectFileRequest> additionalFiles = localDescriptor.relativeFilesStream()
																						   .filter(subFile -> !desc.hasRelativeFile(subFile))
																						   .map(desc::mapAbsolute)
																						   .filter(desc::acceptChildElements)
																						   .map(absPath -> new AutoSyncID.ForDirectFileRequest(desc.folderID, absPath.toFile()))
																						   .collect(Collectors.toList());
					
					additionalFiles.forEach(aid -> BCLib.LOGGER.info("       * " + desc.localFolder.relativize(aid.relFile.toPath()) + " (missing on server)"));
					filesToRemove.addAll(additionalFiles);
				}
				
				desc.relativeFilesStream()
					.filter(desc::acceptChildElements)
					.forEach(subFile -> {
						SubFile localSubFile = localDescriptor.getLocalSubFile(subFile.relPath);
						if (localSubFile != null) {
							//the file exists locally, check if the hashes match
							if (!localSubFile.hash.equals(subFile.hash)) {
								BCLib.LOGGER.info("       * " + subFile.relPath + " (changed)");
								filesToRequest.add(new AutoSyncID.ForDirectFileRequest(desc.folderID, new File(subFile.relPath)));
							}
							else {
								BCLib.LOGGER.info("       * " + subFile.relPath);
							}
						}
						else {
							//the file is missing locally
							BCLib.LOGGER.info("       * " + subFile.relPath + " (missing on client)");
							filesToRequest.add(new AutoSyncID.ForDirectFileRequest(desc.folderID, new File(subFile.relPath)));
						}
					});
				
				//free some memory
				localDescriptor.invalidateCache();
			}
			else {
				BCLib.LOGGER.info("    - " + desc.folderID + " (Failed to find)");
			}
		});
	}
	
	@Environment(EnvType.CLIENT)
	private void processSingleFileSync(final List<AutoSyncID> filesToRequest) {
		final boolean debugHashes = Configs.CLIENT_CONFIG.shouldPrintDebugHashes();
		
		if (autoSyncedFiles.size() > 0) {
			BCLib.LOGGER.info("Files offered by Server:");
		}
		
		//Handle single sync files
		//Single files need to be registered for sync on both client and server
		//There are no restrictions to the target folder, but the client decides the final
		//location.
		for (AutoSync.AutoSyncTriple e : autoSyncedFiles) {
			String actionString = "";
			FileContentWrapper contentWrapper = new FileContentWrapper(e.serverContent);
			if (e.localMatch == null) {
				actionString = "(new, prepare update)";
				filesToRequest.add(new AutoSyncID(e.serverHash.modID, e.serverHash.uniqueID));
			}
			else if (e.localMatch.needTransfer.test(e.localMatch.getFileHash(), e.serverHash, contentWrapper)) {
				actionString = "(prepare update)";
				//we did not yet receive the new content
				if (contentWrapper.getRawContent() == null) {
					filesToRequest.add(new AutoSyncID(e.serverHash.modID, e.serverHash.uniqueID));
				}
				else {
					filesToRequest.add(new AutoSyncID.WithContentOverride(e.serverHash.modID, e.serverHash.uniqueID, contentWrapper, e.localMatch.fileName));
				}
			}
			
			BCLib.LOGGER.info("    - " + e + ": " + actionString);
			if (debugHashes) {
				BCLib.LOGGER.info("      * " + e.serverHash + " (Server)");
				BCLib.LOGGER.info("      * " + e.localMatch.getFileHash() + " (Client)");
				BCLib.LOGGER.info("      * local Content " + (contentWrapper.getRawContent() == null));
			}
		}
	}

	
	@Environment(EnvType.CLIENT)
	private void processModFileSync(final List<AutoSyncID> filesToRequest, final Set<String> mismatchingMods) {
		for (Entry<String, OfferedModInfo> e : modVersion.entrySet()) {
			final String localVersion = ModUtil.getModVersion(e.getKey());
			final OfferedModInfo serverInfo = e.getValue();
			final boolean requestMod = !serverInfo.version.equals(localVersion) && serverInfo.size > 0 && serverInfo.canDownload;
			
			BCLib.LOGGER.info("    - " + e.getKey() + " (client=" + localVersion + ", server=" + serverInfo.version + ", size=" + PathUtil.humanReadableFileSize(serverInfo.size) + (requestMod ? ", requesting" : "") + (serverInfo.canDownload ? "" :", not offered")+ ")");
			if (requestMod) {
				filesToRequest.add(new AutoSyncID.ForModFileRequest(e.getKey(), serverInfo.version));
			}
			if (!serverInfo.version.equals(localVersion)) {
				mismatchingMods.add(e.getKey());
			}
		}

		mismatchingMods.addAll(ModListScreen.localMissing(modVersion));
		mismatchingMods.addAll(ModListScreen.serverMissing(modVersion));
	}
	
	@Override
	protected boolean isBlocking() {
		return true;
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	protected void runOnClientGameThread(Minecraft client) {
		if (!Configs.CLIENT_CONFIG.isAllowingAutoSync()) {
			BCLib.LOGGER.info("Auto-Sync was disabled on the client.");
			return;
		}
		final String localBclibVersion = getBCLibVersion();
		BCLib.LOGGER.info("Received Hello from Server. (client=" + localBclibVersion + ", server=" + bclibVersion + ")");
		
		if (ModUtil.convertModVersion(localBclibVersion) != ModUtil.convertModVersion(bclibVersion)){
		 	showBCLibError(client);
		 	return;
		 }
		
		final List<AutoSyncID> filesToRequest = new ArrayList<>(2);
		final List<AutoSyncID.ForDirectFileRequest> filesToRemove = new ArrayList<>(2);
		final Set<String> mismatchingMods = new HashSet<>(2);
		
		
		processModFileSync(filesToRequest, mismatchingMods);
		processSingleFileSync(filesToRequest);
		processAutoSyncFolder(filesToRequest, filesToRemove);
		
		//Handle folder sync
		//Both client and server need to know about the folder you want to sync
		//Files can only get placed within that folder
		
		if ((filesToRequest.size() > 0 || filesToRemove.size() > 0) && ( Configs.CLIENT_CONFIG.isAcceptingMods() ||  Configs.CLIENT_CONFIG.isAcceptingConfigs() ||  Configs.CLIENT_CONFIG.isAcceptingFiles())) {
			showSyncFilesScreen(client, filesToRequest, filesToRemove);
			return;
		} else if (serverPublishedModInfo && mismatchingMods.size()>0) {
			client.setScreen(new ModListScreen(client.screen, new TranslatableComponent("title.bclib.modmissmatch"), new TranslatableComponent("message.bclib.modmissmatch"), ModUtil.getMods(), modVersion));
			return;
		}
	}
	
	@Environment(EnvType.CLIENT)
	protected void showBCLibError(Minecraft client) {
		BCLib.LOGGER.error("BCLib differs on client and server.");
		client.setScreen(new WarnBCLibVersionMismatch((download) -> {
			if (download) {
				requestBCLibDownload();

				this.onCloseSyncFilesScreen();
			}
		}));
	}
	
	@Environment(EnvType.CLIENT)
	protected void showSyncFilesScreen(Minecraft client, List<AutoSyncID> files, final List<AutoSyncID.ForDirectFileRequest> filesToRemove) {
		int configFiles = 0;
		int singleFiles = 0;
		int folderFiles = 0;
		int modFiles = 0;
		
		for (AutoSyncID aid : files) {
			if (aid.isConfigFile()) {
				configFiles++;
			}
			else if (aid instanceof AutoSyncID.ForModFileRequest) {
				modFiles++;
			}
			else if (aid instanceof AutoSyncID.ForDirectFileRequest) {
				folderFiles++;
			}
			else {
				singleFiles++;
			}
		}
		
		client.setScreen(new SyncFilesScreen(modFiles, configFiles, singleFiles, folderFiles, filesToRemove.size(), modVersion, (downloadMods, downloadConfigs, downloadFiles, removeFiles) -> {
			if (downloadMods || downloadConfigs || downloadFiles) {
				BCLib.LOGGER.info("Updating local Files:");
				List<AutoSyncID.WithContentOverride> localChanges = new ArrayList<>(files.toArray().length);
				List<AutoSyncID> requestFiles = new ArrayList<>(files.toArray().length);
				
				files.forEach(aid -> {
					if (aid.isConfigFile() && downloadConfigs) {
						processOfferedFile(requestFiles, aid);
					}
					else if (aid instanceof AutoSyncID.ForModFileRequest && downloadMods) {
						processOfferedFile(requestFiles, aid);
					}
					else if (downloadFiles) {
						processOfferedFile(requestFiles, aid);
					}
				});
				
				requestFileDownloads(requestFiles);
			}
			if (removeFiles) {
				filesToRemove.forEach(aid -> {
					BCLib.LOGGER.info("    - " + aid.relFile + " (removing)");
					aid.relFile.delete();
				});
			}
			
			this.onCloseSyncFilesScreen();
		}));
	}
	
	@Environment(EnvType.CLIENT)
	private void onCloseSyncFilesScreen(){
		Minecraft.getInstance()
				 .setScreen(ChunkerProgress.getProgressScreen());
	}
	
	private void processOfferedFile(List<AutoSyncID> requestFiles, AutoSyncID aid) {
		if (aid instanceof WithContentOverride) {
			final WithContentOverride aidc = (WithContentOverride) aid;
			BCLib.LOGGER.info("    - " + aid + " (updating Content)");
			
			SendFiles.writeSyncedFile(aid, aidc.contentWrapper.getRawContent(), aidc.localFile);
		}
		else {
			requestFiles.add(aid);
			BCLib.LOGGER.info("    - " + aid + " (requesting)");
		}
	}
	
	private void requestBCLibDownload() {
		BCLib.LOGGER.warning("Starting download of BCLib");
		requestFileDownloads(List.of(new AutoSyncID.ForModFileRequest(BCLib.MOD_ID, bclibVersion)));
	}
	
	@Environment(EnvType.CLIENT)
	private void requestFileDownloads(List<AutoSyncID> files) {
		BCLib.LOGGER.info("Starting download of Files:" + files.size());

		final ProgressScreen progress = new ProgressScreen(null, new TranslatableComponent("title.bclib.filesync.progress"), new TranslatableComponent("message.bclib.filesync.progress"));
		progress.progressStart(new TranslatableComponent("message.bclib.filesync.progress.stage.empty"));
		ChunkerProgress.setProgressScreen(progress);

		DataExchangeAPI.send(new RequestFiles(files));
	}
}
