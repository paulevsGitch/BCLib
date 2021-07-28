package ru.bclib.api.dataexchange;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import ru.bclib.BCLib;

@Environment(EnvType.SERVER)
class ConnectorServerside extends Connector {
	private MinecraftServer server;
	ConnectorServerside(DataExchangeAPI api) {
		super(api);
		server = null;
	}
	
	@Override
	public boolean onClient() {
		return false;
	}
	
	protected void onPlayInit(ServerGamePacketListenerImpl handler, MinecraftServer server){
		if (this.server!=null && this.server != server){
			BCLib.LOGGER.warning("Server changed!");
		}
		this.server = server;
		for(DataHandlerDescriptor desc : descriptors){
			ServerPlayNetworking.registerReceiver(handler, desc.identifier, (_server, _player, _handler, _buf, _responseSender) -> {
				receiveFromClient(desc, _server, _player, _handler, _buf, _responseSender);
			});
		}
	}
	
	void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server){
		for(DataHandlerDescriptor desc : descriptors){
			if (desc.sendOnJoin){
				DataHandler h = desc.instancer.get();
				h.sendToClient(server, handler.player);
			}
		}
	}
	
	void onPlayDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server){
		for(DataHandlerDescriptor desc : descriptors){
			ServerPlayNetworking.unregisterReceiver(handler, desc.identifier);
		}
	}
	
	void receiveFromClient(DataHandlerDescriptor desc, MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender){
		DataHandler h = desc.instancer.get();
		h.receiveFromClient(server, player, handler, buf, responseSender);
	}
	
	void sendToClient(DataHandler h){
		if (server==null){
			throw new RuntimeException("[internal error] Server not initialized yet!");
		}
		h.sendToClient(this.server);
	}
}
