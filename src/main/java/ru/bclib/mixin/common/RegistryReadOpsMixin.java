package ru.bclib.mixin.common;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.api.biomes.BiomeAPI;

/**
 * Fabrics BiomeModifications API is called at this point. We have to ensure that BCLibs Modification API
 * runs before Fabric, so we need to hook into the same class.
 */
@Mixin(RegistryReadOps.class)
public class RegistryReadOpsMixin {
	@Inject(method = "createAndLoad(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/resources/RegistryResourceAccess;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/resources/RegistryReadOps;", at = @At("RETURN"))
	private static <T> void foo(DynamicOps<T> dynamicOps, RegistryResourceAccess registryResourceAccess, RegistryAccess registryAccess, CallbackInfoReturnable<RegistryReadOps<T>> cir){
		BiomeAPI.initRegistry(registryAccess);
		BiomeAPI.applyModifications(registryAccess);
	}
}
