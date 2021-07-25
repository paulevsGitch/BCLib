package ru.bclib.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface ItemModelProvider {
	@Environment(EnvType.CLIENT)
	default UnbakedModel getItemModel(ResourceLocation resourceLocation, Map<ResourceLocation, UnbakedModel> unbakedCache) {
		return unbakedCache.get(resourceLocation);
	}
}