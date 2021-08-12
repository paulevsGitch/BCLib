package ru.bclib.api.dataexchange;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import ru.bclib.api.dataexchange.handler.DataExchange;

import java.io.File;
import java.util.List;

public class DataExchangeAPI extends DataExchange {
    private final static List<String> MODS = Lists.newArrayList();

    /**
     * You should never need to create a custom instance of this Object.
     */
    public DataExchangeAPI() {
        super();
    }

    @Environment(EnvType.CLIENT)
    protected ConnectorClientside clientSupplier(DataExchange api) {
        return new ConnectorClientside(api);
    }

    protected ConnectorServerside serverSupplier(DataExchange api) {
        return new ConnectorServerside(api);
    }

    /**
     * Register a mod to participate in the DataExchange.
     *
     * @param modID - {@link String} modID.
     */
    public static void registerMod(String modID) {
        if (!MODS.contains(modID))
            MODS.add(modID);
    }

    /**
     * Returns the IDs of all registered Mods.
     *
     * @return List of modIDs
     */
    public static List<String> registeredMods() {
        return MODS;
    }

    /**
     * Add a new Descriptor for a {@link DataHandler}.
     *
     * @param desc The Descriptor you want to add.
     */
    public static void registerDescriptor(DataHandlerDescriptor desc) {
        DataExchange api = DataExchange.getInstance();
        api.getDescriptors().add(desc);
    }

    /**
     * Bulk-Add a Descriptors for your {@link DataHandler}-Objects.
     *
     * @param desc The Descriptors you want to add.
     */
    public static void registerDescriptors(List<DataHandlerDescriptor> desc) {
        DataExchange api = DataExchange.getInstance();
        api.getDescriptors().addAll(desc);
    }

    /**
     * Sends the Handler.
     * <p>
     * Depending on what the result of {@link DataHandler#getOriginatesOnServer()}, the Data is sent from the server
     * to the client (if {@code true}) or the other way around.
     * <p>
     * The method {@link DataHandler#serializeData(FriendlyByteBuf)} is called just before the data is sent. You should
     * use this method to add the Data you need to the communication.
     *
     * @param h The Data that you want to send
     */
    public static void send(DataHandler h) {
        if (h.getOriginatesOnServer()) {
            DataExchangeAPI.getInstance().server.sendToClient(h);
        } else {
            DataExchangeAPI.getInstance().client.sendToServer(h);
        }
    }

    /**
     * Registers a File for automatic client syncing.
     *
     * @param modID    The ID of the calling Mod
     * @param fileName The name of the File
     */
    public static void addAutoSyncFile(String modID, File fileName) {
        getInstance().addAutoSyncFileData(modID, fileName, false, FileHash.NEED_TRANSFER);
    }

    /**
     * Registers a File for automatic client syncing.
     * <p>
     * The file is synced of the {@link FileHash} on client and server are not equal. This method will not copy the
     * configs content from the client to the server.
     *
     * @param modID    The ID of the calling Mod
     * @param uniqueID A unique Identifier for the File. (see {@link ru.bclib.api.dataexchange.FileHash#uniqueID} for
     *                 Details
     * @param fileName The name of the File
     */
    public static void addAutoSyncFile(String modID, String uniqueID, File fileName) {
        getInstance().addAutoSyncFileData(modID, uniqueID, fileName, false, FileHash.NEED_TRANSFER);
    }

    /**
     * Registers a File for automatic client syncing.
     * <p>
     * The content of the file is requested for comparison. This will copy the
     * entire file from the client to the server.
     * <p>
     * You should only use this option, if you need to compare parts of the file in order to decide
     * if the File needs to be copied. Normally using the {@link ru.bclib.api.dataexchange.FileHash}
     * for comparison is sufficient.
     *
     * @param modID          The ID of the calling Mod
     * @param fileName       The name of the File
     * @param needTransfer   If the predicate returns true, the file needs to get copied to the server.
     */
    public static void addAutoSyncFile(String modID, File fileName, NeedTransferPredicate needTransfer) {
        getInstance().addAutoSyncFileData(modID, fileName, true, needTransfer);
    }

    /**
     * Registers a File for automatic client syncing.
     * <p>
     * The content of the file is requested for comparison. This will copy the
     * entire file from the client to the server.
     * <p>
     * You should only use this option, if you need to compare parts of the file in order to decide
     * if the File needs to be copied. Normally using the {@link ru.bclib.api.dataexchange.FileHash}
     * for comparison is sufficient.
     *
     * @param modID          The ID of the calling Mod
     * @param uniqueID       A unique Identifier for the File. (see {@link ru.bclib.api.dataexchange.FileHash#uniqueID} for
     *                       Details
     * @param fileName       The name of the File
     * @param needTransfer   If the predicate returns true, the file needs to get copied to the server.
     */
    public static void addAutoSyncFile(String modID, String uniqueID, File fileName, NeedTransferPredicate needTransfer) {
        getInstance().addAutoSyncFileData(modID, uniqueID, fileName, true, needTransfer);
    }
}
