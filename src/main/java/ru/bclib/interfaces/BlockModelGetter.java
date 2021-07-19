package ru.bclib.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import ru.bclib.BCLib;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;

import java.util.Map;
import java.util.Optional;

import static net.minecraft.client.resources.model.ModelBakery.MISSING_MODEL_LOCATION;

public interface BlockModelGetter extends ItemModelGetter {
	@Environment(EnvType.CLIENT)
	default @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
		Optional<String> pattern = PatternsHelper.createBlockSimple(resourceLocation);
		return ModelsHelper.fromPattern(pattern);
	}
	
	@Environment(EnvType.CLIENT)
	default UnbakedModel getModelVariant(ResourceLocation stateId, BlockState blockState, Map<ResourceLocation, UnbakedModel> modelCache) {
		ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath());
		registerBlockModel(stateId, modelId, blockState, modelCache);
		return ModelsHelper.createBlockSimple(modelId);
	}
	
	@Environment(EnvType.CLIENT)
	default void registerBlockModel(ResourceLocation stateId, ResourceLocation modelId, BlockState blockState, Map<ResourceLocation, UnbakedModel> modelCache) {
		if (!modelCache.containsKey(modelId)) {
			BlockModel model = getBlockModel(stateId, blockState);
			if (model != null) {
				model.name = modelId.toString();
				modelCache.put(modelId, model);
			}
			else {
				BCLib.LOGGER.warning("Error loading model: {}", modelId);
				modelCache.put(modelId, modelCache.get(MISSING_MODEL_LOCATION));
			}
		}
	}
}
