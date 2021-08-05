package ru.bclib.api.dataexchange;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.FriendlyByteBuf;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class DataExchangeAPI {
	private final static List<String> MODS = Lists.newArrayList();
	private static DataExchangeAPI instance;
	private ConnectorServerside server;
	private ConnectorClientside client;
	protected final Set<DataHandlerDescriptor> descriptors;

	private static class AutoFileSyncEntry {
		public final Predicate<Object> needTransfer;
		public final File fileName;

		private AutoFileSyncEntry(Predicate<Object> needTransfer, File fileName) {
			this.needTransfer = needTransfer;
			this.fileName = fileName;
		}
	}
	protected final List<AutoFileSyncEntry> autoSyncFiles = new ArrayList<>(4);
	
	
	private DataExchangeAPI(){
		descriptors = new HashSet<>();
	}
	
	static DataExchangeAPI getInstance(){
		if (instance==null){
			instance = new DataExchangeAPI();
		}
		return instance;
	}
	
	@Environment(EnvType.CLIENT)
	private void initClientside(){
		if (client!=null) return;
		client = new ConnectorClientside(this);
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
	
	private void initServerSide(){
		if (server!=null) return;
		server = new ConnectorServerside(this);
		
		ServerPlayConnectionEvents.INIT.register(server::onPlayInit);
		ServerPlayConnectionEvents.JOIN.register(server::onPlayReady);
		ServerPlayConnectionEvents.DISCONNECT.register(server::onPlayDisconnect);
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
		DataExchangeAPI api = DataExchangeAPI.getInstance();
		api.descriptors.add(desc);
	}
	
	
	/**
	 * Initializes all datastructures that need to exist in the client component.
	 * <p>
	 * This is automatically called by BCLib. You can register {@link DataHandler}-Objects before this Method is called
	 */
	@Environment(EnvType.CLIENT)
	public static void prepareClientside(){
		DataExchangeAPI api = DataExchangeAPI.getInstance();
		api.initClientside();
		
	}
	
	/**
	 * Initializes all datastructures that need to exist in the server component.
	 * <p>
	 * This is automatically called by BCLib. You can register {@link DataHandler}-Objects before this Method is called
	 */
	public static void prepareServerside(){
		DataExchangeAPI api = DataExchangeAPI.getInstance();
		api.initServerSide();
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
	 * Automatically called before the player enters the world.
	 * <p>
	 * This will send all {@link DataHandler}-Objects that have {@link DataHandlerDescriptor#sendBeforeEnter} set to*
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
	public static void addAutoSyncFile(Predicate<Object> needTransfer, File fileName){
		DataExchangeAPI.getInstance().autoSyncFiles.add(new AutoFileSyncEntry(needTransfer, fileName));
	}
}
