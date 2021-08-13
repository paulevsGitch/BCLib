package ru.bclib.world.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import ru.bclib.BCLib;
import ru.bclib.api.BiomeAPI;
import ru.bclib.noise.OpenSimplexNoise;
import ru.bclib.world.biomes.BCLBiome;

import java.awt.Point;
import java.util.List;
import java.util.function.Function;

public class BCLibNetherBiomeSource extends BiomeSource {
	public static final Codec<BCLibNetherBiomeSource> CODEC = RecordCodecBuilder.create((instance) -> {
		return instance.group(RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter((theEndBiomeSource) -> {
			return theEndBiomeSource.biomeRegistry;
		}), Codec.LONG.fieldOf("seed").stable().forGetter((theEndBiomeSource) -> {
			return theEndBiomeSource.seed;
		})).apply(instance, instance.stable(BCLibNetherBiomeSource::new));
	});
	private final Registry<Biome> biomeRegistry;
	private BiomeMap biomeMap;
	private final long seed;
	
	public BCLibNetherBiomeSource(Registry<Biome> biomeRegistry, long seed) {
		super(getBiomes(biomeRegistry));
		
		BiomeAPI.NETHER_BIOME_PICKER.clearMutables();
		biomeRegistry.forEach(biome -> {
			ResourceLocation key = biomeRegistry.getKey(biome);
			BCLBiome bclBiome = BiomeAPI.getBiome(key);
			bclBiome.updateActualBiomes(biomeRegistry);
			if (!BiomeAPI.NETHER_BIOME_PICKER.containsImmutable(key)) {
				BiomeAPI.NETHER_BIOME_PICKER.addBiomeMutable(bclBiome);
			}
		});
		BiomeAPI.NETHER_BIOME_PICKER.rebuild();
		
		this.biomeMap = new BiomeMap(seed, GeneratorOptions.getBiomeSizeEndLand(), BiomeAPI.NETHER_BIOME_PICKER);
		this.biomeRegistry = biomeRegistry;
		this.seed = seed;
		
		WorldgenRandom chunkRandom = new WorldgenRandom(seed);
		chunkRandom.consumeCount(17292);
	}
	
	private static List<Biome> getBiomes(Registry<Biome> biomeRegistry) {
		return biomeRegistry.stream().filter(biome -> BiomeAPI.isEndBiome(biomeRegistry.getKey(biome))).toList();
	}
	
	@Override
	public Biome getNoiseBiome(int biomeX, int biomeY, int biomeZ) {
		long i = (long) biomeX * (long) biomeX;
		long j = (long) biomeZ * (long) biomeZ;
		
		if ((biomeX & 31) == 0 && (biomeZ & 31) == 0) {
			biomeMap.clearCache();
		}
		
		return biomeMap.getBiome(biomeX << 2, biomeZ << 2).getActualBiome();
	}
	
	@Override
	public BiomeSource withSeed(long seed) {
		return new BCLibNetherBiomeSource(biomeRegistry, seed);
	}
	
	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}
	
	public static void register() {
		Registry.register(Registry.BIOME_SOURCE, BCLib.makeID("nether_biome_source"), CODEC);
	}
}
