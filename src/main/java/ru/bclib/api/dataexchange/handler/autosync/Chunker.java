package ru.bclib.api.dataexchange.handler.autosync;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.BaseDataHandler;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.dataexchange.handler.DataExchange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Chunker extends DataHandler.FromServer {
	public static class PacketChunkReceiver {
		@NotNull
		public final UUID uuid;
		public final int chunkCount;
		@NotNull
		private final FriendlyByteBuf networkedBuf;
		@Nullable
		private final DataHandlerDescriptor descriptor;
		
		private static List<PacketChunkReceiver> active = new ArrayList<>(1);
		private static PacketChunkReceiver newReceiver(@NotNull UUID uuid, int chunkCount, ResourceLocation origin){
			DataHandlerDescriptor desc = DataExchange.getDescriptor(origin);
			final PacketChunkReceiver r = new PacketChunkReceiver(uuid, chunkCount, desc);
			active.add(r);
			return r;
		}
		
		private static PacketChunkReceiver getOrCreate(@NotNull UUID uuid, int chunkCount, ResourceLocation origin){
			return active.stream().filter(r -> r.uuid.equals(uuid)).findFirst().orElse(newReceiver(uuid, chunkCount, origin));
		}
		
		public static PacketChunkReceiver get(@NotNull UUID uuid){
			return active.stream().filter(r -> r.uuid.equals(uuid)).findFirst().orElse(null);
		}
		
		private PacketChunkReceiver(@NotNull UUID uuid, int chunkCount, @Nullable DataHandlerDescriptor descriptor){
			this.uuid = uuid;
			this.chunkCount = chunkCount;
			networkedBuf = PacketByteBufs.create();
			this.descriptor = descriptor;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof PacketChunkReceiver)) return false;
			PacketChunkReceiver that = (PacketChunkReceiver) o;
			return uuid.equals(that.uuid);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(uuid);
		}
		
		public boolean testFinished(){
			if (incomingBuffer == null){
				return true;
			} if (lastReadSerial>=chunkCount-1){
				onFinish();
				return true;
			}
			return false;
		}
		
		protected void addBuffer(FriendlyByteBuf input){
			final int size = input.readableBytes();
			final int cap = networkedBuf.capacity()-networkedBuf.writerIndex();
			
			if (cap < size){
				networkedBuf.capacity(networkedBuf.writerIndex() + size);
			}
			input.readBytes(networkedBuf, size);
			input.clear();
		}
		
		protected void onFinish(){
			incomingBuffer.clear();
			incomingBuffer = null;
			
			final BaseDataHandler baseHandler = descriptor.INSTANCE.get();
			if (baseHandler instanceof DataHandler.FromServer handler){
				handler.receiveFromMemory(networkedBuf);
			}
		}
		
		Map<Integer, FriendlyByteBuf> incomingBuffer = new HashMap<>();
		int lastReadSerial = -1;
		public void processReceived(FriendlyByteBuf buf, int serialNo, int size){
			if (lastReadSerial == serialNo-1){
				addBuffer(buf);
				lastReadSerial = serialNo;
			} else {
				//not sure if order is guaranteed by the underlying system!
				boolean haveAll = true;
				for (int nr = lastReadSerial+1; nr < serialNo-1; nr++){
					if (incomingBuffer.get(nr) == null){
						haveAll = false;
						break;
					}
				}
				
				if (haveAll){
					for (int nr = lastReadSerial+1; nr < serialNo-1; nr++){
						addBuffer(incomingBuffer.get(nr));
						incomingBuffer.put(nr, null);
					}
					addBuffer(buf);
					lastReadSerial = serialNo;
				} else {
					incomingBuffer.put(serialNo, buf);
				}
			}
		}
	}
	
	public static class PacketChunkSender {
		private final FriendlyByteBuf networkedBuf;
		public final UUID uuid;
		public final int chunkCount;
		public final int size;
		public final ResourceLocation origin;
		
		public PacketChunkSender(FriendlyByteBuf buf, ResourceLocation origin){
			networkedBuf = buf;
			
			size = buf.readableBytes();
			chunkCount = (int)Math.ceil((double)size / MAX_PAYLOAD_SIZE);
			uuid = UUID.randomUUID();
			this.origin = origin;
		}
		
		public void sendChunks(Collection<ServerPlayer> players){
			BCLib.LOGGER.info("Sending Request in " + chunkCount + " Packet-Chunks");
			for (int i=-1; i<chunkCount; i++){
				Chunker c = new Chunker(i, uuid, networkedBuf, chunkCount, origin);
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
	private ResourceLocation origin;
	
	protected Chunker(int serialNo, UUID uuid, FriendlyByteBuf networkedBuf, int chunkCount, ResourceLocation origin) {
		super(DESCRIPTOR.IDENTIFIER);
		this.serialNo = serialNo;
		this.uuid = uuid;
		this.networkedBuf = networkedBuf;
		this.chunkCount = chunkCount;
		this.origin = origin;
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
			writeString(buf, origin.getNamespace());
			writeString(buf, origin.getPath());
		} else {
			buf.capacity(MAX_PACKET_SIZE);
			final int size = Math.min(MAX_PAYLOAD_SIZE, networkedBuf.readableBytes());
			buf.writeInt(size);
			networkedBuf.readBytes(buf, size);
		}
	}
	
	private PacketChunkReceiver receiver;
	
	@Override
	protected void deserializeIncomingDataOnClient(FriendlyByteBuf buf, PacketSender responseSender) {
		final int version = buf.readByte();
		uuid = new UUID(buf.readLong(), buf.readLong());
		serialNo = buf.readInt();
		
		if (serialNo == -1){
			chunkCount = buf.readInt();
			final String namespace = readString(buf);
			final String path = readString(buf);
			ResourceLocation ident = new ResourceLocation(namespace, path);
			BCLib.LOGGER.info("Receiving " + chunkCount + " + Packet-Chunks for " + ident);
			
			receiver = PacketChunkReceiver.getOrCreate(uuid, chunkCount, ident);
		} else {
			receiver = PacketChunkReceiver.get(uuid);
			if (receiver!=null) {
				final int size = buf.readInt();
				receiver.processReceived(buf, serialNo, size);
			} else {
				BCLib.LOGGER.error("Unknown Packet-Chunk Transfer for " + uuid);
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
