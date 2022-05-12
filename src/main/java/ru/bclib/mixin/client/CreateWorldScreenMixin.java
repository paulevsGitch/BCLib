package ru.bclib.mixin.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.core.Registry;
import net.minecraft.world.level.DataPackConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.api.biomes.BiomeAPI;

@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void bcl_init(Screen screen,
								 DataPackConfig dataPackConfig,
								 WorldGenSettingsComponent worldGenSettingsComponent,
								 CallbackInfo ci) {
		BiomeAPI.initRegistry(worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY));
	}
}
