package ru.bclib.api.dataexchange.handler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import ru.bclib.api.dataexchange.ConnectorClientside;
import ru.bclib.api.dataexchange.ConnectorServerside;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.dataexchange.FileHash;
import ru.bclib.config.Configs;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

abstract public class DataExchange {
    public final static Path SYNC_FOLDER = FabricLoader.getInstance()
                                                       .getGameDir()
                                                       .resolve("bclib-sync")
                                                       .toAbsolutePath();
    public final static String SYNC_FOLDER_ID = "BCLIB-SYNC";
    
    @FunctionalInterface
    public interface NeedTransferPredicate  {
        public boolean test(FileHash clientHash, FileHash serverHash, FileContentWrapper content);
    }
    
    protected final static List<BiConsumer<AutoSyncID, File>> onWriteCallbacks = new ArrayList<>(2);

    final static class AutoSyncTriple {
        public final FileHash serverHash;
        public final byte[] serverContent;
        public final AutoFileSyncEntry localMatch;
        
        public AutoSyncTriple(FileHash serverHash, byte[] serverContent, AutoFileSyncEntry localMatch) {
            this.serverHash = serverHash;
            this.serverContent = serverContent;
            this.localMatch = localMatch;
        }
    
        @Override
        public String toString() {
            return serverHash.modID+"."+serverHash.uniqueID;
        }
    }

    private static DataExchangeAPI instance;
    protected static DataExchangeAPI getInstance(){
        if (instance==null){
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

    protected DataExchange(){
        descriptors = new HashSet<>();
    }

    public Set<DataHandlerDescriptor> getDescriptors() { return descriptors; }
    
    public List<AutoFileSyncEntry> getAutoSyncFiles(){
       return autoSyncFiles;
    }
    
    @Environment(EnvType.CLIENT)
    protected void initClientside(){
        if (client!=null) return;
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

    protected void initServerSide(){
        if (server!=null) return;
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
    public static void prepareClientside(){
        DataExchange api = DataExchange.getInstance();
        api.initClientside();

    }

    /**
     * Initializes all datastructures that need to exist in the server component.
     * <p>
     * This is automatically called by BCLib. You can register {@link DataHandler}-Objects before this Method is called
     */
    public static void prepareServerside(){
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
    public static void sendOnEnter(){
        getInstance().descriptors.forEach((desc)-> {
            if (desc.sendBeforeEnter){
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
     * @param modID The ID of the calling Mod
     * @param needTransfer If the predicate returns true, the file needs to get copied to the server.
     * @param fileName The name of the File
     * @param requestContent When {@code true} the content of the file is requested for comparison. This will copy the
     *                       entire file from the client to the server.
     *                       <p>
     *                       You should only use this option, if you need to compare parts of the file in order to decide
     *                       If the File needs to be copied. Normally using the {@link ru.bclib.api.dataexchange.FileHash}
     *                       for comparison is sufficient.
     */
    protected void addAutoSyncFileData(String modID, File fileName, boolean requestContent, NeedTransferPredicate needTransfer){
        autoSyncFiles.add(new AutoFileSyncEntry(modID, fileName, requestContent, needTransfer));
    }

    /**
     * Registers a File for automatic client syncing.
     *
     * @param modID The ID of the calling Mod
     * @param uniqueID A unique Identifier for the File. (see {@link ru.bclib.api.dataexchange.FileHash#uniqueID} for
     *                 Details
     * @param needTransfer If the predicate returns true, the file needs to get copied to the server.
     * @param fileName The name of the File
     * @param requestContent When {@code true} the content of the file is requested for comparison. This will copy the
     *                       entire file from the client to the server.
     *                       <p>
     *                       You should only use this option, if you need to compare parts of the file in order to decide
     *                       If the File needs to be copied. Normally using the {@link ru.bclib.api.dataexchange.FileHash}
     *                       for comparison is sufficient.
     */
    protected void addAutoSyncFileData(String modID, String uniqueID, File fileName, boolean requestContent, NeedTransferPredicate needTransfer){
       autoSyncFiles.add(new AutoFileSyncEntry(modID, uniqueID, fileName, requestContent, needTransfer));
    }

    /**
     * Called when {@code SendFiles} received a File on the Client and wrote it to the FileSystem.
     * <p>
     * This is the place where reload Code should go.
     * @param aid The ID of the received File
     * @param file The location of the FIle on the client
     */
    static void didReceiveFile(AutoSyncID aid, File file){
        onWriteCallbacks.forEach(fkt -> fkt.accept(aid, file));
    }
    
    private List<String> syncFolderContent;
    protected List<String> getSyncFolderContent(){
        if (syncFolderContent==null){
            return new ArrayList<>(0);
        }
        return syncFolderContent;
    }
    
    //we call this from HelloServer to prepare transfer
    protected void loadSyncFolder() {
        if (Configs.MAIN_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "offserSyncFolder", true))
        {
            final File syncPath = SYNC_FOLDER.toFile();
            if (!syncPath.exists()) {
                syncPath.mkdirs();
            }
    
            if (syncFolderContent == null) {
                syncFolderContent = new ArrayList<>(8);
                addFilesForSyncFolder(syncPath);
            }
        }
    }
    
    private void addFilesForSyncFolder(File path){
        for (final File f : path.listFiles()) {
            if (f.isDirectory()) {
                addFilesForSyncFolder(f);
            } else if (f.isFile()) {
                if (!f.getName().startsWith(".")) {
                    Path p = f.toPath();
                    p = SYNC_FOLDER.relativize(p);
                    syncFolderContent.add(p.toString());
                }
            }
        
        }
    }
}
