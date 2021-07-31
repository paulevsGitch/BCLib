package ru.bclib.mixin.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.interfaces.BlockModelProvider;
import ru.bclib.interfaces.ItemModelProvider;

import java.util.Map;
import java.util.Set;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
	@Final
	@Shadow
	private Map<ResourceLocation, UnbakedModel> unbakedCache;
	@Final
	@Shadow
	private Map<ResourceLocation, UnbakedModel> topLevelModels;
	@Final
	@Shadow
	private Set<ResourceLocation> loadingStack;
	
	@Inject(
		method = "<init>*",
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
			args = "ldc=missing_model",
			shift = Shift.BEFORE
		)
	)
	private void bclib_initCustomModels(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profiler, int mipmap, CallbackInfo info) {
		Map<ResourceLocation, UnbakedModel> cache = Maps.newConcurrentMap();
		Map<ResourceLocation, UnbakedModel> topLevel = Maps.newConcurrentMap();
		
		Registry.BLOCK.stream().filter(block -> block instanceof BlockModelProvider).parallel().forEach(block -> {
			ResourceLocation blockID = Registry.BLOCK.getKey(block);
			ResourceLocation storageID = new ResourceLocation(blockID.getNamespace(), "blockstates/" + blockID.getPath() + ".json");
			BlockModelProvider provider = (BlockModelProvider) block;
			
			if (!resourceManager.hasResource(storageID)) {
				BlockState defaultState = block.defaultBlockState();
				ResourceLocation defaultStateID = BlockModelShaper.stateToModelLocation(blockID, defaultState);
				
				UnbakedModel defaultModel = provider.getModelVariant(defaultStateID, defaultState, cache);
				cache.put(blockID, defaultModel);
				topLevel.put(blockID, defaultModel);
				
				ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
				if (states.size() == 1) {
					ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, block.defaultBlockState());
					cache.put(stateID, defaultModel);
					topLevel.put(stateID, defaultModel);
				}
				else {
					states.forEach(blockState -> {
						ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
						BlockModel model = provider.getBlockModel(stateID, blockState);
						cache.put(stateID, model != null ? model : defaultModel);
					});
				}
			}
			
			if (Registry.ITEM.get(blockID) != Items.AIR) {
				storageID = new ResourceLocation(blockID.getNamespace(), "models/item/" + blockID.getPath() + ".json");
				if (!resourceManager.hasResource(storageID)) {
					ResourceLocation itemID = new ModelResourceLocation(blockID.getNamespace(), blockID.getPath(), "inventory");
					BlockModel model = provider.getItemModel(itemID);
					cache.put(itemID, model);
					topLevel.put(itemID, model);
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
				cache.put(itemID, model);
				topLevel.put(itemID, model);
			}
		});
		
		cache.values().forEach(model -> {
			loadingStack.addAll(model.getDependencies());
		});
		
		topLevelModels.putAll(topLevel);
		unbakedCache.putAll(cache);
	}
}
