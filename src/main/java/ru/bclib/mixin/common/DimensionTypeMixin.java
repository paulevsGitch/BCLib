package ru.bclib.mixin.common;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.world.generator.BCLibEndBiomeSource;
import ru.bclib.world.generator.BCLibNetherBiomeSource;
import ru.bclib.world.generator.GeneratorOptions;

@Mixin(value = DimensionType.class, priority = 100)
public class DimensionTypeMixin {
	@Inject(method="defaultDimensions(Lnet/minecraft/core/RegistryAccess;JZ)Lnet/minecraft/core/MappedRegistry;", at=@At("HEAD"), cancellable = true)
	static void bclib_defaultDimensions(RegistryAccess registryAccess, long l, boolean bl, CallbackInfoReturnable<MappedRegistry<LevelStem>> cir){
		MappedRegistry<LevelStem> mappedRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
		Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<Biome> registry2 = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
		Registry<NoiseGeneratorSettings> registry3 = registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
		Registry<NormalNoise.NoiseParameters> registry4 = registryAccess.registryOrThrow(Registry.NOISE_REGISTRY);
		
		mappedRegistry.register(LevelStem.NETHER, new LevelStem(
			() -> registry.getOrThrow(DimensionType.NETHER_LOCATION),
			bclib_replaceNetherBiomeSource(registry2, registry3, registry4, l, bl)
		), Lifecycle.stable());
		
		mappedRegistry.register(LevelStem.END, new LevelStem(
			() -> registry.getOrThrow(DimensionType.END_LOCATION),
			bclib_replaceEndBiomeSource(registry2, registry3, registry4, l)
		), Lifecycle.stable());
		cir.setReturnValue(mappedRegistry);
	}
	
	private static ChunkGenerator bclib_replaceNetherBiomeSource(Registry<Biome> biomeRegistry, Registry<NoiseGeneratorSettings> chunkGeneratorSettingsRegistry, Registry<NormalNoise.NoiseParameters> noiseRegistry , long seed, boolean bl) {
		return new NoiseBasedChunkGenerator(
				noiseRegistry,
				GeneratorOptions.customNetherBiomeSource()
					? new BCLibNetherBiomeSource(biomeRegistry, seed)
					: MultiNoiseBiomeSource.Preset.NETHER.biomeSource(biomeRegistry, bl),
				seed,
				() -> chunkGeneratorSettingsRegistry.getOrThrow(NoiseGeneratorSettings.NETHER)
			);
	}
	
	private static ChunkGenerator bclib_replaceEndBiomeSource(Registry<Biome> biomeRegistry, Registry<NoiseGeneratorSettings> chunkGeneratorSettingsRegistry, Registry<NormalNoise.NoiseParameters> noiseRegistry , long seed) {
		return new NoiseBasedChunkGenerator(
			noiseRegistry,
			GeneratorOptions.customEndBiomeSource()
				? new BCLibEndBiomeSource(biomeRegistry, seed)
				: new TheEndBiomeSource(biomeRegistry, seed),
			seed,
			() -> chunkGeneratorSettingsRegistry.getOrThrow(NoiseGeneratorSettings.END)
		);
	}
}