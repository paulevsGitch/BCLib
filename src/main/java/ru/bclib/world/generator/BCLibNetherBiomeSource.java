package ru.bclib.world.generator;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.*;

import net.fabricmc.fabric.impl.biome.NetherBiomeData;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import java.util.function.Function;

public class BCLibNetherBiomeSource extends BCLBiomeSource {
    private static boolean forceLegacyGenerator = false;
    private static int lastWorldHeight;
    private static int worldHeight;
    public static final Codec<BCLibNetherBiomeSource> CODEC = RecordCodecBuilder
            .create(instance -> instance
                    .group(RegistryOps
                                   .retrieveRegistry(Registry.BIOME_REGISTRY)
                                   .forGetter(source -> source.biomeRegistry),
						   Codec
								   .LONG
								   .fieldOf("seed")
								   .stable()
								   .forGetter(source -> {
									   return source.currentSeed;
								   })
						  )
                    .apply(instance, instance.stable(BCLibNetherBiomeSource::new))
                   );
    private BiomeMap biomeMap;
    private final BiomePicker biomePicker;
	public BCLibNetherBiomeSource(Registry<Biome> biomeRegistry) {
        super(biomeRegistry, getBiomes(biomeRegistry));
        biomePicker = new BiomePicker(biomeRegistry);

        this.possibleBiomes().forEach(biome -> {
            ResourceLocation key = biome.unwrapKey().orElseThrow().location();

            if (!BiomeAPI.hasBiome(key)) {
                BCLBiome bclBiome = new BCLBiome(key, biome.value());
                biomePicker.addBiome(bclBiome);
            } else {
                BCLBiome bclBiome = BiomeAPI.getBiome(key);
                if (bclBiome != BiomeAPI.EMPTY_BIOME) {
                    if (bclBiome.getParentBiome() == null) {
                        biomePicker.addBiome(bclBiome);
                    }
                }
            }
        });

        biomePicker.rebuild();
	}

    public BCLibNetherBiomeSource(Registry<Biome> biomeRegistry, long seed) {
        this(biomeRegistry);
        setSeed(seed);
    }

    /**
     * When true, the older square generator is used for the nether.
     * <p>
     * This override is used (for example) by BetterNether to force the legacy generation for worlds
     * that were created before 1.18
     *
     * @param val wether or not you want to force the old generatore.
     */
    public static void setForceLegacyGeneration(boolean val) {
        forceLegacyGenerator = val;
    }

    /**
     * Set world height, used when Nether is larger than vanilla 128 blocks tall.
     *
     * @param worldHeight height of the Nether ceiling.
     */
    public static void setWorldHeight(int worldHeight) {
        BCLibNetherBiomeSource.worldHeight = worldHeight;
    }

    private static List<Holder<Biome>> getBiomes(Registry<Biome> biomeRegistry) {
        List<String> include = Configs.BIOMES_CONFIG.getEntry("force_include", "nether_biomes", StringArrayEntry.class)
                                                    .getValue();
        List<String> exclude = Configs.BIOMES_CONFIG.getEntry("force_exclude", "nether_biomes", StringArrayEntry.class)
                                                    .getValue();

        return biomeRegistry.stream()
                            .filter(biome -> biomeRegistry.getResourceKey(biome).isPresent())
                            .map(biome -> biomeRegistry.getOrCreateHolder(biomeRegistry.getResourceKey(biome).get()))
                            .filter(biome -> {
                                ResourceLocation location = biome.unwrapKey().orElseThrow().location();
                                final String strLocation = location.toString();
                                if (exclude.contains(strLocation)) return false;
                                if (include.contains(strLocation)) return true;

                                return
                                        NetherBiomeData.canGenerateInNether(biome.unwrapKey().get()) ||
                                        biome.is(BiomeTags.IS_NETHER) ||
                                        BiomeAPI.wasRegisteredAsNetherBiome(location);
                            }).toList();
    }

    public static <T> void debug(Object el, Registry<T> reg) {
        System.out.println("Unknown " + el + " in " + reg);
    }

    public static void register() {
        Registry.register(Registry.BIOME_SOURCE, BCLib.makeID("nether_biome_source"), CODEC);
    }

    @Override
    public void setSeed(long seed) {
        if (seed==currentSeed) return;
        super.setSeed(seed);
        initMap(seed);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler var4) {
        if (biomeMap == null)
            return this.possibleBiomes().stream().findFirst().get();

        if (lastWorldHeight != worldHeight) {
            lastWorldHeight = worldHeight;
            initMap(this.currentSeed);
        }
        if ((biomeX & 63) == 0 && (biomeZ & 63) == 0) {
            biomeMap.clearCache();
        }
        BiomePicker.Entry bb = biomeMap.getBiome(biomeX << 2, biomeY << 2, biomeZ << 2);
        return bb.actual;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    private void initMap(long seed) {
        boolean useLegacy = GeneratorOptions.useOldBiomeGenerator() || forceLegacyGenerator;
        TriFunction<Long, Integer, BiomePicker, BiomeMap> mapConstructor = useLegacy
                ? SquareBiomeMap::new
                : HexBiomeMap::new;
        if (worldHeight > 128 && GeneratorOptions.useVerticalBiomes()) {
            this.biomeMap = new MapStack(
                    seed,
                    GeneratorOptions.getBiomeSizeNether(),
                    biomePicker,
                    GeneratorOptions.getVerticalBiomeSizeNether(),
                    worldHeight,
                    mapConstructor
            );
        } else {
            this.biomeMap = mapConstructor.apply(seed,
                                                 GeneratorOptions.getBiomeSizeNether(),
                                                 biomePicker);
        }
    }

    @Override
    public String toString() {
        return "BCLib - Nether BiomeSource ("+Integer.toHexString(hashCode())+")";
    }
}
