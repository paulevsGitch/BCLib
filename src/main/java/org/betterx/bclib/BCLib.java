package org.betterx.bclib;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import org.betterx.bclib.api.WorldDataAPI;
import org.betterx.bclib.api.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.dataexchange.handler.autosync.*;
import org.betterx.bclib.api.tag.TagAPI;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.presets.worldgen.BCLWorldPresets;
import org.betterx.bclib.recipes.AnvilRecipe;
import org.betterx.bclib.recipes.CraftingRecipes;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.bclib.registry.BaseRegistry;
import org.betterx.bclib.util.Logger;
import org.betterx.bclib.world.generator.BCLibEndBiomeSource;
import org.betterx.bclib.world.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.world.generator.GeneratorOptions;

import java.util.List;

public class BCLib implements ModInitializer {
    public static final String MOD_ID = "bclib";
    public static final String TOGETHER_WORLDS = "worlds_together";
    public static final Logger LOGGER = new Logger(MOD_ID);

    @Override
    public void onInitialize() {
        BaseRegistry.register();
        GeneratorOptions.init();
        BaseBlockEntities.register();
        BCLibEndBiomeSource.register();
        BCLibNetherBiomeSource.register();
        TagAPI.init();
        CraftingRecipes.init();
        WorldDataAPI.registerModCache(MOD_ID);
        WorldDataAPI.registerModCache(TOGETHER_WORLDS);
        DataExchangeAPI.registerMod(MOD_ID);
        BCLWorldPresets.registerPresets();
        AnvilRecipe.register();

        DataExchangeAPI.registerDescriptors(List.of(
                HelloClient.DESCRIPTOR,
                HelloServer.DESCRIPTOR,
                RequestFiles.DESCRIPTOR,
                SendFiles.DESCRIPTOR,
                Chunker.DESCRIPTOR
        ));

        BCLibPatch.register();
        Configs.save();
    }

    public static boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static ResourceLocation makeID(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
