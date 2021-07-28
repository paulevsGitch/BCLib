package ru.bclib.api.dataexchange;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class DataExchangeAPI {
	private static DataExchangeAPI instance;
	private final ConnectorServerside server;
	private final ConnectorClientside client;
	
	public static DataExchangeAPI getInstance(){
		return instance;
	}
	
	private static DataExchangeAPI getOrCreateInstance(boolean isClient){
		if (instance==null){
			instance = new DataExchangeAPI(isClient);
		}
		return instance;
	}
	
	private DataExchangeAPI(boolean isClient){
		if (isClient){
			client = new ConnectorClientside(this);
			server = null;
			
			ClientPlayConnectionEvents.INIT.register(client::onPlayInit);
			ClientPlayConnectionEvents.JOIN.register(client::onPlayReady);
			ClientPlayConnectionEvents.DISCONNECT.register(client::onPlayDisconnect);
		} else {
			client = null;
			server = new ConnectorServerside(this);
			
			ServerPlayConnectionEvents.INIT.register(server::onPlayInit);
			ServerPlayConnectionEvents.JOIN.register(server::onPlayReady);
			ServerPlayConnectionEvents.DISCONNECT.register(server::onPlayDisconnect);
		}
	}
	
	
	@Environment(EnvType.CLIENT)
	public static void registerClientsideHandler(DataHandlerDescriptor desc){
		DataExchangeAPI api = DataExchangeAPI.getOrCreateInstance(true);
		if (api.client == null){
			throw new RuntimeException("[Internal Error] DataExchangeAPI was already created as a Server");
		}
		api.client.addDescriptor(desc);
	}
	
	@Environment(EnvType.SERVER)
	public static void registerServersideHandler(DataHandlerDescriptor desc){
		DataExchangeAPI api = DataExchangeAPI.getOrCreateInstance(false);
		if (api.server == null){
			throw new RuntimeException("[Internal Error] DataExchangeAPI was already created as a Client");
		}
		api.server.addDescriptor(desc);
	}
	
	public static void send(DataHandler h){
		if (h.getOriginatesOnServer()){
			DataExchangeAPI.getInstance().server.sendToClient(h);
		} else {
			DataExchangeAPI.getInstance().client.sendToServer(h);
		}
	}
	
}
