package ru.bclib.api.dataexchange.handler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.dataexchange.handler.AutoSyncID.WithContentOverride;
import ru.bclib.api.dataexchange.handler.DataExchange.SyncFolderDescriptor;
import ru.bclib.api.dataexchange.handler.DataExchange.SyncFolderDescriptor.SubFile;
import ru.bclib.api.datafixer.DataFixerAPI;
import ru.bclib.config.Configs;
import ru.bclib.gui.screens.SyncFilesScreen;
import ru.bclib.gui.screens.WarnBCLibVersionMismatch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Sent from the Server to the Client.
 * <p>
 * For Details refer to {@link HelloServer}
 */
public class HelloClient extends DataHandler {
	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "hello_client"), HelloClient::new, false, false);
	
	public HelloClient() {
		super(DESCRIPTOR.IDENTIFIER, true);
	}
	
	public static ModContainer getModContainer(String modID) {
		Optional<ModContainer> optional = FabricLoader.getInstance()
													  .getModContainer(modID);
		return optional.orElse(null);
	}
	
	public static String getModVersion(String modID) {
		Optional<ModContainer> optional = FabricLoader.getInstance()
													  .getModContainer(modID);
		if (optional.isPresent()) {
			ModContainer modContainer = optional.get();
			return modContainer.getMetadata()
							   .getVersion()
							   .toString();
		}
		return "0.0.0";
	}
	
	static String getBCLibVersion() {
		return getModVersion(BCLib.MOD_ID);
	}
	
	@Override
	protected boolean prepareData(boolean isClient) {
		if (!Configs.MAIN_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "enabled", true)) {
			BCLib.LOGGER.info("Auto-Sync was disabled on the server.");
			return false;
		}
		
		DataExchange.getInstance().loadSyncFolder();
		return true;
	}
	
	@Override
	protected void serializeData(FriendlyByteBuf buf, boolean isClient) {
		final String vbclib = getBCLibVersion();
		BCLib.LOGGER.info("Sending Hello to Client. (server=" + vbclib + ")");
		final List<String> mods = DataExchangeAPI.registeredMods();
		
		//write BCLibVersion (=protocol version)
		buf.writeInt(DataFixerAPI.getModVersion(vbclib));
		
		if (Configs.MAIN_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "offerMods", true)) {
			//write Plugin Versions
			buf.writeInt(mods.size());
			for (String modID : mods) {
				writeString(buf, modID);
				final String ver = getModVersion(modID);
				buf.writeInt(DataFixerAPI.getModVersion(ver));
				BCLib.LOGGER.info("    - Listing Mod " + modID + " v" + ver);
			}
		}
		else {
			BCLib.LOGGER.info("Server will not list Mods.");
			buf.writeInt(0);
		}
		
		if (Configs.MAIN_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "offerFiles", true)) {
			//do only include files that exist on the server
			final List<AutoFileSyncEntry> existingAutoSyncFiles = DataExchange.getInstance()
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
		
		if (Configs.MAIN_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "offersSyncFolders", true)) {
			buf.writeInt(((DataExchange) DataExchange.getInstance()).syncFolderDescriptions.size());
			((DataExchange) DataExchange.getInstance()).syncFolderDescriptions.forEach(desc -> {
				BCLib.LOGGER.info("    - Offering Folder " + desc.localFolder + " (allowDelete=" + desc.removeAdditionalFiles + ")");
				desc.serialize(buf);
			});
		}
		else {
			BCLib.LOGGER.info("Server will not offer Sync Folders.");
			buf.writeInt(0);
		}
		Configs.MAIN_CONFIG.saveChanges();
	}
	
	String bclibVersion = "0.0.0";
	Map<String, String> modVersion = new HashMap<>();
	List<DataExchange.AutoSyncTriple> autoSyncedFiles = null;
	List<SyncFolderDescriptor> autoSynFolders = null;
	
	@Override
	protected void deserializeFromIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean fromClient) {
		//read BCLibVersion (=protocol version)
		bclibVersion = DataFixerAPI.getModVersion(buf.readInt());
		
		//read Plugin Versions
		modVersion = new HashMap<>();
		int count = buf.readInt();
		for (int i = 0; i < count; i++) {
			String id = readString(buf);
			String version = DataFixerAPI.getModVersion(buf.readInt());
			modVersion.put(id, version);
		}
		
		//read config Data
		count = buf.readInt();
		autoSyncedFiles = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			//System.out.println("Deserializing ");
			DataExchange.AutoSyncTriple t = AutoFileSyncEntry.deserializeAndMatch(buf);
			autoSyncedFiles.add(t);
			//System.out.println(t.first);
		}
		
		
		autoSynFolders = new ArrayList<>(1);
		//since v0.4.1 we also send the sync folders
		if (DataFixerAPI.isLargerOrEqualVersion(bclibVersion, "0.4.1")) {
			final int folderCount = buf.readInt();
			for (int i = 0; i < folderCount; i++) {
				SyncFolderDescriptor desc = SyncFolderDescriptor.deserialize(buf);
				autoSynFolders.add(desc);
			}
		}
	}
	
	private void processAutoSyncFolder(final List<AutoSyncID> filesToRequest, final List<AutoSyncID.ForDirectFileRequest> filesToRemove) {
		if (!Configs.CLIENT_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "syncFolders", true)) {
			return;
		}
		
		if (autoSynFolders.size() > 0) {
			BCLib.LOGGER.info("Folders offered by Server:");
		}
		
		autoSynFolders.forEach(desc -> {
			//desc contains the fileCache sent from the server, load the local version to get hold of the actual file cache on the client
			SyncFolderDescriptor localDescriptor = DataExchange.getSyncFolderDescriptor(desc.folderID);
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
							} else {
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
	
	private void processSingleFileSync(final List<AutoSyncID> filesToRequest) {
		final boolean debugHashes = Configs.CLIENT_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "debugHashes", false);
		
		if (autoSyncedFiles.size() > 0) {
			BCLib.LOGGER.info("Files offered by Server:");
		}
		
		//Handle single sync files
		//Single files need to be registered for sync on both client and server
		//There are no restrictions to the target folder, but the client decides the final
		//location.
		for (DataExchange.AutoSyncTriple e : autoSyncedFiles) {
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
	
	
	@Override
	protected void runOnGameThread(Minecraft client, MinecraftServer server, boolean isClient) {
		if (!Configs.CLIENT_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "enabled", true)) {
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
		
		for (Entry<String, String> e : modVersion.entrySet()) {
			String ver = getModVersion(e.getKey());
			BCLib.LOGGER.info("    - " + e.getKey() + " (client=" + ver + ", server=" + ver + ")");
		}
		
		processSingleFileSync(filesToRequest);
		processAutoSyncFolder(filesToRequest, filesToRemove);
		
		//Handle folder sync
		//Both client and server need to know about the folder you want to sync
		//Files can only get placed within that folder
		
		if ((filesToRequest.size() > 0 || filesToRemove.size() > 0) && SendFiles.acceptFiles()) {
			showDownloadConfigs(client, filesToRequest, filesToRemove);
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
	protected void showDownloadConfigs(Minecraft client, List<AutoSyncID> files, final List<AutoSyncID.ForDirectFileRequest> filesToRemove) {
		client.setScreen(new SyncFilesScreen((download) -> {
			Minecraft.getInstance()
					 .setScreen((Screen) null);
			if (download) {
				BCLib.LOGGER.info("Updating local Files:");
				List<AutoSyncID.WithContentOverride> localChanges = new ArrayList<>(files.toArray().length);
				List<AutoSyncID> requestFiles = new ArrayList<>(files.toArray().length);
				
				files.forEach(aid -> {
					if (aid instanceof WithContentOverride) {
						final WithContentOverride aidc = (WithContentOverride) aid;
						BCLib.LOGGER.info("    - " + aid + " (updating Content)");
						
						SendFiles.writeSyncedFile(aid, aidc.contentWrapper.getRawContent(), aidc.localFile);
					}
					else {
						requestFiles.add(aid);
						BCLib.LOGGER.info("    - " + aid + " (requesting)");
					}
				});
				
				filesToRemove.forEach(aid -> {
					BCLib.LOGGER.info("    - " + aid.relFile + " (removing)");
					
					aid.relFile.delete();
				});
				
				requestFileDownloads(requestFiles);
			}
		}));
		
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
