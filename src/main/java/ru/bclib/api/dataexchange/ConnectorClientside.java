package ru.bclib.api.dataexchange;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import ru.bclib.BCLib;

@Environment(EnvType.CLIENT)
class ConnectorClientside extends Connector {
	private Minecraft client;
	ConnectorClientside(DataExchangeAPI api) {
		super(api);
		this.client = null;
	}
	
	
	@Override
	public boolean onClient() {
		return true;
	}
	
	protected void onPlayInit(ClientPacketListener handler, Minecraft client){
		if (this.client!=null && this.client != client){
			BCLib.LOGGER.warning("Client changed!");
		}
		this.client = client;
		for(DataHandlerDescriptor desc : getDescriptors()){
			ClientPlayNetworking.registerReceiver(desc.IDENTIFIER, (_client, _handler, _buf, _responseSender)->{
				receiveFromServer(desc, _client, _handler, _buf, _responseSender);
			});
		}
	}
	
	void onPlayReady(ClientPacketListener handler, PacketSender sender, Minecraft client){
		for(DataHandlerDescriptor desc : getDescriptors()){
			if (desc.sendOnJoin){
				DataHandler h = desc.JOIN_INSTANCE.get();
				if (!h.getOriginatesOnServer()) {
					h.sendToServer(client);
				}
			}
		}
	}
	
	void onPlayDisconnect(ClientPacketListener handler, Minecraft client){
		for(DataHandlerDescriptor desc : getDescriptors()) {
			ClientPlayNetworking.unregisterReceiver(desc.IDENTIFIER);
		}
	}
	
	void receiveFromServer(DataHandlerDescriptor desc, Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender){
		DataHandler h = desc.INSTANCE.get();
		h.receiveFromServer(client, handler, buf, responseSender);
	}
	
	void sendToServer(DataHandler h){
		if (client==null){
			throw new RuntimeException("[internal error] Client not initialized yet!");
		}
		h.sendToServer(this.client);
	}
}
