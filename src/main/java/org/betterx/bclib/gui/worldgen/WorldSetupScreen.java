package org.betterx.bclib.gui.worldgen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import org.betterx.bclib.gui.gridlayout.GridScreen;

import org.jetbrains.annotations.Nullable;

public class WorldSetupScreen  extends GridScreen {
    public WorldSetupScreen(@Nullable Screen parent) {
        super(parent, Component.translatable("title.screen.bclib.worldgen.main"), 10, false);
    }

    @Override
    protected void initLayout() {

    }

    private static WorldCreationContext.Updater worldConfiguration(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        return (frozen, worldGenSettings) -> {
            Registry<StructureSet> registry = frozen.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
            ChunkGenerator chunkGenerator = new FlatLevelSource(registry, flatLevelGeneratorSettings);
            return WorldGenSettings.replaceOverworldGenerator(frozen, worldGenSettings, chunkGenerator);
        };
    }
}
