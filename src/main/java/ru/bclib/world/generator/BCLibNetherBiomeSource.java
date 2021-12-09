package ru.bclib.world.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
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
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.generator.map.MapStack;
import ru.bclib.world.generator.map.hex.HexBiomeMap;
import ru.bclib.world.generator.map.square.SquareBiomeMap;

import java.util.List;

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
		super(getBiomes(biomeRegistry));
		
		BiomeAPI.initRegistry(biomeRegistry);
		BiomeAPI.NETHER_BIOME_PICKER.clearMutables();
		
		this.possibleBiomes().forEach(biome -> {
			ResourceLocation key = biomeRegistry.getKey(biome);
			if (!BiomeAPI.hasBiome(key)) {
				String group = key.getNamespace() + "." + key.getPath();
				float chance = Configs.BIOMES_CONFIG.getFloat(group, "generation_chance", 1.0F);
				float fog = Configs.BIOMES_CONFIG.getFloat(group, "fog_density", 1.0F);
				BCLBiome bclBiome = new BCLBiome(key, biome).setGenChance(chance).setFogDensity(fog);
				BiomeAPI.NETHER_BIOME_PICKER.addBiomeMutable(bclBiome);
			}
			else {
				BCLBiome bclBiome = BiomeAPI.getBiome(key);
				if (bclBiome != BiomeAPI.EMPTY_BIOME && bclBiome.getParentBiome() == null) {
					if (!BiomeAPI.NETHER_BIOME_PICKER.containsImmutable(key)) {
						BiomeAPI.NETHER_BIOME_PICKER.addBiomeMutable(bclBiome);
					}
				}
			}
		});
		
		Configs.BIOMES_CONFIG.saveChanges();
		BiomeAPI.NETHER_BIOME_PICKER.getBiomes().forEach(biome -> biome.updateActualBiomes(biomeRegistry));
		BiomeAPI.NETHER_BIOME_PICKER.rebuild();
		
		initMap();
		
		this.biomeRegistry = biomeRegistry;
		this.seed = seed;
	}
	
	private static List<Biome> getBiomes(Registry<Biome> biomeRegistry) {
		List<String> include = Configs.BIOMES_CONFIG.getEntry("force_include", "nether_biomes", StringArrayEntry.class).getValue();
		
		return biomeRegistry.stream().filter(biome -> {
			ResourceLocation key = biomeRegistry.getKey(biome);
			
			if (include.contains(key.toString())) {
				return true;
			}
			
			if (GeneratorOptions.addNetherBiomesByCategory() && biome.getBiomeCategory() == BiomeCategory.NETHER) {
				return true;
			}
			
			BCLBiome bclBiome = BiomeAPI.getBiome(key);
			if (bclBiome != BiomeAPI.EMPTY_BIOME) {
				if (bclBiome.getParentBiome() != null) {
					bclBiome = bclBiome.getParentBiome();
				}
				key = bclBiome.getID();
			}
			return BiomeAPI.NETHER_BIOME_PICKER.containsImmutable(key) || (biome.getBiomeCategory() == BiomeCategory.NETHER && BiomeAPI.isDatapackBiome(key));
		}).toList();
	}
	
	@Override
	public Biome getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler var4) {
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
			this.biomeMap = new MapStack(seed, GeneratorOptions.getBiomeSizeNether(), BiomeAPI.NETHER_BIOME_PICKER, 86, worldHeight, mapConstructor);
		}
		else {
			this.biomeMap = mapConstructor.apply(seed, GeneratorOptions.getBiomeSizeNether(), BiomeAPI.NETHER_BIOME_PICKER);
		}
	}
}
