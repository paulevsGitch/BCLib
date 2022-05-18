package org.betterx.bclib.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

import org.betterx.bclib.client.render.BaseChestBlockEntityRenderer;
import org.betterx.bclib.client.render.BaseSignBlockEntityRenderer;

@Environment(EnvType.CLIENT)
public class BaseBlockEntityRenders {
    public static void register() {
        BlockEntityRendererRegistry.register(BaseBlockEntities.CHEST, BaseChestBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(BaseBlockEntities.SIGN, BaseSignBlockEntityRenderer::new);
    }
}
