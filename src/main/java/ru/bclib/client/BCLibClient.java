package ru.bclib.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.api.PostInitAPI;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.interfaces.PostInitable;
import ru.bclib.interfaces.RenderLayerProvider;
import ru.bclib.registry.BaseBlockEntityRenders;

public class BCLibClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModIntegrationAPI.registerAll();
		BaseBlockEntityRenders.register();
		registerRenderLayers();
		PostInitAPI.postInit();
	}
	
	private void registerRenderLayers() {
		RenderType cutout = RenderType.cutout();
		RenderType translucent = RenderType.translucent();
		Registry.BLOCK.forEach(block -> {
			if (block instanceof RenderLayerProvider) {
				BCLRenderLayer layer = ((RenderLayerProvider) block).getRenderLayer();
				if (layer == BCLRenderLayer.CUTOUT) BlockRenderLayerMap.INSTANCE.putBlock(block, cutout);
				else if (layer == BCLRenderLayer.TRANSLUCENT) BlockRenderLayerMap.INSTANCE.putBlock(block, translucent);
			}
		});
	}
}
