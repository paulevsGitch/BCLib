package ru.bclib.mixin.client;

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

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
	@Final
	@Shadow
	private Map<ResourceLocation, UnbakedModel> unbakedCache;
	@Final
	@Shadow
	private Map<ResourceLocation, UnbakedModel> topLevelModels;
	
	//private Map<ResourceLocation, UnbakedModel> cache = Maps.newHashMap();
	//private Map<ResourceLocation, UnbakedModel> topLevel = Maps.newHashMap();
	
	@Inject(
		method = "<init>*",
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
			args = "ldc=static_definitions",
			shift = Shift.BEFORE
		)
	)
	private void bclib_initCustomModels(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profiler, int mipmap, CallbackInfo info) {
		Map<ResourceLocation, UnbakedModel> cache = Maps.newHashMap();
		Map<ResourceLocation, UnbakedModel> topLevel = Maps.newHashMap();
		
		Registry.BLOCK.forEach(block -> {
			if (block instanceof BlockModelProvider) {
				ResourceLocation blockID = Registry.BLOCK.getKey(block);
				ResourceLocation storageID = new ResourceLocation(blockID.getNamespace(), "blockstates/" + blockID.getPath() + ".json");
				BlockModelProvider provider = (BlockModelProvider) block;
				
				if (!resourceManager.hasResource(storageID)) {
					BlockState defaultState = block.defaultBlockState();
					ResourceLocation defaultStateID = BlockModelShaper.stateToModelLocation(blockID, defaultState);
					
					UnbakedModel defaultModel = provider.getModelVariant(defaultStateID, defaultState, cache);
					cache.put(blockID, defaultModel);
					topLevel.put(blockID, defaultModel);
					
					block.getStateDefinition().getPossibleStates().forEach(blockState -> {
						ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
						BlockModel model = provider.getBlockModel(stateID, blockState);
						cache.put(stateID, model != null ? model : defaultModel);
					});
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
			}
		});
		
		Registry.ITEM.forEach(item -> {
			if (item instanceof ItemModelProvider) {
				ResourceLocation registryID = Registry.ITEM.getKey(item);
				ResourceLocation storageID = new ResourceLocation(registryID.getNamespace(), "models/item/" + registryID.getPath() + ".json");
				if (!resourceManager.hasResource(storageID)) {
					ResourceLocation itemID = new ModelResourceLocation(registryID.getNamespace(), registryID.getPath(), "inventory");
					ItemModelProvider provider = (ItemModelProvider) item;
					BlockModel model = provider.getItemModel(registryID);
					cache.put(itemID, model);
					topLevel.put(itemID, model);
				}
			}
		});
		
		topLevelModels.putAll(topLevel);
		unbakedCache.putAll(cache);
	}
}
