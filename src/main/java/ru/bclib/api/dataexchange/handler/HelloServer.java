package ru.bclib.api.dataexchange.handler;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.datafixer.DataFixerAPI;

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
	
	@Override
	protected void deserializeFromIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean fromClient) {
		String bclibVersion = DataFixerAPI.getModVersion(buf.readInt());
		String localBclibVersion = getBCLibVersion();
		
		BCLib.LOGGER.info("Hello Server received from BCLib. (server="+localBclibVersion+", client="+bclibVersion+")");
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	protected void runOnClient(Minecraft client) {
	
	}
	
	@Override
	protected void serializeData(FriendlyByteBuf buf) {
		buf.writeInt(DataFixerAPI.getModVersion(getBCLibVersion()));
	}
}
