package ru.bclib.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public interface BlockModelProvider extends ItemModelProvider {
	@Environment(EnvType.CLIENT)
	default void registerModels(ResourceLocation blockID, Map<ResourceLocation, UnbakedModel> modelRegistry, Map<ResourceLocation, UnbakedModel> unbakedCache) {}
	
	@Environment(EnvType.CLIENT)
	default ResourceLocation getStateModel(ResourceLocation stateId, BlockState blockState) {
		return new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath());
	}
}
