package ru.bclib.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.BCLib;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.client.render.CustomModelBakery;
import ru.bclib.interfaces.BlockModelProvider;
import ru.bclib.interfaces.ItemModelProvider;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
	@Final
	@Shadow
	private ResourceManager resourceManager;
	@Final
	@Shadow
	private Map<ResourceLocation, UnbakedModel> unbakedCache;
	
	@Shadow
	protected abstract void cacheAndQueueDependencies(ResourceLocation resourceLocation, UnbakedModel unbakedModel);
	
	@Inject(method = "loadModel", at = @At("HEAD"), cancellable = true)
	private void bclib_loadCustomModels(ResourceLocation resourceLocation, CallbackInfo info) {
		if (resourceLocation instanceof ModelResourceLocation) {
			String modId = resourceLocation.getNamespace();
			String path = resourceLocation.getPath();
			ResourceLocation clearLoc = new ResourceLocation(modId, path);
			ModelResourceLocation modelId = (ModelResourceLocation) resourceLocation;
			if ("inventory".equals(modelId.getVariant())) {
				ResourceLocation itemLoc = new ResourceLocation(modId, "item/" + path);
				ResourceLocation itemModelLoc = new ResourceLocation(modId, "models/" + itemLoc.getPath() + ".json");
				if (!resourceManager.hasResource(itemModelLoc)) {
					Item item = Registry.ITEM.get(clearLoc);
					ItemModelProvider modelProvider = null;
					if (item instanceof ItemModelProvider) {
						modelProvider = (ItemModelProvider) item;
					}
					else if (item instanceof BlockItem) {
						Block block = Registry.BLOCK.get(clearLoc);
						if (block instanceof ItemModelProvider) {
							modelProvider = (ItemModelProvider) block;
						}
					}
					if (modelProvider != null) {
						BlockModel model = modelProvider.getItemModel(clearLoc);
						if (model != null) {
							model.name = itemLoc.toString();
							cacheAndQueueDependencies(modelId, model);
							unbakedCache.put(itemLoc, model);
						}
						else {
							BCLib.LOGGER.warning("Error loading model: {}", itemLoc);
						}
						info.cancel();
					}
				}
			}
			else {
				ResourceLocation stateLoc = new ResourceLocation(modId, "blockstates/" + path + ".json");
				if (!resourceManager.hasResource(stateLoc)) {
					Block block = Registry.BLOCK.get(clearLoc);
					if (block instanceof BlockModelProvider) {
						List<BlockState> possibleStates = block.getStateDefinition().getPossibleStates();
						Optional<BlockState> possibleState = possibleStates
							.stream()
							.filter(state -> modelId.equals(BlockModelShaper.stateToModelLocation(clearLoc, state)))
							.findFirst();
						if (possibleState.isPresent()) {
							UnbakedModel modelVariant = ((BlockModelProvider) block).getModelVariant(
								modelId,
								possibleState.get(),
								unbakedCache
							);
							if (modelVariant != null) {
								if (modelVariant instanceof MultiPart) {
									possibleStates.forEach(state -> {
										ResourceLocation stateId = BlockModelShaper.stateToModelLocation(
											clearLoc,
											state
										);
										cacheAndQueueDependencies(stateId, modelVariant);
									});
								}
								else {
									cacheAndQueueDependencies(modelId, modelVariant);
								}
							}
							else {
								BCLib.LOGGER.warning("Error loading variant: {}", modelId);
							}
							info.cancel();
						}
					}
				}
			}
		}
	}
	
	@Inject(method = "<init>*", at = @At("TAIL"))
	private void bclib_findEmissiveModels(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profiler, int mipmap, CallbackInfo info) {
		//CustomModelBakery.setModelsLoaded(false);
		if (ModIntegrationAPI.hasCanvas()) {
			CustomModelBakery.loadEmissiveModels(unbakedCache);
		}
	}
}
