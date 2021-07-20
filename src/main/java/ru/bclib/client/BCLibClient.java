package ru.bclib.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.interfaces.PostInitable;
import ru.bclib.interfaces.RenderLayerProvider;
import ru.bclib.registry.BaseBlockEntityRenders;

public class BCLibClient implements ClientModInitializer/*, ModelResourceProvider*/ {
	@Override
	public void onInitializeClient() {
		ModIntegrationAPI.registerAll();
		BaseBlockEntityRenders.register();
		registerRenderLayers();
		Registry.BLOCK.forEach(block -> {
			if (block instanceof PostInitable) {
				((PostInitable) block).postInit();
			}
		});
		//ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> this);
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
	
	/*@Override
	public @Nullable UnbakedModel loadModelResource(ResourceLocation resourceId, ModelProviderContext context) throws ModelProviderException {
		if (!resourceId.getPath().startsWith("block")) {
			return null;
		}
		UnbakedModel model = context.loadModel(resourceId);
		if (model instanceof BlockModel) {
			System.out.println(resourceId);
			return new EmissiveModel((BlockModel) model);
		}
		return null;
	}*/
}
