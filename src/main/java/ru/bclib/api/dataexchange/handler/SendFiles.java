package ru.bclib.api.dataexchange.handler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.config.Configs;
import ru.bclib.gui.screens.ConfirmRestartScreen;
import ru.bclib.util.Pair;
import ru.bclib.util.Triple;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SendFiles extends DataHandler {
	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "send_files"), SendFiles::new, false, false);

	protected List<AutoFileSyncEntry> files;
	private String token = "";
	public SendFiles(){
		this(null, "");
	}
	public SendFiles(List<AutoFileSyncEntry> files, String token) {
		super(DESCRIPTOR.IDENTIFIER, true);
		this.files = files;
		this.token = token;
	}

	public static boolean acceptFiles() {
		return Configs.CLIENT_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "acceptFiles", true);
	}
	
	@Override
	protected void serializeData(FriendlyByteBuf buf) {
		List<AutoFileSyncEntry> existingFiles = files.stream().filter(e -> e.fileName.exists()).collect(Collectors.toList());
		/*
		//this will try to send a file that was not registered or requested by the client
		existingFiles.add(new AutoFileSyncEntry("none", new File("D:\\MinecraftPlugins\\BetterNether\\run\\server.properties"),true,(a, b, content) -> {
			System.out.println("Got Content:" + content.length);
			return true;
		}));*/
		
		//this will try to send a folder-file that was not registered or requested by the client
		existingFiles.add(new AutoFileSyncEntry.ForDirectFileRequest(DataExchange.SYNC_FOLDER.folderID, new File("test.json"), DataExchange.SYNC_FOLDER.mapAbsolute("test.json").toFile()));
		
		//this will try to send a folder-file that was not registered or requested by the client and is outside the base-folder
		existingFiles.add(new AutoFileSyncEntry.ForDirectFileRequest(DataExchange.SYNC_FOLDER.folderID, new File("../breakout.json"), DataExchange.SYNC_FOLDER.mapAbsolute("../breakout.json").toFile()));
		
		
		writeString(buf, token);
		buf.writeInt(existingFiles.size());

		BCLib.LOGGER.info("Sending " + existingFiles.size() + " Files to Client:");
		for (AutoFileSyncEntry entry : existingFiles) {
			int length = entry.serializeContent(buf);
			BCLib.LOGGER.info("    - " + entry + " (" + length + " Bytes)");
		}
	}

	private List<Pair<AutoFileSyncEntry, byte[]>> receivedFiles;
	@Override
	protected void deserializeFromIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean fromClient) {
		if (acceptFiles()) {
			token = readString(buf);
			if (!token.equals(RequestFiles.currentToken)) {
				RequestFiles.newToken();
				BCLib.LOGGER.error("Unrequested File Transfer!");
				receivedFiles = new ArrayList<>(0);
				return;
			}
			RequestFiles.newToken();

			int size = buf.readInt();
			receivedFiles = new ArrayList<>(size);
			BCLib.LOGGER.info("Server sent " + size + " Files:");
			for (int i = 0; i < size; i++) {
				Triple<AutoFileSyncEntry, byte[], AutoSyncID> p = AutoFileSyncEntry.deserializeContent(buf);
				if (p.first != null) {
					receivedFiles.add(p);
					BCLib.LOGGER.info("    - " + p.first + " (" + p.second.length + " Bytes)");
				} else {
					BCLib.LOGGER.error("   - Failed to receive File " + p.third + ", possibly sent from a Mod that is not installed on the client.");
				}
			}
		}
	}
	
	@Override
	protected void runOnGameThread(Minecraft client, MinecraftServer server, boolean isClient) {
		if (acceptFiles()) {
			BCLib.LOGGER.info("Writing Files:");
			for (Pair<AutoFileSyncEntry, byte[]> entry : receivedFiles) {
				final AutoFileSyncEntry e = entry.first;
				final byte[] data = entry.second;
				
				writeSyncedFile(e, data, e.fileName);
			}

			showConfirmRestart(client);
		}
	}
	
	public static void writeSyncedFile(AutoSyncID e, byte[] data, File fileName) {
		Path path = fileName.toPath();
		BCLib.LOGGER.info("    - Writing " + path + " (" + data.length + " Bytes)");
		try {
			final File parentFile = path.getParent()
								  .toFile();
			if (!parentFile.exists()){
				parentFile.mkdirs();
			}
			Files.write(path, data);
			DataExchange.didReceiveFile(e, fileName);
		} catch (IOException ioException) {
			BCLib.LOGGER.error("    --> Writing " + fileName + " failed: " + ioException);
		}
	}
	
	@Environment(EnvType.CLIENT)
	protected void showConfirmRestart(Minecraft client){
		client.setScreen(new ConfirmRestartScreen(() -> {
			Minecraft.getInstance().setScreen((Screen)null);
			client.stop();
		}));

	}
}
