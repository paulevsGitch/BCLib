package org.betterx.bclib.presets.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.WorldDataAPI;
import org.betterx.bclib.interfaces.NoiseGeneratorSettingsProvider;
import org.betterx.bclib.util.ModUtil;
import org.betterx.bclib.world.generator.BCLBiomeSource;

import java.util.Optional;

public class BCLChunkGenerator extends NoiseBasedChunkGenerator {

    private static String TAG_GENERATOR = "generator";
    private static final String TAG_VERSION = "version";
    private static final String TAG_BN_GEN_VERSION = "generator_version";

    public static final Codec<BCLChunkGenerator> CODEC = RecordCodecBuilder
            .create((RecordCodecBuilder.Instance<BCLChunkGenerator> builderInstance) -> {
                final RecordCodecBuilder<BCLChunkGenerator, Registry<NormalNoise.NoiseParameters>> noiseGetter = RegistryOps
                        .retrieveRegistry(
                                Registry.NOISE_REGISTRY)
                        .forGetter(
                                BCLChunkGenerator::getNoises);

                RecordCodecBuilder<BCLChunkGenerator, BiomeSource> biomeSourceCodec = BiomeSource.CODEC
                        .fieldOf("biome_source")
                        .forGetter((BCLChunkGenerator generator) -> generator.biomeSource);

                RecordCodecBuilder<BCLChunkGenerator, Holder<NoiseGeneratorSettings>> settingsCodec = NoiseGeneratorSettings.CODEC
                        .fieldOf("settings")
                        .forGetter((BCLChunkGenerator generator) -> generator.settings);


                return NoiseBasedChunkGenerator
                        .commonCodec(builderInstance)
                        .and(builderInstance.group(noiseGetter, biomeSourceCodec, settingsCodec))
                        .apply(builderInstance, builderInstance.stable(BCLChunkGenerator::new));
            });

    public BCLChunkGenerator(Registry<StructureSet> registry,
                             Registry<NormalNoise.NoiseParameters> registry2,
                             BiomeSource biomeSource,
                             Holder<NoiseGeneratorSettings> holder) {
        super(registry, registry2, biomeSource, holder);
        System.out.println("Chunk Generator: " + this);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }


    private Registry<NormalNoise.NoiseParameters> getNoises() {
        if (this instanceof NoiseGeneratorSettingsProvider p) {
            return p.bclib_getNoises();
        }
        return null;
    }

    public static int getBiomeVersionForGenerator(ChunkGenerator generator) {
        if (generator == null) return BCLBiomeSource.getVersionBiomeSource(null);
        return BCLBiomeSource.getVersionBiomeSource(generator.getBiomeSource());
    }

    public static Optional<Holder<LevelStem>> referenceStemForVersion(
            ResourceKey<LevelStem> dimensionKey,
            int biomeSourceVersion,
            RegistryAccess registryAccess,
            long seed,
            boolean generateStructures,
            boolean generateBonusChest
    ) {
        final WorldGenSettings referenceSettings;
        if (biomeSourceVersion == BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA) {
            referenceSettings = net.minecraft.world.level.levelgen.presets.WorldPresets.createNormalWorldFromPreset(
                    registryAccess,
                    seed,
                    generateStructures,
                    generateBonusChest);
        } else if (biomeSourceVersion == BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE) {
            referenceSettings = WorldPresets.createWorldFromPreset(
                    WorldPresets.BCL_WORLD_17,
                    registryAccess,
                    seed,
                    generateStructures,
                    generateBonusChest);
        } else {
            referenceSettings = WorldPresets.createDefaultWorldFromPreset(
                    registryAccess,
                    seed,
                    generateStructures,
                    generateBonusChest);
        }
        return referenceSettings.dimensions().getHolder(dimensionKey);
    }

    public static int getBiomeVersionForCurrentWorld(ResourceKey<LevelStem> key) {
        final CompoundTag settingsNbt = getSettingsNbt();
        if (!settingsNbt.contains(key.location().toString())) return BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION;
        return settingsNbt.getInt(key.location().toString());
    }

    private static void writeDimensionVersion(WorldGenSettings settings,
                                              CompoundTag generatorSettings,
                                              ResourceKey<LevelStem> key) {
        var dimension = settings.dimensions().getHolder(key);
        if (dimension.isPresent()) {
            generatorSettings.putInt(key.location().toString(),
                    getBiomeVersionForGenerator(dimension.get().value().generator()));
        } else {
            generatorSettings.putInt(key.location().toString(), getBiomeVersionForGenerator(null));
        }
    }

    public static void initializeWorldData(WorldGenSettings settings) {
        final CompoundTag settingsNbt = getSettingsNbt();
        writeDimensionVersion(settings, settingsNbt, LevelStem.NETHER);
        writeDimensionVersion(settings, settingsNbt, LevelStem.END);
    }

    private static CompoundTag getSettingsNbt() {
        return WorldDataAPI.getCompoundTag(BCLib.MOD_ID, TAG_GENERATOR);
    }

    public static void migrateGeneratorSettings() {
        final CompoundTag settingsNbt = getSettingsNbt();

        if (settingsNbt.size() == 0) {
            BCLib.LOGGER.info("Found World without generator Settings. Setting up data...");
            int biomeSourceVersion = BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION;

            final CompoundTag bclRoot = WorldDataAPI.getRootTag(BCLib.MOD_ID);

            String bclVersion = "0.0.0";
            if (bclRoot.contains(TAG_VERSION)) {
                bclVersion = bclRoot.getString(TAG_VERSION);
            }
            boolean isPre18 = !ModUtil.isLargerOrEqualVersion(bclVersion, "1.0.0");

            if (isPre18) {
                BCLib.LOGGER.info("World was create pre 1.18!");
                biomeSourceVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE;
            }

            if (WorldDataAPI.hasMod("betternether")) {
                BCLib.LOGGER.info("Found Data from BetterNether, using for migration.");
                final CompoundTag bnRoot = WorldDataAPI.getRootTag("betternether");
                biomeSourceVersion = "1.17".equals(bnRoot.getString(TAG_BN_GEN_VERSION))
                        ? BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE
                        : BCLBiomeSource.BIOME_SOURCE_VERSION_HEX;
            }

            BCLib.LOGGER.info("Set world to BiomeSource Version " + biomeSourceVersion);
            settingsNbt.putInt(LevelStem.NETHER.location().toString(), biomeSourceVersion);
            settingsNbt.putInt(LevelStem.END.location().toString(), biomeSourceVersion);

            WorldDataAPI.saveFile(BCLib.MOD_ID);
        }
    }

    @Override
    public String toString() {
        return "BCLib - Chunk Generator (" + Integer.toHexString(hashCode()) + ")";
    }
}
