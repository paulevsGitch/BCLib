package ru.bclib.world.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.apache.commons.lang3.function.TriFunction;
import ru.bclib.BCLib;
import ru.bclib.api.biomes.BiomeAPI;
import ru.bclib.config.ConfigKeeper.StringArrayEntry;
import ru.bclib.config.Configs;
import ru.bclib.interfaces.BiomeMap;
import ru.bclib.mixin.common.BiomeAccessor;
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.generator.map.MapStack;
import ru.bclib.world.generator.map.hex.HexBiomeMap;
import ru.bclib.world.generator.map.square.SquareBiomeMap;

import java.util.List;

public class BCLibNetherBiomeSource extends BCLBiomeSource {

	public static final Codec<BCLibNetherBiomeSource> CODEC = RecordCodecBuilder
			.create(instance -> instance
					.group(RegistryOps
									.retrieveRegistry(Registry.BIOME_REGISTRY)
									.forGetter(source -> source.biomeRegistry)
							,
							Codec
									.LONG
									.fieldOf("seed")
									.stable()
									.forGetter(source -> source.seed)
					)
					.apply(instance, instance.stable(BCLibNetherBiomeSource::new))
			);

	private BiomeMap biomeMap;

	private static boolean forceLegacyGenerator = false;
	private static int lastWorldHeight;
	private static int worldHeight;

	/**
	 * When true, the older square generator is used for the nether.
	 *
	 * This override is used (for example) by BetterNether to force the legacy generation for worlds
	 * that were created before 1.18
	 * @param val wether or not you want to force the old generatore.
	 */
	public static void setForceLegacyGeneration(boolean val){
		forceLegacyGenerator = val;
	}
	
	/**
	 * Set world height, used when Nether is larger than vanilla 128 blocks tall.
	 * @param worldHeight height of the Nether ceiling.
	 */
	public static void setWorldHeight(int worldHeight) {
		BCLibNetherBiomeSource.worldHeight = worldHeight;
	}
	
	public BCLibNetherBiomeSource(Registry<Biome> biomeRegistry, long seed) {
		super(biomeRegistry, seed, getBiomes(biomeRegistry));
		
		BiomeAPI.NETHER_BIOME_PICKER.clearMutables();
		
		this.possibleBiomes().forEach(biome -> {
			ResourceLocation key = biome.unwrapKey().orElseThrow().location();
			
			if (!BiomeAPI.hasBiome(key)) {
				BCLBiome bclBiome = new BCLBiome(key, biome.value());
				BiomeAPI.NETHER_BIOME_PICKER.addBiomeMutable(bclBiome);
			}
			else {
				BCLBiome bclBiome = BiomeAPI.getBiome(key);
				if (bclBiome != BiomeAPI.EMPTY_BIOME) {
					if (bclBiome.getParentBiome() == null) {
						if (!BiomeAPI.NETHER_BIOME_PICKER.containsImmutable(key)) {
							BiomeAPI.NETHER_BIOME_PICKER.addBiomeMutable(bclBiome);
						}
					}
				}
			}
		});
		
		BiomeAPI.NETHER_BIOME_PICKER.getBiomes().forEach(biome -> biome.updateActualBiomes(biomeRegistry));
		BiomeAPI.NETHER_BIOME_PICKER.rebuild();
		
		initMap();
	}
	
	private static List<Holder<Biome>> getBiomes(Registry<Biome> biomeRegistry) {
		List<String> include = Configs.BIOMES_CONFIG.getEntry("force_include", "nether_biomes", StringArrayEntry.class).getValue();

		return biomeRegistry.stream()
				.filter(biome -> biomeRegistry.getResourceKey(biome).isPresent())
				.map(biome -> biomeRegistry.getOrCreateHolder(biomeRegistry.getResourceKey(biome).get()))
				.filter(biome -> {
					ResourceLocation key = biome.unwrapKey().orElseThrow().location();

					if (include.contains(key.toString())) {
						return true;
					}

					if (GeneratorOptions.addNetherBiomesByCategory() && (biome instanceof BiomeAccessor) && ((BiomeAccessor)(Object)biome).bclib_getBiomeCategory()== BiomeCategory.NETHER) {
						return true;
					}

					BCLBiome bclBiome = BiomeAPI.getBiome(key);
					if (bclBiome != BiomeAPI.EMPTY_BIOME) {
						if (bclBiome.getParentBiome() != null) {
							bclBiome = bclBiome.getParentBiome();
						}
						key = bclBiome.getID();
					}
					final boolean isNetherBiome;
					if ((Object)biome instanceof BiomeAccessor bacc) {
						isNetherBiome = bacc.bclib_getBiomeCategory() == BiomeCategory.NETHER;
					} else {
						isNetherBiome = false;
					}
					return BiomeAPI.NETHER_BIOME_PICKER.containsImmutable(key) || (isNetherBiome && BiomeAPI.isDatapackBiome(key));
		}).toList();
	}
	
	@Override
	public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler var4) {
		if (lastWorldHeight != worldHeight) {
			lastWorldHeight = worldHeight;
			initMap();
		}
		if ((biomeX & 63) == 0 && (biomeZ & 63) == 0) {
			biomeMap.clearCache();
		}
		return biomeMap.getBiome(biomeX << 2, biomeY << 2, biomeZ << 2).getActualBiome();
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
	
	private void initMap() {
		boolean useLegacy = GeneratorOptions.useOldBiomeGenerator() || forceLegacyGenerator;
		TriFunction<Long, Integer, BiomePicker, BiomeMap> mapConstructor = useLegacy ? SquareBiomeMap::new : HexBiomeMap::new;
		if (worldHeight > 128 && GeneratorOptions.useVerticalBiomes()) {
			this.biomeMap = new MapStack(
				seed,
				GeneratorOptions.getBiomeSizeNether(),
				BiomeAPI.NETHER_BIOME_PICKER,
				GeneratorOptions.getVerticalBiomeSizeNether(),
				worldHeight,
				mapConstructor
			);
		}
		else {
			this.biomeMap = mapConstructor.apply(seed, GeneratorOptions.getBiomeSizeNether(), BiomeAPI.NETHER_BIOME_PICKER);
		}
	}
	
	@Override
	public String toString() {
		return "BCLib - Nether BiomeSource";
	}
}
