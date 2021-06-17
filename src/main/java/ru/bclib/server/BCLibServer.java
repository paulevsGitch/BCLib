package ru.bclib.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import ru.bclib.api.ModIntegrationAPI;

public class BCLibServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		ModIntegrationAPI.registerAll();
	}
}
