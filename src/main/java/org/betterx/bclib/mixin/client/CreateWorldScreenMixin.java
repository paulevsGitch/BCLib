package org.betterx.bclib.mixin.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import org.betterx.bclib.api.biomes.BiomeAPI;
import org.betterx.bclib.presets.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void bcl_init(Screen screen,
                          DataPackConfig dataPackConfig,
                          WorldGenSettingsComponent worldGenSettingsComponent,
                          CallbackInfo ci) {
        BiomeAPI.initRegistry(worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY));
    }

    @ModifyArg(method = "openFresh", index = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldGenSettingsComponent;<init>(Lnet/minecraft/client/gui/screens/worldselection/WorldCreationContext;Ljava/util/Optional;Ljava/util/OptionalLong;)V"))
    private static Optional<ResourceKey<WorldPreset>> bcl_NewDefault(Optional<ResourceKey<WorldPreset>> preset) {
        return Optional.of(WorldPresets.BCL_WORLD);
    }
}
