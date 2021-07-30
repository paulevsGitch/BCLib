package ru.bclib.api.dataexchange.handler;

import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import ru.bclib.BCLib;
import ru.bclib.api.WorldDataAPI;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.datafixer.DataFixerAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class HelloServer extends DataHandler {
	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "hello_server"), HelloServer::new, true);
	
	public HelloServer() {
		super(DESCRIPTOR.IDENTIFIER, false);
	}
	
	public static String getModVersion(String modID){
		Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer(modID);
		if (optional.isPresent()) {
			ModContainer modContainer = optional.get();
			return modContainer.getMetadata().getVersion().toString();
		}
		return "0.0.0";
	}
	
	protected static String getBCLibVersion(){
		return getModVersion(BCLib.MOD_ID);
	}
	
	String bclibVersion ="0.0.0";
	Map<String, String> modVersion = new HashMap<>();
	@Override
	protected void deserializeFromIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean fromClient) {
		bclibVersion = DataFixerAPI.getModVersion(buf.readInt());
		modVersion = new HashMap<>();
		
		int count = buf.readInt();
		for (int i=0; i< count; i++){
			String id = readString(buf);
			String version = DataFixerAPI.getModVersion(buf.readInt());
			modVersion.put(id, version);
		}
	}
	
	@Override
	protected void runOnServer(MinecraftServer server) {
		String localBclibVersion = getBCLibVersion();
		BCLib.LOGGER.info("Hello Server received from BCLib. (server="+localBclibVersion+", client="+bclibVersion+")");
		
		for (Entry<String, String> e : modVersion.entrySet()){
			String ver = getModVersion(e.getKey());
			BCLib.LOGGER.info("    - " + e.getKey() + " (server="+ver+", client="+ver+")");
		}
	}
	
	@Override
	protected void serializeData(FriendlyByteBuf buf) {
		final List<String> mods = DataExchangeAPI.registeredMods();
		buf.writeInt(DataFixerAPI.getModVersion(getBCLibVersion()));
		
		buf.writeInt(mods.size());
		for (String modID : mods) {
			writeString(buf, modID);
			buf.writeInt(DataFixerAPI.getModVersion(getModVersion(modID)));
		}
	}
}
