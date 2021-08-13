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

public class BCLibEndBiomeSource extends BiomeSource {
	public static final Codec<BCLibEndBiomeSource> CODEC = RecordCodecBuilder.create((instance) -> {
		return instance.group(RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter((theEndBiomeSource) -> {
			return theEndBiomeSource.biomeRegistry;
		}), Codec.LONG.fieldOf("seed").stable().forGetter((theEndBiomeSource) -> {
			return theEndBiomeSource.seed;
		})).apply(instance, instance.stable(BCLibEndBiomeSource::new));
	});
	private static final OpenSimplexNoise SMALL_NOISE = new OpenSimplexNoise(8324);
	private Function<Point, Boolean> endLandFunction;
	private final Registry<Biome> biomeRegistry;
	private final SimplexNoise noise;
	private final Biome centerBiome;
	private final Biome barrens;
	private BiomeMap mapLand;
	private BiomeMap mapVoid;
	private final long seed;
	private final Point pos;
	
	public BCLibEndBiomeSource(Registry<Biome> biomeRegistry, long seed) {
		super(getBiomes(biomeRegistry));
		
		biomeRegistry.forEach(biome -> {
			ResourceLocation key = biomeRegistry.getKey(biome);
			BCLBiome bclBiome = BiomeAPI.getBiome(key);
			bclBiome.updateActualBiomes(biomeRegistry);
			if (!BiomeAPI.END_LAND_BIOME_PICKER.containsImmutable(key)) {
				BiomeAPI.END_LAND_BIOME_PICKER.addBiomeMutable(bclBiome);
			}
		});
		
		this.mapLand = new BiomeMap(seed, GeneratorOptions.getBiomeSizeEndLand(), BiomeAPI.END_LAND_BIOME_PICKER);
		this.mapVoid = new BiomeMap(seed, GeneratorOptions.getBiomeSizeEndVoid(), BiomeAPI.END_VOID_BIOME_PICKER);
		this.centerBiome = biomeRegistry.getOrThrow(Biomes.THE_END);
		this.barrens = biomeRegistry.getOrThrow(Biomes.END_BARRENS);
		this.biomeRegistry = biomeRegistry;
		this.seed = seed;
		
		WorldgenRandom chunkRandom = new WorldgenRandom(seed);
		chunkRandom.consumeCount(17292);
		this.noise = new SimplexNoise(chunkRandom);
		
		this.endLandFunction = GeneratorOptions.getEndLandFunction();
		this.pos = new Point();
	}
	
	private static List<Biome> getBiomes(Registry<Biome> biomeRegistry) {
		return biomeRegistry.stream().filter(biome -> BiomeAPI.isEndBiome(biomeRegistry.getKey(biome))).toList();
	}
	
	@Override
	public Biome getNoiseBiome(int biomeX, int biomeY, int biomeZ) {
		long i = (long) biomeX * (long) biomeX;
		long j = (long) biomeZ * (long) biomeZ;
		long dist = i + j;
		
		if ((biomeX & 31) == 0 && (biomeZ & 31) == 0) {
			mapLand.clearCache();
			mapVoid.clearCache();
		}
		
		BCLBiome endBiome = null;
		if (endLandFunction == null) {
			if (dist <= 65536L) return centerBiome;
			float height = TheEndBiomeSource.getHeightValue(
				noise,
				(biomeX >> 1) + 1,
				(biomeZ >> 1) + 1
			) + (float) SMALL_NOISE.eval(biomeX, biomeZ) * 5;
			
			if (height > -20F && height < -5F) {
				return barrens;
			}
			
			if (height < -10F) {
				return mapVoid.getBiome(biomeX << 2, biomeZ << 2).getActualBiome();
			}
			else {
				return mapLand.getBiome(biomeX << 2, biomeZ << 2).getActualBiome();
			}
		}
		else {
			pos.setLocation(biomeX, biomeZ);
			if (endLandFunction.apply(pos)) {
				return dist <= 65536L ? centerBiome : mapLand.getBiome(biomeX << 2, biomeZ << 2).getActualBiome();
			}
			else {
				return dist <= 65536L ? barrens : mapVoid.getBiome(biomeX << 2, biomeZ << 2).getActualBiome();
			}
		}
	}
	
	@Override
	public BiomeSource withSeed(long seed) {
		return new BCLibEndBiomeSource(biomeRegistry, seed);
	}
	
	@Override
	protected Codec<? extends BiomeSource> codec() {
		return CODEC;
	}
	
	public static void register() {
		Registry.register(Registry.BIOME_SOURCE, BCLib.makeID("better_end_biome_source"), CODEC);
	}
}
