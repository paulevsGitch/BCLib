package org.betterx.bclib.gui.worldgen;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.betterx.bclib.gui.gridlayout.GridLayout;
import org.betterx.bclib.gui.gridlayout.GridScreen;

import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WorldSetupScreen extends GridScreen {
    private final WorldCreationContext context;
    private final CreateWorldScreen createWorldScreen;

    public WorldSetupScreen(@Nullable CreateWorldScreen parent, WorldCreationContext context) {
        super(parent, Component.translatable("title.screen.bclib.worldgen.main"), 10, false);
        this.context = context;
        this.createWorldScreen = parent;
    }

    @Override
    protected void initLayout() {
        final int BUTTON_HEIGHT = 20;
        grid.addSpacerRow();

        var row = grid.addRow();
        var colNether = row.addColumn(0.5, GridLayout.GridValueType.PERCENTAGE);
        var colEnd = row.addColumn(0.5, GridLayout.GridValueType.PERCENTAGE);

        row = colNether.addRow(GridLayout.VerticalAlignment.CENTER);
        row.addString(Component.literal("The Nether"), GridLayout.Alignment.CENTER, this);

        row = colEnd.addRow(GridLayout.VerticalAlignment.CENTER);
        row.addString(Component.literal("The End"), GridLayout.Alignment.CENTER, this);

        grid.addSpacerRow(15);
        row = grid.addRow();
        row.addFiller();
        row.addButton(CommonComponents.GUI_DONE, BUTTON_HEIGHT, font, (button) -> {
            //TODO: update settings
            onClose();
        });
        grid.addSpacerRow(10);
    }

    private void updateSettings() {

    }

    private static WorldCreationContext.Updater worldConfiguration(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        return (frozen, worldGenSettings) -> {
            Registry<StructureSet> registry = frozen.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
            ChunkGenerator chunkGenerator = new FlatLevelSource(registry, flatLevelGeneratorSettings);
            return WorldGenSettings.replaceOverworldGenerator(frozen, worldGenSettings, chunkGenerator);
        };
    }
}
