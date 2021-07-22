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
	private static List<Consumer<Boolean>> postInitFunctions = Lists.newArrayList();
	
	/**
	 * Register a new function which will be called after all mods are initiated. Will be called on both client and server.
	 * @param function {@link Consumer} with {@code boolean} parameter ({@code true} for client, {@code false} for server).
	 */
	public static void register(Consumer<Boolean> function) {
		postInitFunctions.add(function);
	}
	
	/**
	 * Called in proper BCLib entry points, for internal usage only.
	 * @param isClient {@code boolean}, {@code true} for client, {@code false} for server.
	 */
	public static void postInit(boolean isClient) {
		if (postInitFunctions == null) {
			return;
		}
		postInitFunctions.forEach(function -> function.accept(isClient));
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
