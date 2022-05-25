package org.betterx.bclib.gui.screens;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.gui.gridlayout.GridCheckboxCell;
import org.betterx.bclib.gui.gridlayout.GridLayout;
import org.betterx.bclib.api.worldgen.WorldGenUtil;
import org.betterx.bclib.world.generator.BCLBiomeSource;

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
        final int netherVersion = WorldGenUtil.getBiomeVersionForGenerator(context
                .worldGenSettings()
                .dimensions()
                .getOrCreateHolderOrThrow(
                        LevelStem.NETHER)
                .value()
                .generator());
        final int endVersion = WorldGenUtil.getBiomeVersionForGenerator(context
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
                endVersion == BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                });
        bclibNether = mainSettingsRow.addCheckbox(Component.translatable(
                        "title.screen.bclib.worldgen.custom_biome_source"),
                netherVersion != BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
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
                endVersion == BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                });

        bclibEnd = mainSettingsRow.addCheckbox(Component.translatable(
                        "title.screen.bclib.worldgen.custom_biome_source"),
                endVersion != BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
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
        int endVersion = BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION;
        if (bclibEnd.isChecked()) {
            if (endLegacy.isChecked()) endVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE;
            else endVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_HEX;
        } else {
            endVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA;
        }

        int netherVersion = BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION;
        if (bclibNether.isChecked()) {
            if (netherLegacy.isChecked()) netherVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE;
            else netherVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_HEX;
        } else {
            netherVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA;
        }

        BCLib.LOGGER.info("Custom World Versions: end=" + endVersion + ", nether=" + netherVersion);
        updateConfiguration(LevelStem.END, BuiltinDimensionTypes.END, endVersion);
        updateConfiguration(LevelStem.NETHER, BuiltinDimensionTypes.NETHER, netherVersion);
    }


    private void updateConfiguration(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            int biomeSourceVersion
    ) {
        createWorldScreen.worldGenSettingsComponent.updateSettings(
                (registryAccess, worldGenSettings) -> WorldGenUtil.replaceGenerator(
                        dimensionKey,
                        dimensionTypeKey,
                        biomeSourceVersion,
                        registryAccess,
                        worldGenSettings
                )
        );
    }


}
