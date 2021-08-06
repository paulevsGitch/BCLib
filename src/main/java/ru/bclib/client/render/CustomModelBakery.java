package ru.bclib.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.interfaces.BlockModelProvider;
import ru.bclib.interfaces.ItemModelProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CustomModelBakery {
	private static boolean modelsLoaded;
	
	public static boolean areModelsLoaded() {
		return modelsLoaded;
	}
	
	public static void setModelsLoaded(boolean modelsLoaded) {
		CustomModelBakery.modelsLoaded = modelsLoaded;
	}
	
	public static void loadCustomModels(ResourceManager resourceManager, Map<ResourceLocation, UnbakedModel> unbakedCache, Map<ResourceLocation, UnbakedModel> topLevelModels, Set<ResourceLocation> loadingStack) {
		Map<ResourceLocation, UnbakedModel> cache = Maps.newConcurrentMap();
		Map<ResourceLocation, UnbakedModel> topLevel = Maps.newConcurrentMap();
		
		Registry.BLOCK.stream().filter(block -> block instanceof BlockModelProvider).parallel().forEach(block -> {
			ResourceLocation blockID = Registry.BLOCK.getKey(block);
			ResourceLocation storageID = new ResourceLocation(blockID.getNamespace(), "blockstates/" + blockID.getPath() + ".json");
			BlockModelProvider provider = (BlockModelProvider) block;
			
			if (!resourceManager.hasResource(storageID)) {
				ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
				BlockState defaultState = block.defaultBlockState();
				
				ResourceLocation defaultStateID = BlockModelShaper.stateToModelLocation(blockID, defaultState);
				UnbakedModel defaultModel = provider.getModelVariant(defaultStateID, defaultState, cache);
				
				if (defaultModel instanceof MultiPart) {
					states.forEach(blockState -> {
						ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
						topLevel.put(stateID, defaultModel);
						cache.put(stateID, defaultModel);
					});
				}
				else {
					states.forEach(blockState -> {
						ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
						UnbakedModel model = stateID.equals(defaultStateID) ? defaultModel : provider.getModelVariant(stateID, blockState, cache);
						topLevel.put(stateID, model);
						cache.put(stateID, model);
					});
				}
			}
			
			if (Registry.ITEM.get(blockID) != Items.AIR) {
				storageID = new ResourceLocation(blockID.getNamespace(), "models/item/" + blockID.getPath() + ".json");
				if (!resourceManager.hasResource(storageID)) {
					ResourceLocation itemID = new ModelResourceLocation(blockID.getNamespace(), blockID.getPath(), "inventory");
					BlockModel model = provider.getItemModel(itemID);
					topLevel.put(itemID, model);
					cache.put(itemID, model);
				}
			}
		});
		
		Registry.ITEM.stream().filter(item -> item instanceof ItemModelProvider).parallel().forEach(item -> {
			ResourceLocation registryID = Registry.ITEM.getKey(item);
			ResourceLocation storageID = new ResourceLocation(registryID.getNamespace(), "models/item/" + registryID.getPath() + ".json");
			if (!resourceManager.hasResource(storageID)) {
				ResourceLocation itemID = new ModelResourceLocation(registryID.getNamespace(), registryID.getPath(), "inventory");
				ItemModelProvider provider = (ItemModelProvider) item;
				BlockModel model = provider.getItemModel(registryID);
				topLevel.put(itemID, model);
				cache.put(itemID, model);
			}
		});
		
		cache.values().forEach(model -> {
			loadingStack.addAll(model.getDependencies());
		});
		
		topLevelModels.putAll(topLevel);
		unbakedCache.putAll(cache);
	}
	
	public static void loadEmissiveModels(Map<ResourceLocation, UnbakedModel> unbakedCache) {
		if (!ModIntegrationAPI.hasCanvas()) {
			return;
		}
		
		Map<ResourceLocation, UnbakedModel> cacheCopy = new HashMap<>(unbakedCache);
		Set<Pair<String, String>> strings = Sets.newConcurrentHashSet();
		Registry.BLOCK.keySet().forEach(blockID -> {
			Block block = Registry.BLOCK.get(blockID);
			ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
			boolean addBlock = false;
			
			for (BlockState state: states) {
				ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, state);
				UnbakedModel model = cacheCopy.get(stateID);
				if (model == null) {
					continue;
				}
				Collection<Material> materials = model.getMaterials(cacheCopy::get, strings);
				if (materials == null) {
					continue;
				}
				for (Material material: materials) {
					if (EmissiveTextureInfo.isEmissiveTexture(material.texture())) {
						addBlock = true;
						break;
					}
				}
				if (addBlock) {
					break;
				}
			}
			
			if (addBlock) {
				EmissiveTextureInfo.addBlock(blockID);
			}
		});
	}
}
