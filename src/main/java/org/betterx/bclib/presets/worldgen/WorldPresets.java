package org.betterx.bclib.presets.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.tag.TagAPI;
import org.betterx.bclib.api.tag.TagType;
import org.betterx.bclib.world.generator.BCLBiomeSource;
import org.betterx.bclib.world.generator.BCLibEndBiomeSource;
import org.betterx.bclib.world.generator.BCLibNetherBiomeSource;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class WorldPresets {
    @NotNull
    public static LevelStem getBCLNetherLevelStem(Registry<Biome> biomes,
                                                  Holder<DimensionType> dimension,
                                                  Registry<StructureSet> structureSets,
                                                  Registry<NormalNoise.NoiseParameters> noiseParameters,
                                                  Holder<NoiseGeneratorSettings> generatorSettings,
                                                  Optional<Integer> version) {
        BCLibNetherBiomeSource netherSource = new BCLibNetherBiomeSource(biomes, version);
        LevelStem bclNether = new LevelStem(
                dimension,
                new BCLChunkGenerator(
                        structureSets,
                        noiseParameters,
                        netherSource,
                        generatorSettings)
        );
        return bclNether;
    }

    @NotNull
    public static LevelStem getBCLEndLevelStem(Registry<Biome> biomes,
                                               Holder<DimensionType> dimension,
                                               Registry<StructureSet> structureSets,
                                               Registry<NormalNoise.NoiseParameters> noiseParameters,
                                               Holder<NoiseGeneratorSettings> generatorSettings,
                                               Optional<Integer> version) {
        BCLibEndBiomeSource netherSource = new BCLibEndBiomeSource(biomes, version);
        LevelStem bclEnd = new LevelStem(
                dimension,
                new BCLChunkGenerator(
                        structureSets,
                        noiseParameters,
                        netherSource,
                        generatorSettings)
        );
        return bclEnd;
    }

    public static class SortableWorldPreset extends WorldPreset {
        public final int sortOrder;

        public SortableWorldPreset(Map<ResourceKey<LevelStem>, LevelStem> map, int sortOrder) {
            super(map);
            this.sortOrder = sortOrder;
        }
    }

    public static final TagType.Simple<WorldPreset> WORLD_PRESETS =
            TagAPI.registerType(BuiltinRegistries.WORLD_PRESET, "tags/worldgen/world_preset");

    public static final ResourceKey<WorldPreset> BCL_WORLD = register(BCLib.makeID("normal"));
    public static final ResourceKey<WorldPreset> BCL_WORLD_17 = register(BCLib.makeID("legacy_17"), false);

    public static Optional<ResourceKey<WorldPreset>> DEFAULT = Optional.of(BCL_WORLD);

    public static WorldGenSettings createWorldFromPreset(ResourceKey<WorldPreset> preset,
                                                         RegistryAccess registryAccess,
                                                         long seed,
                                                         boolean generateStructures,
                                                         boolean generateBonusChest) {
        WorldGenSettings settings = registryAccess
                .registryOrThrow(Registry.WORLD_PRESET_REGISTRY)
                .getHolderOrThrow(preset)
                .value()
                .createWorldGenSettings(seed, generateStructures, generateBonusChest);

        for (LevelStem stem : settings.dimensions()) {
            if (stem.generator().getBiomeSource() instanceof BCLBiomeSource bcl) {
                bcl.setSeed(seed);
            }
        }

        return settings;
    }

    public static WorldGenSettings createDefaultWorldFromPreset(RegistryAccess registryAccess,
                                                                long seed,
                                                                boolean generateStructures,
                                                                boolean generateBonusChest) {
        return createWorldFromPreset(DEFAULT.orElseThrow(),
                registryAccess,
                seed,
                generateStructures,
                generateBonusChest);
    }

    public static Pair<WorldGenSettings, RegistryAccess.Frozen> defaultWorldDataSupplier(RegistryAccess.Frozen frozen) {
        WorldGenSettings worldGenSettings = createDefaultWorldFromPreset(frozen);
        return Pair.of(worldGenSettings, frozen);
    }

    public static WorldGenSettings createDefaultWorldFromPreset(RegistryAccess registryAccess, long seed) {
        return createDefaultWorldFromPreset(registryAccess, seed, true, false);
    }

    public static WorldGenSettings createDefaultWorldFromPreset(RegistryAccess registryAccess) {
        return createDefaultWorldFromPreset(registryAccess, RandomSource.create().nextLong());
    }

    public static WorldGenSettings replaceGenerator(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            int biomeSourceVersion,
            RegistryAccess registryAccess,
            WorldGenSettings worldGenSettings
    ) {
        Optional<Holder<LevelStem>> oLevelStem = BCLChunkGenerator.referenceStemForVersion(
                dimensionKey,
                biomeSourceVersion,
                registryAccess,
                worldGenSettings.seed(),
                worldGenSettings.generateStructures(),
                worldGenSettings.generateStructures()
        );

        Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<LevelStem> registry2 = withDimension(dimensionKey, dimensionTypeKey, registry,
                worldGenSettings.dimensions(),
                oLevelStem.map(l -> l.value().generator()).orElseThrow());
        return new WorldGenSettings(worldGenSettings.seed(),
                worldGenSettings.generateStructures(),
                worldGenSettings.generateBonusChest(),
                registry2);
    }

    public static Registry<LevelStem> withDimension(ResourceKey<LevelStem> dimensionKey,
                                                    ResourceKey<DimensionType> dimensionTypeKey,
                                                    Registry<DimensionType> registry,
                                                    Registry<LevelStem> registry2,
                                                    ChunkGenerator chunkGenerator) {
        LevelStem levelStem = registry2.get(dimensionKey);
        Holder<DimensionType> holder = levelStem == null
                ? registry.getOrCreateHolderOrThrow(dimensionTypeKey)
                : levelStem.typeHolder();
        return withDimension(dimensionKey, registry2, holder, chunkGenerator);
    }

    public static Registry<LevelStem> withDimension(ResourceKey<LevelStem> dimensionKey, Registry<LevelStem> registry,
                                                    Holder<DimensionType> holder,
                                                    ChunkGenerator chunkGenerator) {
        MappedRegistry<LevelStem> writableRegistry = new MappedRegistry<LevelStem>(Registry.LEVEL_STEM_REGISTRY,
                Lifecycle.experimental(),
                null);
        writableRegistry.register(dimensionKey,
                new LevelStem(holder, chunkGenerator),
                Lifecycle.stable());
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
            ResourceKey<LevelStem> resourceKey = entry.getKey();
            if (resourceKey == dimensionKey) continue;
            writableRegistry.register(resourceKey,
                    entry.getValue(),
                    registry.lifecycle(entry.getValue()));
        }
        return writableRegistry;
    }

    /**
     * Registers a custom WorldPreset (with custom rules and behaviour)
     * <p>
     * See also {@link org.betterx.bclib.client.presets.WorldPresetsUI} if you need to add a Customize Button/Screen
     * for your preset
     *
     * @param loc The ID of your Preset
     * @return The key you may use to reference your new Preset
     */
    public static ResourceKey<WorldPreset> register(ResourceLocation loc) {
        return register(loc, true);
    }

    private static ResourceKey<WorldPreset> register(ResourceLocation loc, boolean addToNormal) {
        ResourceKey<WorldPreset> key = ResourceKey.create(Registry.WORLD_PRESET_REGISTRY, loc);
        if (addToNormal) {
            WORLD_PRESETS.addUntyped(WorldPresetTags.NORMAL, key.location());
        }

        return key;
    }

    public static void registerPresets() {
    }
}
