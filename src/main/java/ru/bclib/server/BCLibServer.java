package ru.bclib.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.api.PostInitAPI;
import ru.bclib.api.dataexchange.DataExchangeAPI;

public class BCLibServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		ModIntegrationAPI.registerAll();
		DataExchangeAPI.prepareServerside();
		
		PostInitAPI.postInit(false);
	}
}
