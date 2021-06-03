package ru.bclib.client;

import net.fabricmc.api.ClientModInitializer;
import ru.bclib.registry.BaseBlockEntityRenders;

public class BCLibClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BaseBlockEntityRenders.register();
	}
}
