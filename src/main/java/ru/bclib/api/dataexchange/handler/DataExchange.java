package ru.bclib.api.dataexchange.handler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.ConnectorClientside;
import ru.bclib.api.dataexchange.ConnectorServerside;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.dataexchange.FileHash;
import ru.bclib.api.dataexchange.SyncFileHash;
import ru.bclib.api.dataexchange.handler.AutoSyncID.ForDirectFileRequest;
import ru.bclib.config.Configs;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

abstract public class DataExchange {
	public final static SyncFolderDescriptor SYNC_FOLDER = new SyncFolderDescriptor("BCLIB-SYNC", FabricLoader.getInstance()
																											  .getGameDir()
																											  .resolve("bclib-sync")
																											  .toAbsolutePath(), true);
	final List<SyncFolderDescriptor> syncFolderDescriptions = Arrays.asList(SYNC_FOLDER);
	
	public static class SyncFolderDescriptor {
		static class SubFile {
			public final String relPath;
			public final FileHash hash;
			
			
			SubFile(String relPath, FileHash hash) {
				this.relPath = relPath;
				this.hash = hash;
			}
			
			@Override
			public String toString() {
				return relPath;
			}
			
			public void serialize(FriendlyByteBuf buf) {
				DataHandler.writeString(buf, relPath);
				hash.serialize(buf);
			}
			
			public static SubFile deserialize(FriendlyByteBuf buf) {
				final String relPath = DataHandler.readString(buf);
				FileHash hash = FileHash.deserialize(buf);
				return new SubFile(relPath, hash);
			}
			
			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o instanceof String) return relPath.equals(o);
				if (!(o instanceof SubFile)) return false;
				SubFile subFile = (SubFile) o;
				return relPath.equals(subFile.relPath);
			}
			
			@Override
			public int hashCode() {
				return relPath.hashCode();
			}
		}
		
		@NotNull
		public final String folderID;
		public final boolean removeAdditionalFiles;
		@NotNull
		public final Path localFolder;
		
		private List<SubFile> fileCache;
		
		SyncFolderDescriptor(String folderID, Path localFolder, boolean removeAdditionalFiles) {
			this.removeAdditionalFiles = removeAdditionalFiles;
			this.folderID = folderID;
			this.localFolder = localFolder;
			fileCache = null;
		}
		
		@Override
		public String toString() {
			return "SyncFolderDescriptor{" + "folderID='" + folderID + '\'' + ", removeAdditionalFiles=" + removeAdditionalFiles + ", localFolder=" + localFolder + ", files=" + (fileCache == null ? "?" : fileCache.size()) + "}";
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o instanceof String) {
				return folderID.equals(o);
			}
			if (o instanceof AutoSyncID.ForDirectFileRequest) {
				return folderID.equals(((ForDirectFileRequest) o).uniqueID);
			}
			if (!(o instanceof SyncFolderDescriptor)) return false;
			SyncFolderDescriptor that = (SyncFolderDescriptor) o;
			return folderID.equals(that.folderID);
		}
		
		@Override
		public int hashCode() {
			return folderID.hashCode();
		}
		
		public int fileCount(){
			return fileCache==null?0:fileCache.size();
		}
		
		public void invalidateCache() {
			fileCache = null;
		}
		
		public void loadCache() {
			if (fileCache == null) {
				fileCache = new ArrayList<>(8);
				fileWalker(localFolder.toFile(), p -> fileCache.add(new SubFile(localFolder.relativize(p)
																						   .toString(), FileHash.create(p.toFile()))));
			}
		}
		
		public void serialize(FriendlyByteBuf buf) {
			final boolean debugHashes = Configs.CLIENT_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "debugHashes", false);
			loadCache();
			
			DataHandler.writeString(buf, folderID);
			buf.writeBoolean(removeAdditionalFiles);
			buf.writeInt(fileCache.size());
			fileCache.forEach(fl -> {
				BCLib.LOGGER.info("      - " + fl.relPath);
				if (debugHashes){
					BCLib.LOGGER.info("        " + fl.hash);
				}
				fl.serialize(buf);
			});
		}
		
		public static SyncFolderDescriptor deserialize(FriendlyByteBuf buf) {
			final String folderID = DataHandler.readString(buf);
			final boolean remAddFiles = buf.readBoolean();
			final int count = buf.readInt();
			SyncFolderDescriptor localDescriptor = DataExchange.getInstance()
															   .getSyncFolderDescriptor(folderID);
			
			final SyncFolderDescriptor desc;
			if (localDescriptor != null) {
				desc = new SyncFolderDescriptor(folderID, localDescriptor.localFolder, remAddFiles);
				desc.fileCache = new ArrayList<>(count);
			}
			else {
				BCLib.LOGGER.warning(BCLib.isClient() ? "Client" : "Server" + " does not know Sync-Folder ID '" + folderID + "'");
				desc = null;
			}
			
			for (int i = 0; i < count; i++) {
				SubFile relPath = SubFile.deserialize(buf);
				if (desc != null) desc.fileCache.add(relPath);
			}
			
			return desc;
		}
		
		//Note: make sure loadCache was called before using this
		boolean hasRelativeFile(String relFile) {
			return fileCache.stream().filter(sf -> sf.equals(relFile)).findFirst().isPresent();
		}
		
		//Note: make sure loadCache was called before using this
		boolean hasRelativeFile(SubFile subFile) {
			return hasRelativeFile(subFile.relPath);
		}
		
		//Note: make sure loadCache was called before using this
		SubFile getLocalSubFile(String relPath){
			return fileCache.stream().filter(sf -> sf.relPath.equals(relPath)).findFirst().orElse(null);
		}
		
		Stream<SubFile> relativeFilesStream() {
			loadCache();
			return fileCache.stream();
		}
	}
	
	@FunctionalInterface
	public interface NeedTransferPredicate {
		public boolean test(SyncFileHash clientHash, SyncFileHash serverHash, FileContentWrapper content);
	}
	
	protected final static List<BiConsumer<AutoSyncID, File>> onWriteCallbacks = new ArrayList<>(2);
	
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
	
	private static DataExchangeAPI instance;
	
	protected static DataExchangeAPI getInstance() {
		if (instance == null) {
			instance = new DataExchangeAPI();
		}
		return instance;
	}
	
	protected ConnectorServerside server;
	protected ConnectorClientside client;
	protected final Set<DataHandlerDescriptor> descriptors;
	private final List<AutoFileSyncEntry> autoSyncFiles = new ArrayList<>(4);
	
	private boolean didLoadSyncFolder = false;
	
	abstract protected ConnectorClientside clientSupplier(DataExchange api);
	
	abstract protected ConnectorServerside serverSupplier(DataExchange api);
	
	protected DataExchange() {
		descriptors = new HashSet<>();
	}
	
	public Set<DataHandlerDescriptor> getDescriptors() { return descriptors; }
	
	public List<AutoFileSyncEntry> getAutoSyncFiles() {
		return autoSyncFiles;
	}
	
	@Environment(EnvType.CLIENT)
	protected void initClientside() {
		if (client != null) return;
		client = clientSupplier(this);
        /*ClientLoginConnectionEvents.INIT.register((a, b) ->{
            System.out.println("INIT");
        });
        ClientLoginConnectionEvents.QUERY_START.register((a, b) ->{
            System.out.println("INIT");
        });*/
		ClientPlayConnectionEvents.INIT.register(client::onPlayInit);
		ClientPlayConnectionEvents.JOIN.register(client::onPlayReady);
		ClientPlayConnectionEvents.DISCONNECT.register(client::onPlayDisconnect);
	}
	
	protected void initServerSide() {
		if (server != null) return;
		server = serverSupplier(this);
		
		ServerPlayConnectionEvents.INIT.register(server::onPlayInit);
		ServerPlayConnectionEvents.JOIN.register(server::onPlayReady);
		ServerPlayConnectionEvents.DISCONNECT.register(server::onPlayDisconnect);
	}
	
	/**
	 * Initializes all datastructures that need to exist in the client component.
	 * <p>
	 * This is automatically called by BCLib. You can register {@link DataHandler}-Objects before this Method is called
	 */
	@Environment(EnvType.CLIENT)
	public static void prepareClientside() {
		DataExchange api = DataExchange.getInstance();
		api.initClientside();
		
	}
	
	/**
	 * Initializes all datastructures that need to exist in the server component.
	 * <p>
	 * This is automatically called by BCLib. You can register {@link DataHandler}-Objects before this Method is called
	 */
	public static void prepareServerside() {
		DataExchange api = DataExchange.getInstance();
		api.initServerSide();
	}
	
	
	/**
	 * Automatically called before the player enters the world.
	 * <p>
	 * This is automatically called by BCLib. It will send all {@link DataHandler}-Objects that have {@link DataHandlerDescriptor#sendBeforeEnter} set to*
	 * {@code true},
	 */
	@Environment(EnvType.CLIENT)
	public static void sendOnEnter() {
		getInstance().descriptors.forEach((desc) -> {
			if (desc.sendBeforeEnter) {
				DataHandler h = desc.JOIN_INSTANCE.get();
				if (!h.getOriginatesOnServer()) {
					getInstance().client.sendToServer(h);
				}
			}
		});
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
	protected void addAutoSyncFileData(String modID, File fileName, boolean requestContent, NeedTransferPredicate needTransfer) {
		autoSyncFiles.add(new AutoFileSyncEntry(modID, fileName, requestContent, needTransfer));
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
	protected void addAutoSyncFileData(String modID, String uniqueID, File fileName, boolean requestContent, NeedTransferPredicate needTransfer) {
		autoSyncFiles.add(new AutoFileSyncEntry(modID, uniqueID, fileName, requestContent, needTransfer));
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
	
	private List<String> syncFolderContent;
	
	protected List<String> getSyncFolderContent() {
		if (syncFolderContent == null) {
			return new ArrayList<>(0);
		}
		return syncFolderContent;
	}
	
	//we call this from HelloServer to prepare transfer
	protected void loadSyncFolder() {
		if (Configs.MAIN_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "offersSyncFolders", true)) {
			syncFolderDescriptions.forEach(desc -> desc.loadCache());
		}
	}
	
	protected static SyncFolderDescriptor getSyncFolderDescriptor(String folderID) {
		return ((DataExchange) getInstance()).syncFolderDescriptions.stream()
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
	
	protected void registerSyncFolder(String folderID, Path localBaseFolder, boolean removeAdditionalFiles) {
		final SyncFolderDescriptor desc = new SyncFolderDescriptor(folderID, localBaseFolder, removeAdditionalFiles);
		if (this.syncFolderDescriptions.contains(desc)) {
			BCLib.LOGGER.warning("Tried to override Folder Sync '" + folderID + "' again.");
		}
		else {
			this.syncFolderDescriptions.add(desc);
		}
	}
	
	
	/**
	 * A simple directory walker that ignores dot-files
	 *
	 * @param path         The path where you want to start
	 * @param pathConsumer The consumer called for each valid file. The consumer will get an absolute {@link Path}-Object
	 *                     for each visited file
	 */
	public static void fileWalker(File path, Consumer<Path> pathConsumer) {
		if (!path.exists()) return;
		for (final File f : path.listFiles()) {
			if (f.getName()
				 .startsWith(".")) continue;
			if (f.isDirectory()) {
				fileWalker(f, pathConsumer);
			}
			else if (f.isFile()) {
				pathConsumer.accept(f.toPath());
			}
		}
	}
}
