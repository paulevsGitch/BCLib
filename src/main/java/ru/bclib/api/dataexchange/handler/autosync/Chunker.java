package ru.bclib.api.dataexchange.handler.autosync;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Chunker extends DataHandler.FromServer {
	public static class FileChunkReceiver {
		@NotNull
		public final UUID uuid;
		public final int chunkCount;
		@NotNull
		private final FriendlyByteBuf networkedBuf;
		
		private static List<FileChunkReceiver> active = new ArrayList<>(1);
		private static FileChunkReceiver newReceiver(@NotNull UUID uuid, int chunkCount){
			final FileChunkReceiver r = new FileChunkReceiver(uuid, chunkCount);
			active.add(r);
			return r;
		}
		
		private static FileChunkReceiver getOrCreate(@NotNull UUID uuid, int chunkCount){
			return active.stream().filter(r -> r.uuid.equals(uuid)).findFirst().orElse(newReceiver(uuid, chunkCount));
		}
		
		public static FileChunkReceiver get(@NotNull UUID uuid){
			return active.stream().filter(r -> r.uuid.equals(uuid)).findFirst().orElse(null);
		}
		
		private FileChunkReceiver(@NotNull UUID uuid, int chunkCount){
			this.uuid = uuid;
			this.chunkCount = chunkCount;
			networkedBuf = PacketByteBufs.create();
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof FileChunkReceiver)) return false;
			FileChunkReceiver that = (FileChunkReceiver) o;
			return uuid.equals(that.uuid);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(uuid);
		}
		
		public boolean testFinished(){
			return false;
		}
		
		public void processReceived(FriendlyByteBuf buf, int serialNo, int size){
		
		}
	}
	
	public static class FileChunkSender {
		private final FriendlyByteBuf networkedBuf;
		public final UUID uuid;
		public final int chunkCount;
		public final int size;
		
		public FileChunkSender(FriendlyByteBuf buf){
			networkedBuf = buf;
			
			size = buf.readableBytes();
			chunkCount = (int)Math.ceil((double)size / MAX_PAYLOAD_SIZE);
			uuid = UUID.randomUUID();
		}
		
		public void sendChunks(Collection<ServerPlayer> players){
			BCLib.LOGGER.info("Sending Request in " + chunkCount + " File-Chunks");
			for (int i=0; i<chunkCount; i++){
				Chunker c = new Chunker(i, uuid, networkedBuf, chunkCount);
				FriendlyByteBuf buf = PacketByteBufs.create();
				c.serializeDataOnServer(buf);
				
				for (ServerPlayer player : players){
					ServerPlayNetworking.send(player, DESCRIPTOR.IDENTIFIER, buf);
				}
			}
		}
	}
	
	private static final int HEADER_SIZE = 1 + 16 + 4 + 4; //header = version + UUID + serialNo + size
	public static final int MAX_PACKET_SIZE = 1024*1024;
	private static final int MAX_PAYLOAD_SIZE = MAX_PACKET_SIZE - HEADER_SIZE;
	
	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "chunker"), Chunker::new, false, false);
	
	private int serialNo;
	private UUID uuid;
	private int chunkCount;
	private FriendlyByteBuf networkedBuf;
	
	protected Chunker(int serialNo, UUID uuid, FriendlyByteBuf networkedBuf, int chunkCount) {
		super(DESCRIPTOR.IDENTIFIER);
		this.serialNo = serialNo;
		this.uuid = uuid;
		this.networkedBuf = networkedBuf;
		this.chunkCount = chunkCount;
	}
	
	protected Chunker(){
		super(DESCRIPTOR.IDENTIFIER);
	}
	
	
	@Override
	protected void serializeDataOnServer(FriendlyByteBuf buf) {
		buf.writeByte(0);
		buf.writeLong(uuid.getMostSignificantBits());
		buf.writeLong(uuid.getLeastSignificantBits());
		buf.writeInt(serialNo);
		
		if (serialNo == -1){
			//this is our header
			buf.writeInt(chunkCount);
			writeString(buf, SendFiles.DESCRIPTOR.IDENTIFIER.getNamespace());
			writeString(buf, SendFiles.DESCRIPTOR.IDENTIFIER.getPath());
		} else {
			final int size = Math.min(MAX_PAYLOAD_SIZE, networkedBuf.readableBytes());
			buf.writeInt(size);
			networkedBuf.readBytes(buf, size);
		}
	}
	
	private FileChunkReceiver receiver;
	
	@Override
	protected void deserializeIncomingDataOnClient(FriendlyByteBuf buf, PacketSender responseSender) {
		final int version = buf.readByte();
		uuid = new UUID(buf.readLong(), buf.readLong());
		serialNo = buf.readInt();
		
		if (serialNo == -1){
			chunkCount = buf.readInt();
			final String namespace = readString(buf);
			final String path = readString(buf);
			BCLib.LOGGER.info("Receiving " + chunkCount + " + File-Chunks for " + namespace +"."+path);
			
			receiver = FileChunkReceiver.getOrCreate(uuid, chunkCount);
		} else {
			receiver = FileChunkReceiver.get(uuid);
			if (receiver!=null) {
				final int size = buf.readInt();
				receiver.processReceived(buf, serialNo, size);
			} else {
				BCLib.LOGGER.error("Unknown File-Chunk Transfer for " + uuid);
			}
		}
	}
	
	@Override
	protected void runOnClientGameThread(Minecraft client) {
		if (receiver!=null){
			receiver.testFinished();
		}
	}
}
