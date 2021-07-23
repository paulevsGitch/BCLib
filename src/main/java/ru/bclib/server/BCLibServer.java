package ru.bclib.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.api.PostInitAPI;

public class BCLibServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		ModIntegrationAPI.registerAll();
		PostInitAPI.postInit(false);
	}
}
