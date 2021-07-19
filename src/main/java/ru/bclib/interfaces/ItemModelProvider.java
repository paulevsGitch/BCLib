package ru.bclib.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.client.models.ModelsHelper;

public interface ItemModelProvider {
	@Environment(EnvType.CLIENT)
	default BlockModel getItemModel(ResourceLocation resourceLocation) {
		return ModelsHelper.createItemModel(resourceLocation);
	}
}