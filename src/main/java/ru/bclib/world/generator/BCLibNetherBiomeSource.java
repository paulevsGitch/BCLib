package ru.bclib.world.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.BiomeSource;
import ru.bclib.BCLib;
import ru.bclib.api.BiomeAPI;
import ru.bclib.world.biomes.BCLBiome;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

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
	
	@Deprecated(forRemoval = true)
	public static final List<Consumer<BCLibNetherBiomeSource>> onInit = new LinkedList<>();
	
	public BCLibNetherBiomeSource(Registry<Biome> biomeRegistry, long seed) {
		super(getBiomes(biomeRegistry));
		
		BiomeAPI.NETHER_BIOME_PICKER.clearMutables();
		
		this.possibleBiomes.forEach(biome -> {
			ResourceLocation key = biomeRegistry.getKey(biome);
			if (!BiomeAPI.hasBiome(key)) {
				BCLBiome bclBiome = new BCLBiome(key, biome, 1, 1);
				BiomeAPI.NETHER_BIOME_PICKER.addBiomeMutable(bclBiome);
			}
			else {
				BCLBiome bclBiome = BiomeAPI.getBiome(key);
				if (bclBiome != BiomeAPI.EMPTY_BIOME && !bclBiome.hasParentBiome()) {
					if (!BiomeAPI.NETHER_BIOME_PICKER.containsImmutable(key)) {
						BiomeAPI.NETHER_BIOME_PICKER.addBiomeMutable(bclBiome);
					}
				}
			}
		});
		
		BiomeAPI.NETHER_BIOME_PICKER.getBiomes().forEach(biome -> biome.updateActualBiomes(biomeRegistry));
		BiomeAPI.NETHER_BIOME_PICKER.rebuild();
		
		this.biomeMap = new BiomeMap(seed, GeneratorOptions.getBiomeSizeNether(), BiomeAPI.NETHER_BIOME_PICKER);
		this.biomeRegistry = biomeRegistry;
		this.seed = seed;

		onInit.forEach(consumer->consumer.accept(this));
	}
	
	private static List<Biome> getBiomes(Registry<Biome> biomeRegistry) {
		return biomeRegistry.stream().filter(biome -> {
			ResourceLocation key = biomeRegistry.getKey(biome);
			BCLBiome bclBiome = BiomeAPI.getBiome(key);
			if (bclBiome != BiomeAPI.EMPTY_BIOME) {
				if (bclBiome.hasParentBiome()) {
					bclBiome = bclBiome.getParentBiome();
				}
				key = bclBiome.getID();
			}
			return BiomeAPI.NETHER_BIOME_PICKER.containsImmutable(key) || (biome.getBiomeCategory() == BiomeCategory.NETHER && BiomeAPI.isDatapackBiome(key));
		}).toList();
	}
	
	@Override
	public Biome getNoiseBiome(int biomeX, int biomeY, int biomeZ) {
		if ((biomeX & 63) == 0 && (biomeZ & 63) == 0) {
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
