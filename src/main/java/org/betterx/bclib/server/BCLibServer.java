package org.betterx.bclib.server;

import net.fabricmc.api.DedicatedServerModInitializer;

import org.betterx.bclib.api.ModIntegrationAPI;
import org.betterx.bclib.api.PostInitAPI;
import org.betterx.bclib.api.dataexchange.DataExchangeAPI;

public class BCLibServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ModIntegrationAPI.registerAll();
        DataExchangeAPI.prepareServerside();

        PostInitAPI.postInit(false);
    }
}
