package ru.bclib.api.dataexchange;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import ru.bclib.BCLib;

public abstract class DataHandler extends BaseDataHandler {
	public abstract static class WithoutPayload extends DataHandler {
		protected WithoutPayload(ResourceLocation identifier, boolean originatesOnServer) {
			super(identifier, originatesOnServer);
		}
		
		@Override
		protected boolean prepareData(boolean isClient) { return true; }
		
		@Override
		protected void serializeData(FriendlyByteBuf buf, boolean isClient) {
		}
		
		@Override
		protected void deserializeIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean isClient) {
		}
	}
	
	protected DataHandler(ResourceLocation identifier, boolean originatesOnServer) {
		super(identifier, originatesOnServer);
	}
	
	protected boolean prepareData(boolean isClient) { return true; }
	
	abstract protected void serializeData(FriendlyByteBuf buf, boolean isClient);
	
	abstract protected void deserializeIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean isClient);
	
	abstract protected void runOnGameThread(Minecraft client, MinecraftServer server, boolean isClient);
	
	
	@Environment(EnvType.CLIENT)
	@Override
	void receiveFromServer(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
		deserializeIncomingData(buf, responseSender, true);
		client.execute(() -> runOnGameThread(client, null, true));
	}
	
	@Override
	void receiveFromClient(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
		super.receiveFromClient(server, player, handler, buf, responseSender);
		
		deserializeIncomingData(buf, responseSender, false);
		server.execute(() -> runOnGameThread(null, server, false));
	}
	
	@Override
	void sendToClient(MinecraftServer server) {
		if (prepareData(false)) {
			FriendlyByteBuf buf = PacketByteBufs.create();
			serializeData(buf, false);
			
			for (ServerPlayer player : PlayerLookup.all(server)) {
				ServerPlayNetworking.send(player, getIdentifier(), buf);
			}
		}
	}
	
	@Override
	void sendToClient(MinecraftServer server, ServerPlayer player) {
		if (prepareData(false)) {
			FriendlyByteBuf buf = PacketByteBufs.create();
			serializeData(buf, false);
			ServerPlayNetworking.send(player, getIdentifier(), buf);
		}
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	void sendToServer(Minecraft client) {
		if (prepareData(true)) {
			FriendlyByteBuf buf = PacketByteBufs.create();
			serializeData(buf, true);
			ClientPlayNetworking.send(getIdentifier(), buf);
		}
	}
	
	/**
	 * A Message that always originates on the Client
	 */
	public abstract static class FromClient extends BaseDataHandler {
		public abstract static class WithoutPayload extends FromClient {
			protected WithoutPayload(ResourceLocation identifier) {
				super(identifier);
			}
			
			@Override
			protected boolean prepareDataOnClient() { return true; }
			
			@Override
			protected void serializeDataOnClient(FriendlyByteBuf buf) {
			}
			
			@Override
			protected void deserializeIncomingDataOnServer(FriendlyByteBuf buf, PacketSender responseSender) {
			}
		}
		
		protected FromClient(ResourceLocation identifier) {
			super(identifier, false);
		}
		
		@Environment(EnvType.CLIENT)
		protected boolean prepareDataOnClient() { return true; }
		
		@Environment(EnvType.CLIENT)
		abstract protected void serializeDataOnClient(FriendlyByteBuf buf);
		
		abstract protected void deserializeIncomingDataOnServer(FriendlyByteBuf buf, PacketSender responseSender);
		
		abstract protected void runOnServerGameThread(MinecraftServer server);
		
		
		@Environment(EnvType.CLIENT)
		@Override
		void receiveFromServer(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
			BCLib.LOGGER.error("[Internal Error] The message '" + getIdentifier() + "' must originate from the client!");
		}
		
		@Override
		void receiveFromClient(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
			super.receiveFromClient(server, player, handler, buf, responseSender);
			
			deserializeIncomingDataOnServer(buf, responseSender);
			server.execute(() -> runOnServerGameThread(server));
		}
		
		@Override
		void sendToClient(MinecraftServer server) {
			BCLib.LOGGER.error("[Internal Error] The message '" + getIdentifier() + "' must originate from the client!");
		}
		
		@Override
		void sendToClient(MinecraftServer server, ServerPlayer player) {
			BCLib.LOGGER.error("[Internal Error] The message '" + getIdentifier() + "' must originate from the client!");
		}
		
		@Environment(EnvType.CLIENT)
		@Override
		void sendToServer(Minecraft client) {
			if (prepareDataOnClient()) {
				FriendlyByteBuf buf = PacketByteBufs.create();
				serializeDataOnClient(buf);
				ClientPlayNetworking.send(getIdentifier(), buf);
			}
		}
	}
	
	/**
	 * A Message that always originates on the Server
	 */
	public abstract static class FromServer extends BaseDataHandler {
		public abstract static class WithoutPayload extends FromServer {
			protected WithoutPayload(ResourceLocation identifier) {
				super(identifier);
			}
			
			@Override
			protected boolean prepareDataOnServer() { return true; }
			
			@Override
			protected void serializeDataOnServer(FriendlyByteBuf buf) {
			}
			
			@Override
			protected void deserializeIncomingDataOnClient(FriendlyByteBuf buf, PacketSender responseSender) {
			}
		}
		
		protected FromServer(ResourceLocation identifier) {
			super(identifier, true);
		}
		
		protected boolean prepareDataOnServer() { return true; }
		
		abstract protected void serializeDataOnServer(FriendlyByteBuf buf);
		
		@Environment(EnvType.CLIENT)
		abstract protected void deserializeIncomingDataOnClient(FriendlyByteBuf buf, PacketSender responseSender);
		
		@Environment(EnvType.CLIENT)
		abstract protected void runOnClientGameThread(Minecraft client);
		
		
		@Environment(EnvType.CLIENT)
		@Override
		final void receiveFromServer(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
			deserializeIncomingDataOnClient(buf, responseSender);
			client.execute(() -> runOnClientGameThread(client));
		}
		
		@Override
		final void receiveFromClient(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
			super.receiveFromClient(server, player, handler, buf, responseSender);
			BCLib.LOGGER.error("[Internal Error] The message '" + getIdentifier() + "' must originate from the server!");
		}
		
		@Override
		final void sendToClient(MinecraftServer server) {
			if (prepareDataOnServer()) {
				FriendlyByteBuf buf = PacketByteBufs.create();
				serializeDataOnServer(buf);
				
				for (ServerPlayer player : PlayerLookup.all(server)) {
					ServerPlayNetworking.send(player, getIdentifier(), buf);
				}
			}
		}
		
		@Override
		final void sendToClient(MinecraftServer server, ServerPlayer player) {
			if (prepareDataOnServer()) {
				FriendlyByteBuf buf = PacketByteBufs.create();
				serializeDataOnServer(buf);
				ServerPlayNetworking.send(player, getIdentifier(), buf);
			}
		}
		
		@Environment(EnvType.CLIENT)
		@Override
		final void sendToServer(Minecraft client) {
			BCLib.LOGGER.error("[Internal Error] The message '" + getIdentifier() + "' must originate from the server!");
		}
	}
}
