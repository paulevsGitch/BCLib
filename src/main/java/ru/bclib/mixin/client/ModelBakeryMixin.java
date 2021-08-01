package ru.bclib.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.client.render.CustomModelBakery;

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
		require = 0,
		expect = 0,
		at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
			args = "ldc=missing_model",
			shift = Shift.BEFORE
		)
	)
	private void bclib_initCustomModels(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profiler, int mipmap, CallbackInfo info) {
		CustomModelBakery.loadCustomModels(resourceManager, unbakedCache, topLevelModels, loadingStack);
		CustomModelBakery.setModelsLoaded(true);
	}
	
	// If Injection above failed - with Optifine, for example
	@Inject(method = "loadModel(Lnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"))
	private void bclib_loadModelsIfNecessary(ResourceLocation resourceLocation, CallbackInfo model) {
		if (!CustomModelBakery.areModelsLoaded()) {
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			CustomModelBakery.loadCustomModels(resourceManager, unbakedCache, topLevelModels, loadingStack);
			CustomModelBakery.setModelsLoaded(true);
		}
	}
	
	@Inject(method = "<init>*", at = @At("TAIL"))
	private void bclib_findEmissiveModels(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profiler, int mipmap, CallbackInfo info) {
		if (ModIntegrationAPI.hasCanvas()) {
			CustomModelBakery.loadEmissiveModels(unbakedCache);
		}
		CustomModelBakery.setModelsLoaded(false);
	}
}
