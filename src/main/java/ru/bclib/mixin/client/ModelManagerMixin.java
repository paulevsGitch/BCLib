package ru.bclib.mixin.client;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.client.BCLibClient;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
	@Inject(method = "prepare", at = @At("HEAD"))
	private void bclib_loadCustomModels(ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<ModelBakery> info) {
		BCLibClient.modelBakery.loadCustomModels(resourceManager);
	}
}
