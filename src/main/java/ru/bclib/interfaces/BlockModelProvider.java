package ru.bclib.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import ru.bclib.client.models.ModelsHelper;

import java.util.Map;

public interface BlockModelProvider {
	@Environment(EnvType.CLIENT)
	default UnbakedModel getModelVariant(ResourceLocation stateId, BlockState blockState, Map<ResourceLocation, UnbakedModel> modelCache) {
		ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath());
		return ModelsHelper.createBlockSimple(modelId);
	}
}
