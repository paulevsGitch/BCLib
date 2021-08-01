package ru.bclib.api.dataexchange.handler;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.DataHandler;
import ru.bclib.api.dataexchange.DataHandlerDescriptor;
import ru.bclib.api.datafixer.DataFixerAPI;

public class HelloServer extends DataHandler {
	public static DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(new ResourceLocation(BCLib.MOD_ID, "hello_server"), HelloServer::new, false, true);
	
	protected String bclibVersion ="0.0.0";
	public HelloServer() {
		super(DESCRIPTOR.IDENTIFIER, false);
	}
	
	@Override
	protected void deserializeFromIncomingData(FriendlyByteBuf buf, PacketSender responseSender, boolean fromClient) {
		bclibVersion = DataFixerAPI.getModVersion(buf.readInt());
	}
	
	@Override
	protected void runOnGameThread(Minecraft client, MinecraftServer server, boolean isClient) {
		String localBclibVersion = HelloClient.getBCLibVersion();
		BCLib.LOGGER.info("Received Hello from Client. (server="+localBclibVersion+", client="+bclibVersion+")");
		reply(new HelloClient(), server);
	}
	
	@Override
	protected void serializeData(FriendlyByteBuf buf) {
		buf.writeInt(DataFixerAPI.getModVersion(HelloClient.getBCLibVersion()));
	}
}
