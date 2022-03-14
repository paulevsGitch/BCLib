package ru.bclib.world.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import ru.bclib.BCLib;
import ru.bclib.api.biomes.BiomeAPI;
import ru.bclib.config.ConfigKeeper.StringArrayEntry;
import ru.bclib.config.Configs;
import ru.bclib.interfaces.BiomeMap;
import ru.bclib.mixin.common.BiomeAccessor;
import ru.bclib.noise.OpenSimplexNoise;
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.generator.map.hex.HexBiomeMap;
import ru.bclib.world.generator.map.square.SquareBiomeMap;

import java.awt.Point;
import java.util.List;
import java.util.function.Function;

public class BCLibEndBiomeSource extends BCLBiomeSource {
	public static Codec<BCLibEndBiomeSource> CODEC = RecordCodecBuilder.create((instance) -> instance.group(RegistryOps
			.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter((theEndBiomeSource) -> theEndBiomeSource.biomeRegistry), Codec.LONG.fieldOf("seed").stable().forGetter((theEndBiomeSource) -> theEndBiomeSource.seed)).apply(instance, instance.stable(BCLibEndBiomeSource::new)));
	private static final OpenSimplexNoise SMALL_NOISE = new OpenSimplexNoise(8324);
	private Function<Point, Boolean> endLandFunction;

	private final SimplexNoise noise;
	private final Holder<Biome> centerBiome;
	private final Holder<Biome> barrens;
	private BiomeMap mapLand;
	private BiomeMap mapVoid;
	private final Point pos;
	
	public BCLibEndBiomeSource(Registry<Biome> biomeRegistry, long seed) {
		super(biomeRegistry, seed, getBiomes(biomeRegistry));

		BiomeAPI.END_LAND_BIOME_PICKER.clearMutables();
		BiomeAPI.END_VOID_BIOME_PICKER.clearMutables();
		
		List<String> includeVoid = Configs.BIOMES_CONFIG.getEntry("force_include", "end_void_biomes", StringArrayEntry.class).getValue();
		this.possibleBiomes().forEach(biome -> {
			ResourceLocation key = biomeRegistry.getKey(biome.value());
			String group = key.getNamespace() + "." + key.getPath();
			
			if (!BiomeAPI.hasBiome(key)) {
				BCLBiome bclBiome = new BCLBiome(key, biome);
				
				if (includeVoid.contains(key.toString())) {
					BiomeAPI.END_VOID_BIOME_PICKER.addBiomeMutable(bclBiome);
				}
				else {
					BiomeAPI.END_LAND_BIOME_PICKER.addBiomeMutable(bclBiome);
				}
			}
			else {
				BCLBiome bclBiome = BiomeAPI.getBiome(key);
				if (bclBiome != BiomeAPI.EMPTY_BIOME) {
					if (bclBiome.getParentBiome() == null) {
						if (!BiomeAPI.END_LAND_BIOME_PICKER.containsImmutable(key) && !BiomeAPI.END_VOID_BIOME_PICKER.containsImmutable(key)) {
							if (includeVoid.contains(key.toString())) {
								BiomeAPI.END_VOID_BIOME_PICKER.addBiomeMutable(bclBiome);
							}
							else {
								BiomeAPI.END_LAND_BIOME_PICKER.addBiomeMutable(bclBiome);
							}
						}
					}
				}
			}
		});
		
		BiomeAPI.END_LAND_BIOME_PICKER.getBiomes().forEach(biome -> biome.updateActualBiomes(biomeRegistry));
		BiomeAPI.END_VOID_BIOME_PICKER.getBiomes().forEach(biome -> biome.updateActualBiomes(biomeRegistry));
		
		BiomeAPI.END_LAND_BIOME_PICKER.rebuild();
		BiomeAPI.END_VOID_BIOME_PICKER.rebuild();
		
		if (GeneratorOptions.useOldBiomeGenerator()) {
			this.mapLand = new SquareBiomeMap(seed, GeneratorOptions.getBiomeSizeEndLand(), BiomeAPI.END_LAND_BIOME_PICKER);
			this.mapVoid = new SquareBiomeMap(seed, GeneratorOptions.getBiomeSizeEndVoid(), BiomeAPI.END_VOID_BIOME_PICKER);
		}
		else {
			this.mapLand = new HexBiomeMap(seed, GeneratorOptions.getBiomeSizeEndLand(), BiomeAPI.END_LAND_BIOME_PICKER);
			this.mapVoid = new HexBiomeMap(seed, GeneratorOptions.getBiomeSizeEndVoid(), BiomeAPI.END_VOID_BIOME_PICKER);
		}
		
		this.centerBiome = biomeRegistry.getHolderOrThrow(Biomes.THE_END);
		this.barrens = biomeRegistry.getHolderOrThrow(Biomes.END_BARRENS);

		WorldgenRandom chunkRandom = new WorldgenRandom(new LegacyRandomSource(seed));
		chunkRandom.consumeCount(17292);
		this.noise = new SimplexNoise(chunkRandom);
		
		this.endLandFunction = GeneratorOptions.getEndLandFunction();
		this.pos = new Point();
	}
	
	private static List<Biome> getBiomes(Registry<Biome> biomeRegistry) {
		List<String> includeLand = Configs.BIOMES_CONFIG.getEntry("force_include", "end_land_biomes", StringArrayEntry.class).getValue();
		List<String> includeVoid = Configs.BIOMES_CONFIG.getEntry("force_include", "end_void_biomes", StringArrayEntry.class).getValue();
		
		return biomeRegistry.stream().filter(biome -> {
			ResourceLocation key = biomeRegistry.getKey(biome);
			
			if (includeLand.contains(key.toString()) || includeVoid.contains(key.toString())) {
				return true;
			}

			final boolean isEndBiome;
			if ((Object)biome instanceof BiomeAccessor bacc) {
				isEndBiome = bacc.bclib_getBiomeCategory() == BiomeCategory.THEEND;
				if (GeneratorOptions.addEndBiomesByCategory() && isEndBiome) {
					return true;
				}
			} else {
				isEndBiome = false;
			}
			
			BCLBiome bclBiome = BiomeAPI.getBiome(key);
			if (bclBiome != BiomeAPI.EMPTY_BIOME) {
				if (bclBiome.getParentBiome() != null) {
					bclBiome = bclBiome.getParentBiome();
				}
				key = bclBiome.getID();
			}
			return BiomeAPI.END_LAND_BIOME_PICKER.containsImmutable(key) || BiomeAPI.END_VOID_BIOME_PICKER.containsImmutable(key) || (isEndBiome && BiomeAPI.isDatapackBiome(key));
		}).toList();
	}
	
	@Override
	public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler sampler) {
		long posX = biomeX << 2;
		long posZ = biomeZ << 2;
		long farEndBiomes = GeneratorOptions.getFarEndBiomes();
		long dist = posX * posX + posZ * posZ;
		
		if ((biomeX & 63) == 0 && (biomeZ & 63) == 0) {
			mapLand.clearCache();
			mapVoid.clearCache();
		}
		
		if (endLandFunction == null) {
			if (dist <= farEndBiomes) return centerBiome;
			float height = TheEndBiomeSource.getHeightValue(
				noise,
				(biomeX >> 1) + 1,
				(biomeZ >> 1) + 1
			) + (float) SMALL_NOISE.eval(biomeX, biomeZ) * 5;
			
			if (height > -20F && height < -5F) {
				return barrens;
			}
			
			if (height < -10F) {
				return mapVoid.getBiome(posX, biomeY << 2, posZ).getActualBiome();
			}
			else {
				return mapLand.getBiome(posX, biomeY << 2, posZ).getActualBiome();
			}
		}
		else {
			pos.setLocation(biomeX, biomeZ);
			if (endLandFunction.apply(pos)) {
				return dist <= farEndBiomes ? centerBiome : mapLand.getBiome(posX, biomeY << 2, posZ).getActualBiome();
			}
			else {
				return dist <= farEndBiomes ? barrens : mapVoid.getBiome(posX, biomeY << 2, posZ).getActualBiome();
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
		Registry.register(Registry.BIOME_SOURCE, BCLib.makeID("end_biome_source"), CODEC);
	}
	
	@Override
	public String toString() {
		return "BCLib - The End BiomeSource";
	}
}
