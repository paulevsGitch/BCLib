package ru.bclib.api.dataexchange.handler;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SendFiles extends DataHandler {
	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "send_files"), SendFiles::new, false, false);

	protected List<DataExchange.AutoFileSyncEntry> files;
	public SendFiles(){
		this(null);
	}
	public SendFiles(List<DataExchange.AutoFileSyncEntry> files) {
		super(DESCRIPTOR.IDENTIFIER, true);
		this.files = files;
	}
	
	@Override
	protected void serializeData(FriendlyByteBuf buf) {
		List<DataExchange.AutoFileSyncEntry> existingFiles = files.stream().filter(e -> e.fileName.exists()).collect(Collectors.toList());
		buf.writeInt(existingFiles.size());

		for (DataExchange.AutoFileSyncEntry entry : existingFiles) {
			entry.serializeContent(buf);
		}
	}

	private List<Pair<DataExchange.AutoFileSyncEntry, byte[]>> receivedFiles;
	@Override
	protected void deserializeFromIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean fromClient) {
		int size = buf.readInt();
		receivedFiles = new ArrayList<>(size);
		BCLib.LOGGER.info("Server sent " + size + " Files:");
		for (int i=0; i<size; i++){
			Pair<DataExchange.AutoFileSyncEntry, byte[]> p = DataExchange.AutoFileSyncEntry.deserializeContent(buf);
			if (p.first != null) {
				receivedFiles.add(p);
				BCLib.LOGGER.info("    - " + p.first + " (" + p.second.length + " Bytes)");
			} else {
				BCLib.LOGGER.error("    - Failed to receive File");
			}
		}
	}
	
	@Override
	protected void runOnGameThread(Minecraft client, MinecraftServer server, boolean isClient) {
	
	}
}
