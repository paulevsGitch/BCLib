package ru.bclib.api.dataexchange.handler.autosync;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.dataexchange.SyncFileHash;
import ru.bclib.config.ConfigUI;
import ru.bclib.config.Configs;
import ru.bclib.config.NamedPathConfig;
import ru.bclib.util.PathUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class AutoSync {
	public static final String SYNC_CATEGORY = "auto_sync";
	public final static SyncFolderDescriptor SYNC_FOLDER = new SyncFolderDescriptor("BCLIB-SYNC", FabricLoader.getInstance()
																											  .getGameDir()
																											  .resolve("bclib-sync")
																											  .normalize()
																											  .toAbsolutePath(), true);
	
	@FunctionalInterface
	public interface NeedTransferPredicate {
		public boolean test(SyncFileHash clientHash, SyncFileHash serverHash, FileContentWrapper content);
	}
	
	@Environment(EnvType.CLIENT)
	public static class ClientConfig extends NamedPathConfig{
		public static final ConfigToken<Boolean> ENABLED = ConfigToken.Boolean(true, "enabled", SYNC_CATEGORY);
		@ConfigUI(leftPadding =8)
		public static final DependendConfigToken<Boolean> ACCEPT_CONFIGS = DependendConfigToken.Boolean(true,"acceptConfigs", SYNC_CATEGORY, (config)->config.get(ENABLED));
		@ConfigUI(leftPadding =8)
		public static final DependendConfigToken<Boolean> ACCEPT_FILES = DependendConfigToken.Boolean(true,"acceptFiles", SYNC_CATEGORY, (config)->config.get(ENABLED));
		@ConfigUI(leftPadding =8)
		public static final DependendConfigToken<Boolean> ACCEPT_MODS = DependendConfigToken.Boolean(true,"acceptMods", SYNC_CATEGORY, (config)->config.get(ENABLED));
		@ConfigUI(topPadding = 12)
		public static final ConfigToken<Boolean> DEBUG_HASHES = ConfigToken.Boolean(true, "debugHashes", SYNC_CATEGORY);
		
		
		public ClientConfig(){
			super(BCLib.MOD_ID, "client", false);
		}
		
		public boolean shouldPrintDebugHashes() {
			return get(DEBUG_HASHES);
		}
		
		public boolean isAllowingAutoSync() {
			return get(ENABLED);
		}
		
		public boolean isAcceptingMods() {
			return get(ACCEPT_MODS) /*&& isAllowingAutoSync()*/;
		}
		
		public  boolean isAcceptingConfigs() {
			return get(ACCEPT_CONFIGS) /*&& isAllowingAutoSync()*/;
		}
		
		public  boolean isAcceptingFiles() {
			return get(ACCEPT_FILES) /*&& isAllowingAutoSync()*/;
		}
	}
	
	public static class ServerConfig extends NamedPathConfig {
		public static final ConfigToken<Boolean> ENABLED = ConfigToken.Boolean(true, "enabled", SYNC_CATEGORY);
		public static final DependendConfigToken<Boolean> OFFER_CONFIGS = DependendConfigToken.Boolean(true,"offerConfigs", SYNC_CATEGORY, (config)->config.get(ENABLED));
		public static final DependendConfigToken<Boolean> OFFER_FILES = DependendConfigToken.Boolean(true,"offerFiles", SYNC_CATEGORY, (config)->config.get(ENABLED));
		public static final DependendConfigToken<Boolean> OFFER_MODS = DependendConfigToken.Boolean(true,"offerMods", SYNC_CATEGORY, (config)->config.get(ENABLED));
		
		public static final ConfigToken<List<String>> ADDITIONAL_MODS = ConfigToken.StringArray(new ArrayList<>(0),"additionalMods", SYNC_CATEGORY);
		
		
		public ServerConfig(){
			super(BCLib.MOD_ID, "server", false);
		}
		
		public boolean isAllowingAutoSync() {
			return get(ENABLED);
		}
		
		public boolean isOfferingConfigs() {
			return get(OFFER_CONFIGS) /*&& isAllowingAutoSync()*/;
		}
		
		public boolean isOfferingFiles() {
			return get(OFFER_FILES) /*&& isAllowingAutoSync()*/;
		}
		
		public boolean isOfferingMods() {
			return get(OFFER_MODS) /*&& isAllowingAutoSync()*/;
		}
		
		public String[] additionalModsForSync() {
			return new String[0];
		}
	}
	
	final static class AutoSyncTriple {
		public final SyncFileHash serverHash;
		public final byte[] serverContent;
		public final AutoFileSyncEntry localMatch;
		
		public AutoSyncTriple(SyncFileHash serverHash, byte[] serverContent, AutoFileSyncEntry localMatch) {
			this.serverHash = serverHash;
			this.serverContent = serverContent;
			this.localMatch = localMatch;
		}
		
		@Override
		public String toString() {
			return serverHash.modID + "." + serverHash.uniqueID;
		}
	}
	
	
	// ##### File Syncing
	protected final static List<BiConsumer<AutoSyncID, File>> onWriteCallbacks = new ArrayList<>(2);
	/**
	 * Register a function that is called whenever the client receives a file from the server and replaced toe local
	 * file with the new content.
	 * <p>
	 * This callback is usefull if you need to reload the new content before the game is quit.
	 *
	 * @param callback A Function that receives the AutoSyncID as well as the Filename.
	 */
	public static void addOnWriteCallback(BiConsumer<AutoSyncID, File> callback) {
		onWriteCallbacks.add(callback);
	}
	private static final List<AutoFileSyncEntry> autoSyncFiles = new ArrayList<>(4);
	
	public static List<AutoFileSyncEntry> getAutoSyncFiles() {
		return autoSyncFiles;
	}
	
	/**
	 * Registers a File for automatic client syncing.
	 *
	 * @param modID          The ID of the calling Mod
	 * @param needTransfer   If the predicate returns true, the file needs to get copied to the server.
	 * @param fileName       The name of the File
	 * @param requestContent When {@code true} the content of the file is requested for comparison. This will copy the
	 *                       entire file from the client to the server.
	 *                       <p>
	 *                       You should only use this option, if you need to compare parts of the file in order to decide
	 *                       If the File needs to be copied. Normally using the {@link SyncFileHash}
	 *                       for comparison is sufficient.
	 */
	public static void addAutoSyncFileData(String modID, File fileName, boolean requestContent, NeedTransferPredicate needTransfer) {
		if (!PathUtil.isChildOf(PathUtil.GAME_FOLDER, fileName.toPath())){
			BCLib.LOGGER.error(fileName + " is outside of Game Folder " + PathUtil.GAME_FOLDER);
		} else {
			autoSyncFiles.add(new AutoFileSyncEntry(modID, fileName, requestContent, needTransfer));
		}
	}
	
	/**
	 * Registers a File for automatic client syncing.
	 *
	 * @param modID          The ID of the calling Mod
	 * @param uniqueID       A unique Identifier for the File. (see {@link SyncFileHash#uniqueID} for
	 *                       Details
	 * @param needTransfer   If the predicate returns true, the file needs to get copied to the server.
	 * @param fileName       The name of the File
	 * @param requestContent When {@code true} the content of the file is requested for comparison. This will copy the
	 *                       entire file from the client to the server.
	 *                       <p>
	 *                       You should only use this option, if you need to compare parts of the file in order to decide
	 *                       If the File needs to be copied. Normally using the {@link SyncFileHash}
	 *                       for comparison is sufficient.
	 */
	public static void addAutoSyncFileData(String modID, String uniqueID, File fileName, boolean requestContent, NeedTransferPredicate needTransfer) {
		if (!PathUtil.isChildOf(PathUtil.GAME_FOLDER, fileName.toPath())){
			BCLib.LOGGER.error(fileName + " is outside of Game Folder " + PathUtil.GAME_FOLDER);
		} else {
			autoSyncFiles.add(new AutoFileSyncEntry(modID, uniqueID, fileName, requestContent, needTransfer));
		}
	}
	
	/**
	 * Called when {@code SendFiles} received a File on the Client and wrote it to the FileSystem.
	 * <p>
	 * This is the place where reload Code should go.
	 *
	 * @param aid  The ID of the received File
	 * @param file The location of the FIle on the client
	 */
	static void didReceiveFile(AutoSyncID aid, File file) {
		onWriteCallbacks.forEach(fkt -> fkt.accept(aid, file));
	}
	
	
	// ##### Folder Syncing
	static final List<SyncFolderDescriptor> syncFolderDescriptions = Arrays.asList(SYNC_FOLDER);
	
	private List<String> syncFolderContent;
	
	protected List<String> getSyncFolderContent() {
		if (syncFolderContent == null) {
			return new ArrayList<>(0);
		}
		return syncFolderContent;
	}
	
	private static boolean didRegisterAdditionalMods = false;
	//we call this from HelloClient on the Srérver to prepare transfer
	protected static void loadSyncFolder() {
		if (Configs.SERVER_CONFIG.isOfferingFiles()) {
			syncFolderDescriptions.forEach(desc -> desc.loadCache());
		}
		
		if (!didRegisterAdditionalMods && Configs.SERVER_CONFIG.isOfferingMods()){
			didRegisterAdditionalMods = true;
			List<String> modIDs = Configs.SERVER_CONFIG.get(ServerConfig.ADDITIONAL_MODS);
			if (modIDs != null){
				modIDs.stream().forEach(modID -> DataExchangeAPI.registerModDependency(modID));
			}
		}
		
	}
	
	protected static SyncFolderDescriptor getSyncFolderDescriptor(String folderID) {
		return syncFolderDescriptions.stream()
									 .filter(d -> d.equals(folderID))
									 .findFirst()
									 .orElse(null);
	}
	
	protected static Path localBasePathForFolderID(String folderID) {
		final SyncFolderDescriptor desc = getSyncFolderDescriptor(folderID);
		if (desc != null) {
			return desc.localFolder;
		}
		else {
			BCLib.LOGGER.warning("Unknown Sync-Folder ID '" + folderID + "'");
			return null;
		}
	}
	
	public static void registerSyncFolder(String folderID, Path localBaseFolder, boolean removeAdditionalFiles) {
		localBaseFolder = localBaseFolder.normalize();
		if (PathUtil.isChildOf(PathUtil.GAME_FOLDER, localBaseFolder)) {
			final SyncFolderDescriptor desc = new SyncFolderDescriptor(folderID, localBaseFolder, removeAdditionalFiles);
			if (syncFolderDescriptions.contains(desc)) {
				BCLib.LOGGER.warning("Tried to override Folder Sync '" + folderID + "' again.");
			}
			else {
				syncFolderDescriptions.add(desc);
			}
		}
		else {
			BCLib.LOGGER.error(localBaseFolder + " (from " + folderID + ") is outside the game directory " + PathUtil.GAME_FOLDER + ". Sync is not allowed.");
		}
	}
}
