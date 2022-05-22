package org.betterx.bclib.gui.screens;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.gui.gridlayout.GridCheckboxCell;
import org.betterx.bclib.gui.gridlayout.GridLayout;
import org.betterx.bclib.presets.worldgen.BCLChunkGenerator;

import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WorldSetupScreen extends BCLibScreen {
    private final WorldCreationContext context;
    private final CreateWorldScreen createWorldScreen;

    public WorldSetupScreen(@Nullable CreateWorldScreen parent, WorldCreationContext context) {
        super(parent, Component.translatable("title.screen.bclib.worldgen.main"), 10, false);
        this.context = context;
        this.createWorldScreen = parent;
    }


    private GridCheckboxCell bclibEnd;
    private GridCheckboxCell bclibNether;
    GridCheckboxCell endLegacy;
    GridCheckboxCell netherLegacy;

    @Override
    protected void initLayout() {
        final int netherVersion = BCLChunkGenerator.getBiomeVersionForGenerator(context
                .worldGenSettings()
                .dimensions()
                .getOrCreateHolderOrThrow(
                        LevelStem.NETHER)
                .value()
                .generator());
        final int endVersion = BCLChunkGenerator.getBiomeVersionForGenerator(context
                .worldGenSettings()
                .dimensions()
                .getOrCreateHolderOrThrow(
                        LevelStem.END)
                .value()
                .generator());

        final int BUTTON_HEIGHT = 20;
        grid.addSpacerRow(20);

        var row = grid.addRow();
        var colNether = row.addColumn(0.5, GridLayout.GridValueType.PERCENTAGE);
        var colEnd = row.addColumn(0.5, GridLayout.GridValueType.PERCENTAGE);

        row = colNether.addRow();
        row.addString(Component.translatable("title.bclib.the_nether"), GridLayout.Alignment.CENTER, this);
        colNether.addSpacerRow(15);

        var mainSettingsRow = colNether.addRow();
        mainSettingsRow.addSpacer(16);
        row = colNether.addRow();
        row.addSpacer(32);
        netherLegacy = row.addCheckbox(Component.translatable("title.screen.bclib.worldgen.legacy_square"),
                endVersion == BCLChunkGenerator.BIOME_SOURCE_VERSION_SQUARE,
                font,
                (state) -> {
                });
        bclibNether = mainSettingsRow.addCheckbox(Component.translatable(
                        "title.screen.bclib.worldgen.custom_biome_source"),
                netherVersion != BCLChunkGenerator.BIOME_SOURCE_VERSION_VANILLA,
                font,
                (state) -> {
                    netherLegacy.setEnabled(state);
                });


        row = colEnd.addRow(GridLayout.VerticalAlignment.CENTER);
        row.addString(Component.translatable("title.bclib.the_end"), GridLayout.Alignment.CENTER, this);
        colEnd.addSpacerRow(15);

        mainSettingsRow = colEnd.addRow();
        mainSettingsRow.addSpacer(16);
        row = colEnd.addRow();
        row.addSpacer(32);

        endLegacy = row.addCheckbox(Component.translatable("title.screen.bclib.worldgen.legacy_square"),
                endVersion == BCLChunkGenerator.BIOME_SOURCE_VERSION_SQUARE,
                font,
                (state) -> {
                });

        bclibEnd = mainSettingsRow.addCheckbox(Component.translatable(
                        "title.screen.bclib.worldgen.custom_biome_source"),
                endVersion != BCLChunkGenerator.BIOME_SOURCE_VERSION_VANILLA,
                font,
                (state) -> {
                    endLegacy.setEnabled(state);
                });

        grid.addSpacerRow(15);
        row = grid.addRow();
        row.addFiller();
        row.addButton(CommonComponents.GUI_DONE, BUTTON_HEIGHT, font, (button) -> {
            updateSettings();
            onClose();
        });
        grid.addSpacerRow(10);
    }

    private void updateSettings() {
        int endVersion = BCLChunkGenerator.DEFAULT_BIOME_SOURCE_VERSION;
        if (bclibEnd.isChecked()) {
            if (endLegacy.isChecked()) endVersion = BCLChunkGenerator.BIOME_SOURCE_VERSION_SQUARE;
            else endVersion = BCLChunkGenerator.BIOME_SOURCE_VERSION_HEX;
        } else {
            endVersion = BCLChunkGenerator.BIOME_SOURCE_VERSION_VANILLA;
        }

        int netherVersion = BCLChunkGenerator.DEFAULT_BIOME_SOURCE_VERSION;
        if (bclibNether.isChecked()) {
            if (netherLegacy.isChecked()) netherVersion = BCLChunkGenerator.BIOME_SOURCE_VERSION_SQUARE;
            else netherVersion = BCLChunkGenerator.BIOME_SOURCE_VERSION_HEX;
        } else {
            netherVersion = BCLChunkGenerator.BIOME_SOURCE_VERSION_VANILLA;
        }

        BCLib.LOGGER.info("Custom World Versions: end=" + endVersion + ", nether=" + netherVersion);
    }

    private WorldCreationContext.Updater worldConfiguration(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {

        return (frozen, worldGenSettings) -> {
            Registry<StructureSet> registry = frozen.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
            ChunkGenerator chunkGenerator = new FlatLevelSource(registry, flatLevelGeneratorSettings);
            return WorldGenSettings.replaceOverworldGenerator(frozen, worldGenSettings, chunkGenerator);
        };
    }
}
