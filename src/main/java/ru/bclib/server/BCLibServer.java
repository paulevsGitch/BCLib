package ru.bclib.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.core.Registry;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.interfaces.IPostInit;

public class BCLibServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		ModIntegrationAPI.registerAll();
		Registry.BLOCK.forEach(block -> {
			if (block instanceof IPostInit) {
				((IPostInit) block).postInit();
			}
		});
	}
}
