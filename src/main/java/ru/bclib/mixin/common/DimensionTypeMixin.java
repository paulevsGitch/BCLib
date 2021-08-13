package ru.bclib.mixin.common;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.world.generator.BCLibEndBiomeSource;
import ru.bclib.world.generator.BCLibNetherBiomeSource;

@Mixin(value = DimensionType.class, priority = 100)
public class DimensionTypeMixin {
	@Inject(method = "defaultNetherGenerator", at = @At("HEAD"), cancellable = true)
	private static void be_replaceNetherBiomeSource(Registry<Biome> biomeRegistry, Registry<NoiseGeneratorSettings> chunkGeneratorSettingsRegistry, long seed, CallbackInfoReturnable<ChunkGenerator> info) {
		info.setReturnValue(new NoiseBasedChunkGenerator(
			new BCLibNetherBiomeSource(biomeRegistry, seed),
			seed,
			() -> chunkGeneratorSettingsRegistry.getOrThrow(NoiseGeneratorSettings.NETHER)
		));
	}
	
	@Inject(method = "defaultEndGenerator", at = @At("HEAD"), cancellable = true)
	private static void be_replaceEndBiomeSource(Registry<Biome> biomeRegistry, Registry<NoiseGeneratorSettings> chunkGeneratorSettingsRegistry, long seed, CallbackInfoReturnable<ChunkGenerator> info) {
		info.setReturnValue(new NoiseBasedChunkGenerator(
			new BCLibEndBiomeSource(biomeRegistry, seed),
			seed,
			() -> chunkGeneratorSettingsRegistry.getOrThrow(NoiseGeneratorSettings.END)
		));
	}
}