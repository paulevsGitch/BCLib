package ru.bclib.client;

import net.fabricmc.api.ClientModInitializer;
import ru.bclib.blocks.BaseChestBlock;
import ru.bclib.blocks.BaseSignBlock;
import ru.bclib.client.render.BaseChestBlockEntityRenderer;
import ru.bclib.client.render.BaseSignBlockEntityRenderer;
import ru.bclib.registry.BaseBlockEntities;
import ru.bclib.registry.BaseBlockEntityRenders;

import java.util.Arrays;

public class BCLibClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BaseBlockEntityRenders.register();
		Arrays.stream(BaseBlockEntities.getChests()).forEach(chest -> {
			BaseChestBlockEntityRenderer.registerRenderLayer((BaseChestBlock) chest);
		});
		Arrays.stream(BaseBlockEntities.getSigns()).forEach(sign -> {
			BaseSignBlockEntityRenderer.registerRenderLayer((BaseSignBlock) sign);
		});
	}
}
