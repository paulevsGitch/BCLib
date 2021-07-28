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
import org.jetbrains.annotations.NotNull;

public abstract class DataHandler {
	private final boolean originatesOnServer;
	@NotNull
	private final ResourceLocation identifier;
	
	protected DataHandler(ResourceLocation identifier, boolean originatesOnServer){
		this.originatesOnServer = originatesOnServer;
		this.identifier = identifier;
	}
	
	final public boolean getOriginatesOnServer(){
		return originatesOnServer;
	}
	
	final public ResourceLocation getIdentifier(){
		return identifier;
	}
	
	@Environment(EnvType.CLIENT)
	void receiveFromServer(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender){
		deserializeFromIncomingData(buf, responseSender, false);
		client.execute(() -> runOnClient(client));
	}
	
	@Environment(EnvType.SERVER)
	void receiveFromClient(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender){
		deserializeFromIncomingData(buf, responseSender, true);
		server.execute(() -> runOnServer(server));
	}
	
	protected void deserializeFromIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean fromClient){
	}
	
	@Environment(EnvType.CLIENT)
	protected void runOnClient(Minecraft client){
	
	}
	
	@Environment(EnvType.SERVER)
	protected void runOnServer(MinecraftServer server){
	
	}
	
	protected void serializeData(FriendlyByteBuf buf) {
	
	}
	
	@Environment(EnvType.SERVER)
	void sendToClient(MinecraftServer server){
		FriendlyByteBuf buf = PacketByteBufs.create();
		serializeData(buf);
		
		for (ServerPlayer player : PlayerLookup.all(server)) {
			ServerPlayNetworking.send(player, this.identifier, buf);
		}
	}
	
	@Environment(EnvType.SERVER)
	void sendToClient(MinecraftServer server, ServerPlayer player){
		FriendlyByteBuf buf = PacketByteBufs.create();
		serializeData(buf);
		ServerPlayNetworking.send(player, this.identifier, buf);
	}
	
	@Environment(EnvType.CLIENT)
	void sendToServer(Minecraft client){
		FriendlyByteBuf buf = PacketByteBufs.create();
		serializeData(buf);
		ClientPlayNetworking.send(identifier, buf);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DataHandler that = (DataHandler) o;
		return originatesOnServer == that.originatesOnServer && identifier.equals(that.identifier);
	}
	
	@Override
	public int hashCode() {
		int hash = identifier.hashCode();
		if (originatesOnServer) hash |= 0x80000000;
		else hash &=0x7FFFFFFF;
		
		return hash;
	}
	
	@Override
	public String toString() {
		return "DataHandler{" + "originatesOnServer=" + originatesOnServer + ", identifier=" + identifier + '}';
	}
}
