package ru.bclib.api.dataexchange.handler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.dataexchange.handler.DataExchange.AutoSyncID;
import ru.bclib.api.datafixer.DataFixerAPI;
import ru.bclib.config.Configs;
import ru.bclib.gui.screens.SyncFilesScreen;
import ru.bclib.gui.screens.WarnBCLibVersionMismatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Sent from the Server to the Client.
 * <p>
 * For Details refer to {@link HelloServer}
 */
public class HelloClient extends DataHandler {
	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "hello_client"), HelloClient::new, false, false);
	
	public HelloClient() {
		super(DESCRIPTOR.IDENTIFIER, true);
	}
	
	public static String getModVersion(String modID){
		Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer(modID);
		if (optional.isPresent()) {
			ModContainer modContainer = optional.get();
			return modContainer.getMetadata().getVersion().toString();
		}
		return "0.0.0";
	}
	
	static String getBCLibVersion(){
		return getModVersion(BCLib.MOD_ID);
	}

	@Override
	protected void serializeData(FriendlyByteBuf buf) {
		final String vbclib = getBCLibVersion();
		BCLib.LOGGER.info("Sending Hello to Client. (server="+vbclib+")");
		final List<String> mods = DataExchangeAPI.registeredMods();

		//write BCLibVersion (=protocol version)
		buf.writeInt(DataFixerAPI.getModVersion(vbclib));

		if (Configs.MAIN_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "offerMods", true)) {
			//write Plugin Versions
			buf.writeInt(mods.size());
			for (String modID : mods) {
				writeString(buf, modID);
				final String ver = getModVersion(modID);
				buf.writeInt(DataFixerAPI.getModVersion(ver));
				BCLib.LOGGER.info("    - Listing Mod " + modID + " v" + ver);
			}
		} else {
			buf.writeInt(0);
		}

		if (Configs.MAIN_CONFIG.getBoolean(Configs.MAIN_SYNC_CATEGORY, "offerConfigs", true)) {
			//do only include files that exist on the server
			final List<AutoFileSyncEntry> existingAutoSyncFiles = DataExchange
					.getInstance()
					.autoSyncFiles
					.stream()
					.filter(e -> e.fileName.exists())
					.collect(Collectors.toList());

			//send config Data
			buf.writeInt(existingAutoSyncFiles.size());
			for (AutoFileSyncEntry entry : existingAutoSyncFiles) {
				entry.serialize(buf);
				BCLib.LOGGER.info("    - Offering File " + entry);
			}
		} else {
			buf.writeInt(0);
		}
	}
	
	String bclibVersion ="0.0.0";
	Map<String, String> modVersion = new HashMap<>();
	List<DataExchange.AutoSyncTriple> autoSyncedFiles = null;
	@Override
	protected void deserializeFromIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean fromClient) {
		//read BCLibVersion (=protocol version)
		bclibVersion = DataFixerAPI.getModVersion(buf.readInt());

		//read Plugin Versions
		modVersion = new HashMap<>();
		int count = buf.readInt();
		for (int i=0; i< count; i++) {
			String id = readString(buf);
			String version = DataFixerAPI.getModVersion(buf.readInt());
			modVersion.put(id, version);
		}

		//read config Data
		count = buf.readInt();
		autoSyncedFiles = new ArrayList<>(count);
		for (int i=0; i< count; i++) {
			//System.out.println("Deserializing ");
			DataExchange.AutoSyncTriple t = AutoFileSyncEntry.deserializeAndMatch(buf);
			autoSyncedFiles.add(t);
			//System.out.println(t.first);
		}
	}
	
	@Override
	protected void runOnGameThread(Minecraft client, MinecraftServer server, boolean isClient) {
		String localBclibVersion = getBCLibVersion();
		BCLib.LOGGER.info("Received Hello from Server. (client="+localBclibVersion+", server="+bclibVersion+")");
		
		// if (DataFixerAPI.getModVersion(localBclibVersion) != DataFixerAPI.getModVersion(bclibVersion)){
		// 	showBCLibError(client);
		// 	return;
		// }
		
		List<AutoSyncID> filesToRequest = new ArrayList<>(4);
		
		for (Entry<String, String> e : modVersion.entrySet()){
			String ver = getModVersion(e.getKey());
			BCLib.LOGGER.info("    - " + e.getKey() + " (client="+ver+", server="+ver+")");
		}

		BCLib.LOGGER.info("Server offered Files to sync.");
		for (DataExchange.AutoSyncTriple e : autoSyncedFiles) {
			boolean willRequest = false;
			if (e.third == null) {
				filesToRequest.add(new AutoSyncID(e.first.modID, e.first.uniqueID));
				willRequest = true;
				BCLib.LOGGER.info("    - File " + e + ": Does not exist on client.");
			} else if (e.third.needTransfer.test(e.third.getFileHash(), e.first, e.second)) {
				willRequest = true;
				filesToRequest.add(new AutoSyncID(e.first.modID, e.first.uniqueID));
				BCLib.LOGGER.info("    - File " + e + ": Needs Transfer");
			}
			
			BCLib.LOGGER.info("    - " + e + ": " + (willRequest ? " (requesting)":""));
		}

		if (filesToRequest.size()>0 && SendFiles.acceptFiles()) {
			showDonwloadConfigs(client, filesToRequest);
			return;
		}
	}
	
	@Environment(EnvType.CLIENT)
	protected void showBCLibError(Minecraft client){
		BCLib.LOGGER.error("BCLib differs on client and server.");
		client.setScreen(new WarnBCLibVersionMismatch((download) -> {
			Minecraft.getInstance().setScreen((Screen)null);
			if (download){
				requestBCLibDownload((hadErrors)->{
					client.stop();
				});
			}
		}));
	}
	
	@Environment(EnvType.CLIENT)
	protected void showDonwloadConfigs(Minecraft client, List<AutoSyncID> files){
		client.setScreen(new SyncFilesScreen((download) -> {
			Minecraft.getInstance().setScreen((Screen)null);
			if (download){
				requestFileDownloads(files);
			}
		}));

	}
	
	private void requestBCLibDownload(Consumer<Boolean> whenFinished){
		BCLib.LOGGER.warning("Starting download of BCLib");
		whenFinished.accept(true);
	}
	
	private void requestFileDownloads(List<AutoSyncID> files){
		BCLib.LOGGER.info("Starting download of Files:" + files.size());
		DataExchangeAPI.send(new RequestFiles(files));
	}
}
