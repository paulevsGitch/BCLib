package ru.bclib.mixin.common;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.NotNull;
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
		method = "defaultDimensions(Lnet/minecraft/core/RegistryAccess;JZ)Lnet/minecraft/core/Registry;",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At("TAIL")
	)
	private static void bclib_updateDimensions(RegistryAccess registryAccess, long seed, boolean bl, CallbackInfoReturnable<Registry> info, WritableRegistry writableRegistry, Registry registry, Registry biomeRegistry, Registry structureRegistry, Registry noiseSettingsRegistry, Registry noiseParamRegistry) {
		int id = writableRegistry.getId(writableRegistry.get(LevelStem.NETHER));
		writableRegistry.register(
				LevelStem.NETHER, 
				new LevelStem(
						registry.getOrCreateHolder(DimensionType.NETHER_LOCATION),
						new NoiseBasedChunkGenerator(
								structureRegistry,
								noiseParamRegistry,
								new BCLibNetherBiomeSource(biomeRegistry, seed),
								seed,
								noiseSettingsRegistry.getOrCreateHolder(NoiseGeneratorSettings.NETHER))
				),
				Lifecycle.stable()
		);


		id = writableRegistry.getId(writableRegistry.get(LevelStem.END));
		writableRegistry.register(
				LevelStem.END,
				new LevelStem(
						registry.getOrCreateHolder(DimensionType.END_LOCATION),
						new NoiseBasedChunkGenerator(
								structureRegistry,
								noiseParamRegistry,
								new BCLibEndBiomeSource(biomeRegistry, seed),
								seed,
								noiseSettingsRegistry.getOrCreateHolder(NoiseGeneratorSettings.END))
				),
				Lifecycle.stable()
		);
	}
}
