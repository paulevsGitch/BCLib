package ru.bclib.api.dataexchange.handler.autosync;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.dataexchange.handler.autosync.AutoSync.ClientConfig;
import ru.bclib.api.dataexchange.handler.autosync.AutoSync.Config;
import ru.bclib.api.dataexchange.handler.autosync.AutoSyncID.WithContentOverride;
import ru.bclib.api.dataexchange.handler.autosync.SyncFolderDescriptor.SubFile;
import ru.bclib.gui.screens.SyncFilesScreen;
import ru.bclib.gui.screens.WarnBCLibVersionMismatch;
import ru.bclib.util.ModUtil;
import ru.bclib.util.Pair;
import ru.bclib.util.PathUtil;
import ru.bclib.util.ModUtil.ModInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Sent from the Server to the Client.
 * <p>
 * For Details refer to {@link HelloServer}
 */
public class HelloClient extends DataHandler.FromServer {
	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "hello_client"), HelloClient::new, false, false);
	
	public HelloClient() {
		super(DESCRIPTOR.IDENTIFIER);
	}
	
	static String getBCLibVersion() {
		return ModUtil.getModVersion(BCLib.MOD_ID);
	}
	
	@Override
	protected boolean prepareDataOnServer() {
		if (!Config.isAllowingAutoSync()) {
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
		final List<String> mods = DataExchangeAPI.registeredMods();
		
		//write BCLibVersion (=protocol version)
		buf.writeInt(ModUtil.convertModVersion(vbclib));
		
		if (Config.isOfferingMods()) {
			//write Plugin Versions
			buf.writeInt(mods.size());
			for (String modID : mods) {
				final String ver = ModUtil.getModVersion(modID);
				final ModInfo mi = ModUtil.getModInfo(modID);
				int size = 0;
				if (mi!=null) {
					try {
						size = (int)Files.size(mi.jarPath);
					}
					catch (IOException e) {
						BCLib.LOGGER.error("Unable to get File Size: " + e.getMessage());
					}
				}
				
				writeString(buf, modID);
				buf.writeInt(ModUtil.convertModVersion(ver));
				buf.writeInt(size);
				
				BCLib.LOGGER.info("    - Listing Mod " + modID + " v" + ver + " ("+PathUtil.humanReadableFileSize(size)+")");
			}
		}
		else {
			BCLib.LOGGER.info("Server will not list Mods.");
			buf.writeInt(0);
		}
		
		if (Config.isOfferingFiles()) {
			//do only include files that exist on the server
			final List<AutoFileSyncEntry> existingAutoSyncFiles = AutoSync
																			  .getAutoSyncFiles()
																			  .stream()
																			  .filter(e -> e.fileName.exists())
																			  .collect(Collectors.toList());
			
			//send config Data
			buf.writeInt(existingAutoSyncFiles.size());
			for (AutoFileSyncEntry entry : existingAutoSyncFiles) {
				entry.serialize(buf);
				BCLib.LOGGER.info("    - Offering File " + entry);
			}
		}
		else {
			BCLib.LOGGER.info("Server will not offer Files.");
			buf.writeInt(0);
		}
		
		if (Config.isOfferingFolders()) {
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
	}
	
	String bclibVersion = "0.0.0";
	Map<String, Pair<String, Integer>> modVersion = new HashMap<>();
	List<AutoSync.AutoSyncTriple> autoSyncedFiles = null;
	List<SyncFolderDescriptor> autoSynFolders = null;
	
	@Environment(EnvType.CLIENT)
	@Override
	protected void deserializeIncomingDataOnClient(FriendlyByteBuf buf, PacketSender responseSender) {
		//read BCLibVersion (=protocol version)
		bclibVersion = ModUtil.convertModVersion(buf.readInt());
		final boolean protocolVersion_0_4_1 = ModUtil.isLargerOrEqualVersion(bclibVersion, "0.4.1");
		
		
		//read Plugin Versions
		modVersion = new HashMap<>();
		int count = buf.readInt();
		for (int i = 0; i < count; i++) {
			final String id = readString(buf);
			final String version = ModUtil.convertModVersion(buf.readInt());
			final int size;
			//since v0.4.1 we also send the size of the mod-File
			if (protocolVersion_0_4_1) {
				size = buf.readInt();
			} else {
				size = 0;
			}
			modVersion.put(id, new Pair<>(version, size));
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
		}
	}
	
	@Environment(EnvType.CLIENT)
	private void processAutoSyncFolder(final List<AutoSyncID> filesToRequest, final List<AutoSyncID.ForDirectFileRequest> filesToRemove) {
		if (!ClientConfig.isAcceptingFolders()) {
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
		final boolean debugHashes = ClientConfig.shouldPrintDebugHashes();
		
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
	private void processModFileSync(final List<AutoSyncID> filesToRequest) {
		for (Entry<String, Pair<String, Integer>> e : modVersion.entrySet()) {
			final String localVersion = ModUtil.getModVersion(e.getKey());
			final Pair<String, Integer> serverInfo = e.getValue();
			final boolean requestMod = !serverInfo.first.equals(localVersion) && serverInfo.second>0;
			
			BCLib.LOGGER.info("    - " + e.getKey() + " (client=" + localVersion + ", server=" + serverInfo.first  + ", size=" + PathUtil.humanReadableFileSize(serverInfo.second) + (requestMod?", requesting":"") +")");
			if (requestMod){
				filesToRequest.add(new AutoSyncID.ForModFileRequest(e.getKey(), serverInfo.first));
			}
		}
	}
	
	
	@Environment(EnvType.CLIENT)
	@Override
	protected void runOnClientGameThread(Minecraft client) {
		if (!ClientConfig.isAllowingAutoSync()) {
			BCLib.LOGGER.info("Auto-Sync was disabled on the client.");
			return;
		}
		final String localBclibVersion = getBCLibVersion();
		BCLib.LOGGER.info("Received Hello from Server. (client=" + localBclibVersion + ", server=" + bclibVersion + ")");
		
		// if (DataFixerAPI.getModVersion(localBclibVersion) != DataFixerAPI.getModVersion(bclibVersion)){
		// 	showBCLibError(client);
		// 	return;
		// }
		
		final List<AutoSyncID> filesToRequest = new ArrayList<>(2);
		final List<AutoSyncID.ForDirectFileRequest> filesToRemove = new ArrayList<>(2);
		
		
		
		processModFileSync(filesToRequest);
		processSingleFileSync(filesToRequest);
		processAutoSyncFolder(filesToRequest, filesToRemove);
		
		//Handle folder sync
		//Both client and server need to know about the folder you want to sync
		//Files can only get placed within that folder
		
		if ((filesToRequest.size() > 0 || filesToRemove.size() > 0) && ClientConfig.isAcceptingFiles()) {
			showSyncFilesScreen(client, filesToRequest, filesToRemove);
			return;
		}
	}
	
	@Environment(EnvType.CLIENT)
	protected void showBCLibError(Minecraft client) {
		BCLib.LOGGER.error("BCLib differs on client and server.");
		client.setScreen(new WarnBCLibVersionMismatch((download) -> {
			Minecraft.getInstance()
					 .setScreen((Screen) null);
			if (download) {
				requestBCLibDownload((hadErrors) -> {
					client.stop();
				});
			}
		}));
	}
	
	@Environment(EnvType.CLIENT)
	protected void showSyncFilesScreen(Minecraft client, List<AutoSyncID> files, final List<AutoSyncID.ForDirectFileRequest> filesToRemove) {
		int configFiles = 0;
		int singleFiles = 0;
		int folderFiles = 0;
		int modFiles = 0;
		
		for (AutoSyncID aid : files){
			if (aid.isConfigFile()){
				configFiles++;
			} else if (aid instanceof AutoSyncID.ForModFileRequest){
				modFiles++;
			} else if (aid instanceof AutoSyncID.ForDirectFileRequest){
				folderFiles++;
			} else {
				singleFiles++;
			}
		}
		
		client.setScreen(new SyncFilesScreen(modFiles, configFiles, singleFiles, folderFiles, filesToRemove.size(), (downloadMods, downloadConfigs, downloadFiles, removeFiles) -> {
			Minecraft.getInstance()
					 .setScreen((Screen) null);
			if (downloadMods || downloadConfigs || downloadFiles) {
				BCLib.LOGGER.info("Updating local Files:");
				List<AutoSyncID.WithContentOverride> localChanges = new ArrayList<>(files.toArray().length);
				List<AutoSyncID> requestFiles = new ArrayList<>(files.toArray().length);
				
				files.forEach(aid -> {
					if (aid.isConfigFile() && downloadConfigs){
						processOfferedFile(requestFiles, aid);
					} else if (aid instanceof AutoSyncID.ForModFileRequest && downloadMods){
						processOfferedFile(requestFiles, aid);
					} else if (downloadFiles){
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
		}));
		
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
	
	private void requestBCLibDownload(Consumer<Boolean> whenFinished) {
		BCLib.LOGGER.warning("Starting download of BCLib");
		whenFinished.accept(true);
	}
	
	private void requestFileDownloads(List<AutoSyncID> files) {
		BCLib.LOGGER.info("Starting download of Files:" + files.size());
		DataExchangeAPI.send(new RequestFiles(files));
	}
}
