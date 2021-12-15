package ru.bclib.mixin.common;

import net.fabricmc.fabric.impl.biome.NetherBiomeData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.world.biomes.FabricBiomesData;

@Mixin(value = NetherBiomeData.class, remap = false)
public class NetherBiomeDataMixin {
	@Inject(method = "addNetherBiome", at = @At(value = "HEAD"))
	private static void bclib_addNetherBiome(ResourceKey<Biome> biome, Climate.ParameterPoint spawnNoisePoint, CallbackInfo info) {
		FabricBiomesData.NETHER_BIOMES.add(biome);
	}
}
