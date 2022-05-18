package ru.bclib.world.generator;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ru.bclib.BCLib;
import ru.bclib.api.biomes.BiomeAPI;
import ru.bclib.config.ConfigKeeper.StringArrayEntry;
import ru.bclib.config.Configs;
import ru.bclib.interfaces.BiomeMap;
import ru.bclib.noise.OpenSimplexNoise;
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.generator.map.hex.HexBiomeMap;
import ru.bclib.world.generator.map.square.SquareBiomeMap;

import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class BCLibEndBiomeSource extends BCLBiomeSource {
    private static final OpenSimplexNoise SMALL_NOISE = new OpenSimplexNoise(8324);
    public static Codec<BCLibEndBiomeSource> CODEC
            = RecordCodecBuilder.create((instance) -> instance.group(
                                                                      RegistryOps
                                                                              .retrieveRegistry(Registry.BIOME_REGISTRY)
                                                                              .forGetter((theEndBiomeSource) -> theEndBiomeSource.biomeRegistry),
                                                                      Codec
                                                                              .LONG
                                                                              .fieldOf("seed")
                                                                              .stable()
                                                                              .forGetter(source -> source.currentSeed))
                                                              .apply(instance,
                                                                     instance.stable(BCLibEndBiomeSource::new)
                                                                    )
                                       );
    private final Holder<Biome> centerBiome;
    private final Holder<Biome> barrens;
    private final Point pos;
    private Function<Point, Boolean> endLandFunction;
    private SimplexNoise noise;
    private BiomeMap mapLand;
    private BiomeMap mapVoid;

    private final BiomePicker endLandBiomePicker;
    private final BiomePicker endVoidBiomePicker;

    public BCLibEndBiomeSource(Registry<Biome> biomeRegistry, long seed) {
        this(biomeRegistry);
        this.setSeed(seed);
    }

    public BCLibEndBiomeSource(Registry<Biome> biomeRegistry) {
        super(biomeRegistry, getBiomes(biomeRegistry));

        endLandBiomePicker = new BiomePicker(biomeRegistry);
        endVoidBiomePicker = new BiomePicker(biomeRegistry);

        List<String> includeVoid = Configs.BIOMES_CONFIG.getEntry("force_include",
                                                                  "end_void_biomes",
                                                                  StringArrayEntry.class).getValue();
        this.possibleBiomes().forEach(biome -> {
            ResourceLocation key = biome.unwrapKey().orElseThrow().location();
            String group = key.getNamespace() + "." + key.getPath();

            if (!BiomeAPI.hasBiome(key)) {
                BCLBiome bclBiome = new BCLBiome(key, biome.value());

                if (includeVoid.contains(key.toString())) {
                    endVoidBiomePicker.addBiome(bclBiome);
                } else {
                    endLandBiomePicker.addBiome(bclBiome);
                }
            } else {
                BCLBiome bclBiome = BiomeAPI.getBiome(key);
                if (bclBiome != BiomeAPI.EMPTY_BIOME) {
                    if (bclBiome.getParentBiome() == null) {
                        if (BiomeAPI.wasRegisteredAsEndVoidBiome(key) || includeVoid.contains(key.toString())) {
                            endVoidBiomePicker.addBiome(bclBiome);
                        } else {
                            endLandBiomePicker.addBiome(bclBiome);
                        }
                    }
                }
            }
        });


        endLandBiomePicker.rebuild();
        endVoidBiomePicker.rebuild();


        this.centerBiome = biomeRegistry.getOrCreateHolder(Biomes.THE_END);
        this.barrens = biomeRegistry.getOrCreateHolder(Biomes.END_BARRENS);

        this.endLandFunction = GeneratorOptions.getEndLandFunction();
        this.pos = new Point();
    }

    private static List<Holder<Biome>> getBiomes(Registry<Biome> biomeRegistry) {
        List<String> includeLand = Configs.BIOMES_CONFIG.getEntry("force_include",
                                                                  "end_land_biomes",
                                                                  StringArrayEntry.class).getValue();
        List<String> includeVoid = Configs.BIOMES_CONFIG.getEntry("force_include",
                                                                  "end_void_biomes",
                                                                  StringArrayEntry.class).getValue();

        return biomeRegistry.stream()
                            .filter(biome -> biomeRegistry.getResourceKey(biome).isPresent())
                            .map(biome -> biomeRegistry.getOrCreateHolder(biomeRegistry.getResourceKey(biome).get()))
                            .filter(biome -> {
                                ResourceLocation key = biome.unwrapKey().orElseThrow().location();


                                if (includeLand.contains(key.toString()) || includeVoid.contains(key.toString())) {
                                    return true;
                                }

                                final boolean isEndBiome = biome.is(BiomeTags.IS_END)  ||
                                        BiomeAPI.wasRegisteredAsEndBiome(key);


                                BCLBiome bclBiome = BiomeAPI.getBiome(key);
                                if (bclBiome != BiomeAPI.EMPTY_BIOME) {
                                    if (bclBiome.getParentBiome() != null) {
                                        bclBiome = bclBiome.getParentBiome();
                                    }
                                    key = bclBiome.getID();
                                }
                                return isEndBiome;
                            }).toList();
    }

    public static float getLegacyHeightValue(SimplexNoise simplexNoise, int i, int j) {
        int k = i / 2;
        int l = j / 2;
        int m = i % 2;
        int n = j % 2;
        float f = 100.0f - Mth.sqrt(i * i + j * j) * 8.0f;
        f = Mth.clamp(f, -100.0f, 80.0f);
        for (int o = -12; o <= 12; ++o) {
            for (int p = -12; p <= 12; ++p) {
                long q = k + o;
                long r = l + p;
                if (q * q + r * r <= 4096L || !(simplexNoise.getValue(q, r) < (double) -0.9f)) continue;
                float g = (Mth.abs(q) * 3439.0f + Mth.abs(r) * 147.0f) % 13.0f + 9.0f;
                float h = m - o * 2;
                float s = n - p * 2;
                float t = 100.0f - Mth.sqrt(h * h + s * s) * g;
                t = Mth.clamp(t, -100.0f, 80.0f);
                f = Math.max(f, t);
            }
        }
        return f;
    }

    public static void register() {
        Registry.register(Registry.BIOME_SOURCE, BCLib.makeID("end_biome_source"), CODEC);
    }

    private void initMap(long seed) {
        if (GeneratorOptions.useOldBiomeGenerator()) {
            this.mapLand = new SquareBiomeMap(seed,
                                              GeneratorOptions.getBiomeSizeEndLand(),
                                              endLandBiomePicker);
            this.mapVoid = new SquareBiomeMap(seed,
                                              GeneratorOptions.getBiomeSizeEndVoid(),
                                              endVoidBiomePicker);
        } else {
            this.mapLand = new HexBiomeMap(seed,
                                           GeneratorOptions.getBiomeSizeEndLand(),
                                           endLandBiomePicker);
            this.mapVoid = new HexBiomeMap(seed,
                                           GeneratorOptions.getBiomeSizeEndVoid(),
                                           endVoidBiomePicker);
        }

        WorldgenRandom chunkRandom = new WorldgenRandom(new LegacyRandomSource(seed));
        chunkRandom.consumeCount(17292);
        this.noise = new SimplexNoise(chunkRandom);
    }

    @Override
    public void setSeed(long seed) {
        if (seed==currentSeed) return;

        super.setSeed(seed);
        initMap(seed);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler sampler) {
        if (mapLand == null || mapVoid == null)
            return this.possibleBiomes().stream().findFirst().get();
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
            float height = getLegacyHeightValue(
                    noise,
                    (biomeX >> 1) + 1,
                    (biomeZ >> 1) + 1
                                               ) + (float) SMALL_NOISE.eval(biomeX, biomeZ) * 5;

            if (height > -20F && height < -5F) {
                return barrens;
            }

            if (height < -10F) {
                return mapVoid.getBiome(posX, biomeY << 2, posZ).biome;
            } else {
                return mapLand.getBiome(posX, biomeY << 2, posZ).biome;
            }
        } else {
            pos.setLocation(biomeX, biomeZ);
            if (endLandFunction.apply(pos)) {
                return dist <= farEndBiomes ? centerBiome : mapLand.getBiome(posX, biomeY << 2, posZ).biome;
            } else {
                return dist <= farEndBiomes ? barrens : mapVoid.getBiome(posX, biomeY << 2, posZ).biome;
            }
        }
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "BCLib - The End BiomeSource";
    }
}
