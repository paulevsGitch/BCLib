package ru.bclib.api.dataexchange.handler.autosync;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.dataexchange.handler.autosync.AutoSync.ClientConfig;
import ru.bclib.api.dataexchange.handler.autosync.AutoSync.Config;
import ru.bclib.gui.screens.ConfirmRestartScreen;
import ru.bclib.util.Pair;
import ru.bclib.util.PathUtil;
import ru.bclib.util.Triple;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SendFiles extends DataHandler.FromServer {
	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "send_files"), SendFiles::new, false, false);
	
	protected List<AutoFileSyncEntry> files;
	private String token = "";
	
	public SendFiles() {
		this(null, "");
	}
	
	public SendFiles(List<AutoFileSyncEntry> files, String token) {
		super(DESCRIPTOR.IDENTIFIER);
		this.files = files;
		this.token = token;
	}
	
	@Override
	protected boolean prepareDataOnServer() {
		if (!Config.isAllowingAutoSync()) {
			BCLib.LOGGER.info("Auto-Sync was disabled on the server.");
			return false;
		}
		
		return true;
	}
	
	@Override
	protected void serializeDataOnServer(FriendlyByteBuf buf) {
		List<AutoFileSyncEntry> existingFiles = files.stream()
													 .filter(e -> e.fileName.exists())
													 .collect(Collectors.toList());
		/*
		//this will try to send a file that was not registered or requested by the client
		existingFiles.add(new AutoFileSyncEntry("none", new File("D:\\MinecraftPlugins\\BetterNether\\run\\server.properties"),true,(a, b, content) -> {
			System.out.println("Got Content:" + content.length);
			return true;
		}));*/
		
		/*//this will try to send a folder-file that was not registered or requested by the client
		existingFiles.add(new AutoFileSyncEntry.ForDirectFileRequest(DataExchange.SYNC_FOLDER.folderID, new File("test.json"), DataExchange.SYNC_FOLDER.mapAbsolute("test.json").toFile()));*/
		
		/*//this will try to send a folder-file that was not registered or requested by the client and is outside the base-folder
		existingFiles.add(new AutoFileSyncEntry.ForDirectFileRequest(DataExchange.SYNC_FOLDER.folderID, new File("../breakout.json"), DataExchange.SYNC_FOLDER.mapAbsolute("../breakout.json").toFile()));*/
		
		
		writeString(buf, token);
		buf.writeInt(existingFiles.size());
		
		BCLib.LOGGER.info("Sending " + existingFiles.size() + " Files to Client:");
		for (AutoFileSyncEntry entry : existingFiles) {
			int length = entry.serializeContent(buf);
			BCLib.LOGGER.info("    - " + entry + " (" + PathUtil.humanReadableFileSize(length) + ")");
		}
	}
	
	private List<Pair<AutoFileSyncEntry, byte[]>> receivedFiles;
	
	@Environment(EnvType.CLIENT)
	@Override
	protected void deserializeIncomingDataOnClient(FriendlyByteBuf buf, PacketSender responseSender) {
		if (ClientConfig.isAcceptingFiles()) {
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
					BCLib.LOGGER.info("    - " + p.first + " (" + PathUtil.humanReadableFileSize(p.second.length) + ")");
				}
				else {
					BCLib.LOGGER.error("   - Failed to receive File " + p.third + ", possibly sent from a Mod that is not installed on the client.");
				}
			}
		}
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	protected void runOnClientGameThread(Minecraft client) {
		if (ClientConfig.isAcceptingFiles()) {
			BCLib.LOGGER.info("Writing Files:");
			
			//TODO: Reject files that were not in the last RequestFiles.
			for (Pair<AutoFileSyncEntry, byte[]> entry : receivedFiles) {
				final AutoFileSyncEntry e = entry.first;
				final byte[] data = entry.second;
				
				writeSyncedFile(e, data, e.fileName);
			}
			
			showConfirmRestart(client);
		}
	}
	
	
	@Environment(EnvType.CLIENT)
	public static void writeSyncedFile(AutoSyncID e, byte[] data, File fileName) {
		if (!PathUtil.MOD_BAK_FOLDER.toFile().exists()){
			PathUtil.MOD_BAK_FOLDER.toFile().mkdirs();
		}
		
		Path path = fileName!=null?fileName.toPath():null;
		Path removeAfter = null;
		if (e instanceof  AutoFileSyncEntry.ForModFileRequest){
			AutoFileSyncEntry.ForModFileRequest mase = (AutoFileSyncEntry.ForModFileRequest)e;
			removeAfter = path;
			int count = 0;
			String name = "bclib_synced_" + mase.modID + "_" + mase.version.replace(".", "_") + ".jar";
			do {
				if (path != null) {
					//move to the same directory as the existing Mod
					path = path.getParent()
							   .resolve(name);
				}
				else {
					//move to the default mode location
					path = PathUtil.MOD_FOLDER.resolve(name);
				}
				count++;
				name = "bclib_synced_" + mase.modID + "_" + mase.version.replace(".", "_") + "__" + String.format("%03d", count) + ".jar";
			} while (path.toFile().exists());
		}
		
		BCLib.LOGGER.info("    - Writing " + path + " (" + PathUtil.humanReadableFileSize(data.length) + ")");
		try {
			final File parentFile = path.getParent()
										.toFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
			Files.write(path, data);
			if (removeAfter != null){
				final String bakFileName = removeAfter.toFile().getName();
				String collisionFreeName = bakFileName;
				Path targetPath;
				int count = 0;
				do {
					targetPath = PathUtil.MOD_BAK_FOLDER.resolve(collisionFreeName);
					count++;
					collisionFreeName = String.format("%03d", count) + "_" + bakFileName;
				} while (targetPath.toFile().exists());
				
				BCLib.LOGGER.info("    - Moving " + removeAfter + " to " +targetPath);
				removeAfter.toFile().renameTo(targetPath.toFile());
			}
			AutoSync.didReceiveFile(e, fileName);
			
			
		}
		catch (IOException ioException) {
			BCLib.LOGGER.error("    --> Writing " + fileName + " failed: " + ioException);
		}
	}
	
	@Environment(EnvType.CLIENT)
	protected void showConfirmRestart(Minecraft client) {
		client.setScreen(new ConfirmRestartScreen(() -> {
			Minecraft.getInstance()
					 .setScreen((Screen) null);
			client.stop();
		}));
		
	}
}
