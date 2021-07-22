package ru.bclib.api;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import ru.bclib.blocks.BaseChestBlock;
import ru.bclib.blocks.BaseSignBlock;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.client.render.BaseChestBlockEntityRenderer;
import ru.bclib.client.render.BaseSignBlockEntityRenderer;
import ru.bclib.interfaces.PostInitable;
import ru.bclib.interfaces.RenderLayerProvider;

import java.util.List;
import java.util.function.Consumer;

public class PostInitAPI {
	private static List<Consumer<Void>> postInitFunctions = Lists.newArrayList();
	
	public static void register(Consumer<Void> function) {
		postInitFunctions.add(function);
	}
	
	public static void postInit(boolean isClient) {
		if (postInitFunctions == null) {
			return;
		}
		postInitFunctions.forEach(function -> function.accept(null));
		Registry.BLOCK.forEach(block -> {
			processBlockCommon(block);
			if (isClient) {
				processBlockClient(block);
			}
		});
		postInitFunctions = null;
	}
	
	@Environment(EnvType.CLIENT)
	private static void processBlockClient(Block block) {
		if (block instanceof RenderLayerProvider) {
			BCLRenderLayer layer = ((RenderLayerProvider) block).getRenderLayer();
			if (layer == BCLRenderLayer.CUTOUT) BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
			else if (layer == BCLRenderLayer.TRANSLUCENT) BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.translucent());
		}
		if (block instanceof BaseChestBlock) {
			BaseChestBlockEntityRenderer.registerRenderLayer(block);
		}
		else if (block instanceof BaseSignBlock) {
			BaseSignBlockEntityRenderer.registerRenderLayer(block);
		}
	}
	
	private static void processBlockCommon(Block block) {
		if (block instanceof PostInitable) {
			((PostInitable) block).postInit();
		}
	}
}
