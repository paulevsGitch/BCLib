package ru.bclib.client;

import net.fabricmc.api.ClientModInitializer;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.api.PostInitAPI;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.registry.BaseBlockEntityRenders;

public class BCLibClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModIntegrationAPI.registerAll();
		BaseBlockEntityRenders.register();
		DataExchangeAPI.prepareClientside();
		PostInitAPI.postInit(true);
	}
}
