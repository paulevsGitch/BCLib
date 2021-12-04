package ru.bclib.mixin.common;

import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.api.biomes.BiomeAPI;

@Mixin(WorldPreset.class)
public class WorldPresetMixin {
	@Inject(method = "create", at = @At("HEAD"))
	private void create(RegistryAccess.RegistryHolder registryHolder, long l, boolean bl, boolean bl2, CallbackInfoReturnable<WorldGenSettings> info) {
		BiomeAPI.initRegistry(registryHolder.registryOrThrow(Registry.BIOME_REGISTRY));
	}
}
