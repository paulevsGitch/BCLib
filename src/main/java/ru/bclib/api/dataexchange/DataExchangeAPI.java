package ru.bclib.api.dataexchange;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.FriendlyByteBuf;
import ru.bclib.api.dataexchange.handler.DataExchange;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class DataExchangeAPI extends DataExchange {
	private final static List<String> MODS = Lists.newArrayList();

	protected DataExchangeAPI() {
		super((api) -> new ConnectorClientside(api), (api) -> new ConnectorServerside(api));
	}


	/**
	 * Register a mod to participate in the DataExchange.
	 *
	 * @param modID - {@link String} modID.
	 */
	public static void registerMod(String modID) {
		MODS.add(modID);
	}
	
	/**
	 * Returns the IDs of all registered Mods.
	 * @return List of modIDs
	 */
	public static List<String> registeredMods(){
		return MODS;
	}
	
	/**
	 * Add a new Descriptor for a DataHandler.
	 * @param desc The Descriptor you want to add.
	 */
	public static void registerDescriptor(DataHandlerDescriptor desc){
		DataExchangeAPI api = DataExchange.getInstance();
		api.descriptors.add(desc);
	}

	/**
	 * Sends the Handler.
	 * <p>
	 * Depending on what the result of {@link DataHandler#getOriginatesOnServer()}, the Data is sent from the server
	 * to the client (if {@code true}) or the other way around.
	 * <p>
	 * The method {@link DataHandler#serializeData(FriendlyByteBuf)} is called just before the data is sent. You should
	 * use this method to add the Data you need to the communication.
	 * @param h The Data that you want to send
	 */
	public static void send(DataHandler h){
		if (h.getOriginatesOnServer()){
			DataExchangeAPI.getInstance().server.sendToClient(h);
		} else {
			DataExchangeAPI.getInstance().client.sendToServer(h);
		}
	}

	/**
	 * Registers a File for automatic client syncing.
	 *
	 * @param needTransfer If the predicate returns true, the file needs to get copied to the server.
	 * @param fileName The name of the File
	 */
	public static void addAutoSyncFile(Predicate<Object> needTransfer, File fileName){
		DataExchangeAPI.getInstance().addAutoSyncFileData(needTransfer, fileName);
	}
}
