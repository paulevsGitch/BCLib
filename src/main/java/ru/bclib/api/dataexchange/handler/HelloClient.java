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
import ru.bclib.api.dataexchange.FileHash;
import ru.bclib.api.datafixer.DataFixerAPI;
import ru.bclib.gui.screens.WarnBCLibVersionMismatch;
import ru.bclib.util.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;

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
		final List<String> mods = DataExchangeAPI.registeredMods();

		//write BCLibVersion (=protocol version)
		buf.writeInt(DataFixerAPI.getModVersion(getBCLibVersion()));

		//write Plugin Versions
		buf.writeInt(mods.size());
		for (String modID : mods) {
			writeString(buf, modID);
			buf.writeInt(DataFixerAPI.getModVersion(getModVersion(modID)));
		}

		//send config Data
		final List<DataExchange.AutoFileSyncEntry> autoSyncFiles = DataExchange.getInstance().autoSyncFiles;
		buf.writeInt(autoSyncFiles.size());
		for (DataExchange.AutoFileSyncEntry entry : autoSyncFiles) {
			System.out.println("Serializing " + entry.getFileHash());
			entry.serialize(buf);
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
			System.out.println("Deserializing ");
			DataExchange.AutoSyncTriple t = DataExchange.AutoFileSyncEntry.deserializeAndMatch(buf);
			autoSyncedFiles.add(t);
			System.out.println(t.first);
		}
	}
	
	@Override
	protected void runOnGameThread(Minecraft client, MinecraftServer server, boolean isClient) {
		String localBclibVersion = getBCLibVersion();
		BCLib.LOGGER.info("Received Hello from Server. (client="+localBclibVersion+", server="+bclibVersion+")");
		
		if (DataFixerAPI.getModVersion(localBclibVersion) == DataFixerAPI.getModVersion(bclibVersion)){
			showBCLibError(client);
			return;
		}
		
		for (Entry<String, String> e : modVersion.entrySet()){
			String ver = getModVersion(e.getKey());
			BCLib.LOGGER.info("    - " + e.getKey() + " (client="+ver+", server="+ver+")");
		}

		for (DataExchange.AutoSyncTriple e : autoSyncedFiles) {
			if (e.third == null) {
				BCLib.LOGGER.info("    - File " + e.first.modID + "." + e.first.uniqueID + ": Does not exist on client.");
			} else if (e.third.needTransfer.test(e.third.getFileHash(), e.first, e.second)) {
				BCLib.LOGGER.info("    - File " + e.first.modID + "." + e.first.uniqueID + ": Needs Transfer");
			}
		}
	}
	
	@Environment(EnvType.CLIENT)
	protected void showBCLibError(Minecraft client){
		BCLib.LOGGER.error("BCLib differs on client and server.");
		client.setScreen(new WarnBCLibVersionMismatch((download) -> {
			Minecraft.getInstance().setScreen((Screen)null);
			if (download){
				requestDownloads((hadErrors)->{
					client.stop();
				});
			}
		}));
	}
	
	private void requestDownloads(Consumer<Boolean> whenFinished){
		BCLib.LOGGER.warning("Starting download of BCLib");
	}
}
