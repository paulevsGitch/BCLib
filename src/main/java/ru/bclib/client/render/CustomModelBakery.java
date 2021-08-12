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
	private static final Map<ResourceLocation, UnbakedModel> UNBAKED_CACHE = Maps.newConcurrentMap();
	private static final Set<ResourceLocation> LOADING_STACK = Sets.newConcurrentHashSet();
	private static boolean modelsLoaded;
	
	@Deprecated // Not working with Fabric model API
	public static boolean modelsLoaded() {
		return modelsLoaded;
	}
	
	@Deprecated // Not working with Fabric model API
	public static void setModelsLoaded(boolean modelsLoaded) {
		CustomModelBakery.modelsLoaded = modelsLoaded;
	}
	
	@Deprecated // Not working with Fabric model API
	public static void loadCustomModels(ResourceManager resourceManager, Map<ResourceLocation, UnbakedModel> unbakedCache, Set<ResourceLocation> loadingStack) {
		Registry.BLOCK.stream().parallel().filter(block -> block instanceof BlockModelProvider).forEach(block -> {
			ResourceLocation blockID = Registry.BLOCK.getKey(block);
			ResourceLocation storageID = new ResourceLocation(blockID.getNamespace(), "blockstates/" + blockID.getPath() + ".json");
			
			if (!resourceManager.hasResource(storageID)) {
				addBlockModel(blockID, block);
			}
			
			if (Registry.ITEM.get(blockID) != Items.AIR) {
				addItemModel(blockID, (ItemModelProvider) block);
			}
		});
		
		Registry.ITEM.stream().parallel().filter(item -> item instanceof ItemModelProvider).forEach(item -> {
			ResourceLocation registryID = Registry.ITEM.getKey(item);
			ResourceLocation storageID = new ResourceLocation(registryID.getNamespace(), "models/item/" + registryID.getPath() + ".json");
			if (!resourceManager.hasResource(storageID)) {
				addItemModel(registryID, (ItemModelProvider) item);
			}
		});
		
		unbakedCache.putAll(UNBAKED_CACHE);
		loadingStack.addAll(LOADING_STACK);
		UNBAKED_CACHE.clear();
		LOADING_STACK.clear();
		
		modelsLoaded = true;
	}
	
	@Deprecated // Not working with Fabric model API
	private static void addBlockModel(ResourceLocation blockID, Block block) {
		BlockModelProvider provider = (BlockModelProvider) block;
		ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
		BlockState defaultState = block.defaultBlockState();
		
		ResourceLocation defaultStateID = BlockModelShaper.stateToModelLocation(blockID, defaultState);
		UnbakedModel defaultModel = provider.getModelVariant(defaultStateID, defaultState, UNBAKED_CACHE);
		
		if (defaultModel instanceof MultiPart) {
			states.forEach(blockState -> {
				ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
				cacheAndQueueDependencies(stateID, defaultModel);
			});
		}
		else {
			states.forEach(blockState -> {
				ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
				UnbakedModel model = stateID.equals(defaultStateID) ? defaultModel : provider.getModelVariant(stateID, blockState, UNBAKED_CACHE);
				cacheAndQueueDependencies(stateID, model);
			});
		}
	}
	
	@Deprecated // Not working with Fabric model API
	private static void addItemModel(ResourceLocation itemID, ItemModelProvider provider) {
		ModelResourceLocation modelLocation = new ModelResourceLocation(itemID.getNamespace(), itemID.getPath(), "inventory");
		if (UNBAKED_CACHE.containsKey(modelLocation)) {
			return;
		}
		BlockModel model = provider.getItemModel(modelLocation);
		cacheAndQueueDependencies(modelLocation, model);
		UNBAKED_CACHE.put(modelLocation, model);
	}
	
	@Deprecated // Not working with Fabric model API
	private static void cacheAndQueueDependencies(ResourceLocation resourceLocation, UnbakedModel unbakedModel) {
		UNBAKED_CACHE.put(resourceLocation, unbakedModel);
		LOADING_STACK.addAll(unbakedModel.getDependencies());
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
