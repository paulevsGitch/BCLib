package ru.bclib.mixin.client;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.BCLib;
import ru.bclib.interfaces.BlockModelProvider;
import ru.bclib.interfaces.ItemModelProvider;

import java.util.List;
import java.util.Map;

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
	
	private final Map<ResourceLocation, UnbakedModel> modelRegistry = Maps.newHashMap();
	
	@Inject(method = "loadModel", at = @At("HEAD"), cancellable = true)
	private void bclib_loadModels(ResourceLocation resourceLocation, CallbackInfo info) {
		if (resourceLocation instanceof ModelResourceLocation) {
			String modId = resourceLocation.getNamespace();
			String path = resourceLocation.getPath();
			ResourceLocation clearLoc = new ResourceLocation(modId, path);
			ModelResourceLocation modelId = (ModelResourceLocation) resourceLocation;
			if (modelId.getVariant().equals("inventory")) {
				if (bclib_loadItemModel(modId, path, clearLoc, modelId)) {
					info.cancel();
				}
			}
			else if (bclib_loadBlockModel(modId, path, clearLoc, modelId)) {
				info.cancel();
			}
		}
	}
	
	private void bclib_updateModelName(UnbakedModel model, ResourceLocation id) {
		if (model instanceof BlockModel) {
			((BlockModel) model).name = id.toString();
		}
	}
	
	private boolean bclib_loadItemModel(String modId, String path, ResourceLocation clearLoc, ResourceLocation modelId) {
		ResourceLocation itemLoc = new ResourceLocation(modId, "item/" + path);
		ResourceLocation itemModelLoc = new ResourceLocation(modId, "models/" + itemLoc.getPath() + ".json");
		
		if (resourceManager.hasResource(itemModelLoc)) {
			return false;
		}
		
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
		
		if (modelProvider == null) {
			return false;
		}
		
		UnbakedModel model = modelProvider.getItemModel(clearLoc, unbakedCache);
		if (model != null) {
			bclib_updateModelName(model, itemLoc);
			cacheAndQueueDependencies(modelId, model);
			unbakedCache.put(itemLoc, model);
		}
		else {
			BCLib.LOGGER.warning("Error loading model: {}", itemLoc);
		}
		
		return true;
	}
	
	private boolean bclib_loadBlockModel(String modId, String path, ResourceLocation clearLoc, ResourceLocation modelId) {
		ResourceLocation stateLoc = new ResourceLocation(modId, "blockstates/" + path + ".json");
		
		if (resourceManager.hasResource(stateLoc)) {
			return false;
		}
		
		Block block = Registry.BLOCK.get(clearLoc);
		if (!(block instanceof BlockModelProvider)) {
			return false;
		}
		
		BlockModelProvider modelProvider = (BlockModelProvider) block;
		
		modelRegistry.clear();
		modelProvider.registerModels(new ResourceLocation(modId, path), modelRegistry, unbakedCache);
		modelRegistry.forEach((id, model) -> {
			bclib_updateModelName(model, modelId);
			unbakedCache.put(id, model);
		});
		
		List<BlockState> possibleStates = block.getStateDefinition().getPossibleStates();
		possibleStates.forEach(state -> {
			ResourceLocation stateId = BlockModelShaper.stateToModelLocation(clearLoc, state);
			ResourceLocation modelID = modelProvider.getStateModel(stateId, state);
			UnbakedModel model = unbakedCache.get(modelID);
			if (model != null) {
				cacheAndQueueDependencies(stateId, model);
			}
			else {
				BCLib.LOGGER.warning("Error loading variant: {}", modelId);
			}
		});
		
		return true;
	}
}
