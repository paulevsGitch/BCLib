package ru.bclib.api.dataexchange;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.HashSet;
import java.util.Set;

public class DataExchangeAPI {
	private static DataExchangeAPI instance;
	private ConnectorServerside server;
	private ConnectorClientside client;
	protected final Set<DataHandlerDescriptor> descriptors;
	
	
	private DataExchangeAPI(){
		descriptors = new HashSet<>();
	}
	
	public static DataExchangeAPI getInstance(){
		if (instance==null){
			instance = new DataExchangeAPI();
		}
		return instance;
	}
	
	@Environment(EnvType.CLIENT)
	private void initClientside(){
		if (client!=null) return;
		client = new ConnectorClientside(this);
		
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
	
	public static void registerDescriptor(DataHandlerDescriptor desc){
		DataExchangeAPI api = DataExchangeAPI.getInstance();
		api.descriptors.add(desc);
	}
	
	
	@Environment(EnvType.CLIENT)
	public static void prepareClientside(){
		DataExchangeAPI api = DataExchangeAPI.getInstance();
		api.initClientside();
		
	}
	
	public static void prepareServerside(){
		DataExchangeAPI api = DataExchangeAPI.getInstance();
		api.initServerSide();
	}
	
	public static void send(DataHandler h){
		if (h.getOriginatesOnServer()){
			DataExchangeAPI.getInstance().server.sendToClient(h);
		} else {
			DataExchangeAPI.getInstance().client.sendToServer(h);
		}
	}
	
	
}
