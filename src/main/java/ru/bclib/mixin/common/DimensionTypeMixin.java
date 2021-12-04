package ru.bclib.mixin.common;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ru.bclib.world.generator.BCLibEndBiomeSource;
import ru.bclib.world.generator.BCLibNetherBiomeSource;

import java.util.OptionalInt;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {
	@Inject(
		method = "defaultDimensions(Lnet/minecraft/core/RegistryAccess;JZ)Lnet/minecraft/core/MappedRegistry;",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At("TAIL")
	)
	private static void bclib_updateDimensions(RegistryAccess registryAccess, long seed, boolean bl, CallbackInfoReturnable<MappedRegistry<LevelStem>> info, MappedRegistry<LevelStem> mappedRegistry, Registry<DimensionType> registry, Registry<Biome> biomeRegistry, Registry<NoiseGeneratorSettings> noiseSettingsRegistry, Registry<NormalNoise.NoiseParameters> noiseParamRegistry) {
		int id = mappedRegistry.getId(mappedRegistry.get(LevelStem.NETHER));
		mappedRegistry.registerOrOverride(
			OptionalInt.of(id),
			LevelStem.NETHER,
			new LevelStem(
				() -> registry.getOrThrow(DimensionType.NETHER_LOCATION),
				new NoiseBasedChunkGenerator(
					noiseParamRegistry,
					new BCLibNetherBiomeSource(biomeRegistry, seed),
					seed,
					() -> noiseSettingsRegistry.getOrThrow(NoiseGeneratorSettings.NETHER)
				)
			),
			Lifecycle.stable()
		);
		
		id = mappedRegistry.getId(mappedRegistry.get(LevelStem.END));
		mappedRegistry.registerOrOverride(
			OptionalInt.of(id),
			LevelStem.END,
			new LevelStem(
				() -> registry.getOrThrow(DimensionType.END_LOCATION),
				new NoiseBasedChunkGenerator(
					noiseParamRegistry,
					new BCLibEndBiomeSource(biomeRegistry, seed),
					seed,
					() -> noiseSettingsRegistry.getOrThrow(NoiseGeneratorSettings.END)
				)
			),
			Lifecycle.stable()
		);
	}
}
