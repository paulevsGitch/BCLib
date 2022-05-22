package org.betterx.bclib.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import org.betterx.bclib.world.generator.BCLBiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import java.util.Optional;

@Mixin(WorldPresets.Bootstrap.class)
public abstract class WorldPresetsBootstrapMixin {
    @Shadow
    @Final
    private Registry<WorldPreset> presets;
    @Shadow
    @Final
    private Registry<Biome> biomes;
    @Shadow
    @Final
    private Registry<StructureSet> structureSets;
    @Shadow
    @Final
    private Registry<NormalNoise.NoiseParameters> noises;
    @Shadow
    @Final
    private Holder<DimensionType> netherDimensionType;
    @Shadow
    @Final
    private Holder<NoiseGeneratorSettings> netherNoiseSettings;
    @Shadow
    @Final
    private Holder<DimensionType> endDimensionType;
    @Shadow
    @Final
    private Holder<NoiseGeneratorSettings> endNoiseSettings;

    //see WorldPresets.register

    @ModifyArg(method = "run", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/level/levelgen/presets/WorldPresets$Bootstrap;registerCustomOverworldPreset(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;)Lnet/minecraft/core/Holder;"))
    private LevelStem bcl_getOverworldStem(LevelStem overworldStem) {
        WorldPreset preset_18 = new org.betterx.bclib.presets.worldgen.WorldPresets.SortableWorldPreset(
                Map.of(LevelStem.OVERWORLD,
                        overworldStem,
                        LevelStem.NETHER,
                        org.betterx.bclib.presets.worldgen.WorldPresets.getBCLNetherLevelStem(this.biomes,
                                this.netherDimensionType,
                                this.structureSets,
                                this.noises,
                                this.netherNoiseSettings,
                                Optional.empty()),
                        LevelStem.END,
                        org.betterx.bclib.presets.worldgen.WorldPresets.getBCLEndLevelStem(this.biomes,
                                this.endDimensionType,
                                this.structureSets,
                                this.noises,
                                this.endNoiseSettings,
                                Optional.empty())
                ), 0
        );

        WorldPreset preset_17 = new org.betterx.bclib.presets.worldgen.WorldPresets.SortableWorldPreset(
                Map.of(LevelStem.OVERWORLD,
                        overworldStem,
                        LevelStem.NETHER,
                        org.betterx.bclib.presets.worldgen.WorldPresets.getBCLNetherLevelStem(this.biomes,
                                this.netherDimensionType,
                                this.structureSets,
                                this.noises,
                                this.netherNoiseSettings,
                                Optional.of(BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE)),
                        LevelStem.END,
                        org.betterx.bclib.presets.worldgen.WorldPresets.getBCLEndLevelStem(this.biomes,
                                this.endDimensionType,
                                this.structureSets,
                                this.noises,
                                this.endNoiseSettings,
                                Optional.of(BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE))
                ), 0
        );

        BuiltinRegistries.register(this.presets, org.betterx.bclib.presets.worldgen.WorldPresets.BCL_WORLD, preset_18);
        BuiltinRegistries.register(this.presets,
                org.betterx.bclib.presets.worldgen.WorldPresets.BCL_WORLD_17,
                preset_17);

        return overworldStem;
    }

}