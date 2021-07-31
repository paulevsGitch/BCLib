package ru.bclib.api.dataexchange;

import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;
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

import java.nio.charset.StandardCharsets;

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
		deserializeFromIncomingData(buf, responseSender, true);
		client.execute(() -> runOnGameThread(client, null, true));
	}
	
	void receiveFromClient(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender){
		deserializeFromIncomingData(buf, responseSender, false);
		server.execute(() -> runOnGameThread(null, server, false));
	}
	
	protected void deserializeFromIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean isClient){
	}
	
	protected void runOnGameThread(Minecraft client, MinecraftServer server, boolean isClient){
	
	}
	
	protected void serializeData(FriendlyByteBuf buf) {
	
	}
	
	void sendToClient(MinecraftServer server){
		FriendlyByteBuf buf = PacketByteBufs.create();
		serializeData(buf);
		
		for (ServerPlayer player : PlayerLookup.all(server)) {
			ServerPlayNetworking.send(player, this.identifier, buf);
		}
	}
	
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
	
	/**
	 * Write a String to a buffer (Convenience Method)
	 * @param buf The buffer to write to
	 * @param s The String you want to write
	 */
	public static void writeString(FriendlyByteBuf buf, String s){
		buf.writeByteArray(s.getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Read a string from a buffer (Convenience Method)
	 * @param buf Thea buffer to read from
	 * @return The received String
	 */
	public static String readString(FriendlyByteBuf buf){
		byte[] data = buf.readByteArray();
		return new String(data, StandardCharsets.UTF_8);
	}
}
