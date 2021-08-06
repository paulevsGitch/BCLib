package ru.bclib.api.dataexchange.handler;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.dataexchange.handler.DataExchange.AutoSyncID;

import java.util.ArrayList;
import java.util.List;

public class RequestFiles extends DataHandler {
	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "request_files"), RequestFiles::new, false, false);
	
	protected List<AutoSyncID> files;
	private RequestFiles(){
		this(null);
	}
	
	public RequestFiles(List<AutoSyncID> files) {
		super(DESCRIPTOR.IDENTIFIER, false);
		this.files = files;
	}
	
	@Override
	protected void serializeData(FriendlyByteBuf buf) {
		buf.writeInt(files.size());
		
		for (AutoSyncID a : files){
			writeString(buf, a.getModID());
			writeString(buf, a.getUniqueID());
		}
	}
	
	@Override
	protected void deserializeFromIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean fromClient) {
		int size = buf.readInt();
		files = new ArrayList<>(size);
		
		BCLib.LOGGER.info("Client requested " + size + " Files:");
		for (int i=0; i<size; i++){
			String modID = readString(buf);
			String uID = readString(buf);
			AutoSyncID asid = new AutoSyncID(modID, uID);
			files.add(asid);
			BCLib.LOGGER.info("    - " + asid);
		}
		
		
	}
	
	@Override
	protected void runOnGameThread(Minecraft client, MinecraftServer server, boolean isClient) {
	
	}
}
