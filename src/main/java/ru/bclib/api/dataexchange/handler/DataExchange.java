package ru.bclib.api.dataexchange.handler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import ru.bclib.api.dataexchange.ConnectorClientside;
import ru.bclib.api.dataexchange.ConnectorServerside;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

abstract public class DataExchange {
    private static class AutoFileSyncEntry {
        public final Predicate<Object> needTransfer;
        public final File fileName;

        private AutoFileSyncEntry(Predicate<Object> needTransfer, File fileName) {
            this.needTransfer = needTransfer;
            this.fileName = fileName;
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
    public void prepareClientside(){
        DataExchangeAPI api = DataExchangeAPI.getInstance();
        api.initClientside();

    }

    /**
     * Initializes all datastructures that need to exist in the server component.
     * <p>
     * This is automatically called by BCLib. You can register {@link DataHandler}-Objects before this Method is called
     */
    public static void prepareServerside(){
        DataExchange api = DataExchangeAPI.getInstance();
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
     * @param needTransfer If the predicate returns true, the file needs to get copied to the server.
     * @param fileName The name of the File
     */
    protected void addAutoSyncFileData(Predicate<Object> needTransfer, File fileName){
       autoSyncFiles.add(new AutoFileSyncEntry(needTransfer, fileName));
    }
}
