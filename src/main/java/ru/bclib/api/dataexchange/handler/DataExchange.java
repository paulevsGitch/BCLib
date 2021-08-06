package ru.bclib.api.dataexchange.handler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.FriendlyByteBuf;
import ru.bclib.api.dataexchange.ConnectorClientside;
import ru.bclib.api.dataexchange.ConnectorServerside;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.dataexchange.FileHash;
import ru.bclib.util.Pair;
import ru.bclib.util.Triple;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

abstract public class DataExchange {
    @FunctionalInterface
    public interface NeedTransferPredicate  {
        public boolean test(FileHash clientHash, FileHash serverHash, byte[] content);
    }
    
    final static class AutoSyncID extends Pair<String, String>{
        public AutoSyncID(String modID, String uniqueID) {
            super(modID, uniqueID);
        }
        
        public String getModID() { return this.first; }
        public String getUniqueID() { return this.second; }
    
        @Override
        public String toString() {
            return first+"."+second;
        }
    }

    final static class AutoSyncTriple extends Triple<FileHash, byte[], DataExchange.AutoFileSyncEntry>{
        public AutoSyncTriple(FileHash first, byte[] second, AutoFileSyncEntry third) {
            super(first, second, third);
        }
    
        @Override
        public String toString() {
            return first.modID+"."+first.uniqueID;
        }
    }
    static class AutoFileSyncEntry {
        public final NeedTransferPredicate needTransfer;
        public final File fileName;
        public final String modID;
        public final String uniqueID;
        public final boolean requestContent;
        private FileHash hash;

        AutoFileSyncEntry(String modID, File fileName, boolean requestContent, NeedTransferPredicate needTransfer) {
            this(modID, fileName.getName(), fileName, requestContent, needTransfer);
        }

        AutoFileSyncEntry(String modID, String uniqueID, File fileName, boolean requestContent, NeedTransferPredicate needTransfer) {
            this.needTransfer = needTransfer;
            this.fileName = fileName;
            this.modID = modID;
            this.uniqueID = uniqueID;
            this.requestContent = requestContent;
        }

        public FileHash getFileHash(){
            if (hash == null) {
                hash = FileHash.create(modID, fileName, uniqueID);
            }
            return hash;
        }

        public byte[] getContent(){
            if (!fileName.exists()) return new byte[0];
            final Path path = fileName.toPath();

            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {

            }
            return new byte[0];
        }

        public void serializeContent(FriendlyByteBuf buf){
            DataHandler.writeString(buf, modID);
            DataHandler.writeString(buf, uniqueID);
            serializeFileContent(buf);
        }
        public static Pair<AutoFileSyncEntry, byte[]> deserializeContent(FriendlyByteBuf buf){
            final String modID = DataHandler.readString(buf);
            final String uniqueID = DataHandler.readString(buf);
            byte[] data = deserializeFileContent(buf);

            AutoFileSyncEntry entry = AutoFileSyncEntry.findMatching(modID, uniqueID);
            return new Pair<>(entry, data);
        }

        
        public void serialize(FriendlyByteBuf buf){
            getFileHash().serialize(buf);
            buf.writeBoolean(requestContent);

            if (requestContent) {
                serializeFileContent(buf);
            }
        }

        public static AutoSyncTriple deserializeAndMatch(FriendlyByteBuf buf){
            Pair<FileHash, byte[]> e = deserialize(buf);
            AutoFileSyncEntry match = findMatching(e.first);
            return new AutoSyncTriple(e.first, e.second, match);
        }

        public static Pair<FileHash, byte[]> deserialize(FriendlyByteBuf buf){
            FileHash hash = FileHash.deserialize(buf);
            boolean withContent = buf.readBoolean();
            byte[] data = null;
            if (withContent) {
                data = deserializeFileContent(buf);
            }

            return new Pair(hash, data);
        }

        private void serializeFileContent(FriendlyByteBuf buf) {
            byte[] content = getContent();
            buf.writeInt(content.length);
            buf.writeByteArray(content);
        }

        private static byte[] deserializeFileContent(FriendlyByteBuf buf) {
            byte[] data;
            int size = buf.readInt();
            data = buf.readByteArray(size);
            return data;
        }

        public static AutoFileSyncEntry findMatching(FileHash hash){
            return findMatching(hash.modID, hash.uniqueID);
        }
        public static AutoFileSyncEntry findMatching(AutoSyncID aid){
            return findMatching(aid.getModID(), aid.getUniqueID());
        }

        public static AutoFileSyncEntry findMatching(String modID, String uniqueID){
            return DataExchange
                    .getInstance()
                    .autoSyncFiles
                    .stream()
                    .filter(asf -> asf.modID.equals(modID) && asf.uniqueID.equals(uniqueID))
                    .findFirst()
                    .orElse(null);
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
    protected final List<AutoFileSyncEntry> autoSyncFiles = new ArrayList<>(4);

    private final Function<DataExchange, ConnectorClientside> clientSupplier;
    private final Function<DataExchange, ConnectorServerside> serverSupplier;

    protected DataExchange(Function<DataExchange, ConnectorClientside> client, Function<DataExchange, ConnectorServerside> server){
        descriptors = new HashSet<>();
        this.clientSupplier = client;
        this.serverSupplier = server;
    }

    public Set<DataHandlerDescriptor> getDescriptors() { return descriptors; }

    @Environment(EnvType.CLIENT)
    protected void initClientside(){
        if (client!=null) return;
        client = clientSupplier.apply(this);
        ClientLoginConnectionEvents.INIT.register((a, b) ->{
            System.out.println("INIT");
        });
        ClientLoginConnectionEvents.QUERY_START.register((a, b) ->{
            System.out.println("INIT");
        });
        ClientPlayConnectionEvents.INIT.register(client::onPlayInit);
        ClientPlayConnectionEvents.JOIN.register(client::onPlayReady);
        ClientPlayConnectionEvents.DISCONNECT.register(client::onPlayDisconnect);
    }

    protected void initServerSide(){
        if (server!=null) return;
        server = serverSupplier.apply(this);

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
     * {@Code true},
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
}
