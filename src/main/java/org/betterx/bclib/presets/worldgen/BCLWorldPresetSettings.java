package org.betterx.bclib.presets.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.ChunkGeneratorAccessor;
import org.betterx.bclib.interfaces.NoiseGeneratorSettingsProvider;
import org.betterx.bclib.world.generator.BCLBiomeSource;
import org.betterx.bclib.world.generator.BCLibNetherBiomeSource;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BCLWorldPresetSettings extends WorldPresetSettings {
    public final static BCLWorldPresetSettings DEFAULT = new BCLWorldPresetSettings(BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION);

    public static final Codec<BCLWorldPresetSettings> CODEC = RecordCodecBuilder
            .create((RecordCodecBuilder.Instance<BCLWorldPresetSettings> builderInstance) -> {
                RecordCodecBuilder<BCLWorldPresetSettings, Integer> netherVersion = Codec.INT
                        .fieldOf(LevelStem.NETHER.location().toString())
                        .forGetter((BCLWorldPresetSettings settings) -> settings.netherVersion);

                RecordCodecBuilder<BCLWorldPresetSettings, Integer> endVersion = Codec.INT
                        .fieldOf(LevelStem.END.location().toString())
                        .forGetter((BCLWorldPresetSettings settings) -> settings.endVersion);


                return builderInstance.group(netherVersion, endVersion)
                                      .apply(builderInstance, builderInstance.stable(BCLWorldPresetSettings::new));
            });
    public final int netherVersion;
    public final int endVersion;

    public BCLWorldPresetSettings(int version) {
        this(version, version);
    }

    public BCLWorldPresetSettings(int netherVersion, int endVersion) {
        this.netherVersion = netherVersion;
        this.endVersion = endVersion;
    }


    @Override
    public Codec<? extends WorldPresetSettings> codec() {
        return CODEC;
    }

    public BCLWorldPreset buildPreset(LevelStem overworldStem,
                                      WorldGenUtilities.Context netherContext,
                                      WorldGenUtilities.Context endContext) {
        return new BCLWorldPreset(buildDimensionMap(overworldStem, netherContext, endContext), 1000, this);
    }

    public Map<ResourceKey<LevelStem>, LevelStem> buildDimensionMap(LevelStem overworldStem,
                                                                    WorldGenUtilities.Context netherContext,
                                                                    WorldGenUtilities.Context endContext) {
        return Map.of(LevelStem.OVERWORLD,
                overworldStem,
                LevelStem.NETHER,
                createNetherStem(netherContext),
                LevelStem.END,
                createEndStem(endContext)
        );
    }

    public int getVersion(ResourceKey<LevelStem> key) {
        if (key == LevelStem.NETHER) return netherVersion;
        if (key == LevelStem.END) return endVersion;

        return BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA;
    }

    public LevelStem createStem(WorldGenUtilities.Context ctx, ResourceKey<LevelStem> key) {
        if (key == LevelStem.NETHER) return createNetherStem(ctx);
        if (key == LevelStem.END) return createEndStem(ctx);
        return null;
    }

    public LevelStem createNetherStem(WorldGenUtilities.Context ctx) {
        return WorldGenUtilities.getBCLNetherLevelStem(ctx, Optional.of(netherVersion));
    }

    public LevelStem createEndStem(WorldGenUtilities.Context ctx) {
        return WorldGenUtilities.getBCLEndLevelStem(ctx, Optional.of(endVersion));
    }

    public BiomeSource fixBiomeSource(BiomeSource biomeSource, Set<Holder<Biome>> datapackBiomes) {
        if (biomeSource instanceof BCLibNetherBiomeSource bs) {
            return bs.createCopyForDatapack(datapackBiomes);
        }
        return biomeSource;
    }


    /**
     * Datapacks can change the world's generator. This Method will ensure, that the Generators contain
     * the correct BiomeSources for this world
     *
     * @param dimensionKey
     * @param dimensionTypeKey
     * @param settings
     * @return
     */
    public WorldGenSettings fixSettingsInCurrentWorld(RegistryAccess access, ResourceKey<LevelStem> dimensionKey,
                                                      ResourceKey<DimensionType> dimensionTypeKey,
                                                      WorldGenSettings settings) {
        var oldNether = settings.dimensions().getHolder(dimensionKey);
        int loaderVersion = WorldGenUtilities.getBiomeVersionForGenerator(oldNether
                .map(h -> h.value().generator())
                .orElse(null));

        int targetVersion = getVersion(dimensionKey);
        if (loaderVersion != targetVersion) {
            BCLib.LOGGER.info("Enforcing Correct Generator for " + dimensionKey.location().toString() + ".");
            var chunkGenerator = oldNether.map(h -> h.value().generator()).orElse(null);
            Optional<Holder<LevelStem>> refLevelStem = WorldGenUtilities.referenceStemForVersion(
                    dimensionKey,
                    targetVersion,
                    access,
                    settings.seed(),
                    settings.generateStructures(),
                    settings.generateBonusChest()
            );

            ChunkGenerator referenceGenerator = refLevelStem.map(h -> h.value().generator()).orElse(null);
            if (referenceGenerator == null) {
                BCLib.LOGGER.error("Failed to create Generator for " + dimensionKey.location().toString());
                return settings;
            }

            if (chunkGenerator instanceof ChunkGeneratorAccessor generator) {
                if (chunkGenerator instanceof NoiseGeneratorSettingsProvider noiseProvider) {
                    final Set<Holder<Biome>> biomes = chunkGenerator.getBiomeSource().possibleBiomes();

                    referenceGenerator = new BCLChunkGenerator(generator.bclib_getStructureSetsRegistry(),
                            noiseProvider.bclib_getNoises(),
                            fixBiomeSource(referenceGenerator.getBiomeSource(), biomes),
                            noiseProvider.bclib_getNoiseGeneratorSettingHolders());
                }
            }

            return WorldGenUtilities.replaceGenerator(dimensionKey,
                    dimensionTypeKey,
                    access,
                    settings,
                    referenceGenerator);
        }
        return settings;
    }

    public WorldGenSettings repairSettingsOnLoad(RegistryAccess registryAccess, WorldGenSettings settings) {
        settings = fixSettingsInCurrentWorld(registryAccess, LevelStem.NETHER, BuiltinDimensionTypes.NETHER, settings);
        settings = fixSettingsInCurrentWorld(registryAccess, LevelStem.END, BuiltinDimensionTypes.END, settings);
        return settings;
    }
}
