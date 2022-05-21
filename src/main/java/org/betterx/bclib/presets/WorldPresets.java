package org.betterx.bclib.presets;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import com.mojang.datafixers.util.Pair;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.tag.TagAPI;
import org.betterx.bclib.api.tag.TagType;
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
                                                  Holder<NoiseGeneratorSettings> generatorSettings) {
        BCLibNetherBiomeSource netherSource = new BCLibNetherBiomeSource(biomes);
        LevelStem bclNether = new LevelStem(
                dimension,
                new NoiseBasedChunkGenerator(
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
                                               Holder<NoiseGeneratorSettings> generatorSettings) {
        BCLibEndBiomeSource netherSource = new BCLibEndBiomeSource(biomes);
        LevelStem bclEnd = new LevelStem(
                dimension,
                new NoiseBasedChunkGenerator(
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

    public static Optional<ResourceKey<WorldPreset>> DEFAULT = Optional.of(BCL_WORLD);

    public static WorldGenSettings createDefaultWorldFromPreset(RegistryAccess registryAccess,
                                                                long seed,
                                                                boolean generateStructures,
                                                                boolean generateBonusChest) {
        return registryAccess
                .registryOrThrow(Registry.WORLD_PRESET_REGISTRY)
                .getHolderOrThrow(DEFAULT.orElseThrow())
                .value()
                .createWorldGenSettings(seed, generateStructures, generateBonusChest);
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
        ResourceKey<WorldPreset> key = ResourceKey.create(Registry.WORLD_PRESET_REGISTRY, loc);
        WORLD_PRESETS.addUntyped(WorldPresetTags.NORMAL, key.location());

        return key;
    }

    public static void registerPresets() {
    }
}
