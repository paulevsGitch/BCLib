package ru.bclib.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import ru.bclib.client.render.BaseChestBlockEntityRenderer;
import ru.bclib.client.render.BaseSignBlockEntityRenderer;

@Environment(EnvType.CLIENT)
public class BaseBlockEntityRenders {
	public static void register() {
		BlockEntityRendererRegistry.INSTANCE.register(BaseBlockEntities.CHEST, BaseChestBlockEntityRenderer::new);
		BlockEntityRendererRegistry.INSTANCE.register(BaseBlockEntities.SIGN, BaseSignBlockEntityRenderer::new);
	}
}
