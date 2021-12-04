package ru.bclib;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import ru.bclib.api.TagAPI;
import ru.bclib.api.WorldDataAPI;
import ru.bclib.api.biomes.BiomeAPI;
import ru.bclib.api.biomes.SurfaceRuleBuilder;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.dataexchange.handler.autosync.Chunker;
import ru.bclib.api.dataexchange.handler.autosync.HelloClient;
import ru.bclib.api.dataexchange.handler.autosync.HelloServer;
import ru.bclib.api.dataexchange.handler.autosync.RequestFiles;
import ru.bclib.api.dataexchange.handler.autosync.SendFiles;
import ru.bclib.config.Configs;
import ru.bclib.recipes.CraftingRecipes;
import ru.bclib.registry.BaseBlockEntities;
import ru.bclib.registry.BaseRegistry;
import ru.bclib.util.Logger;
import ru.bclib.world.generator.BCLibEndBiomeSource;
import ru.bclib.world.generator.BCLibNetherBiomeSource;
import ru.bclib.world.generator.GeneratorOptions;

import java.util.List;

public class BCLib implements ModInitializer {
	public static final String MOD_ID = "bclib";
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
		DataExchangeAPI.registerMod(MOD_ID);
		
		DataExchangeAPI.registerDescriptors(List.of(
			HelloClient.DESCRIPTOR,
			HelloServer.DESCRIPTOR,
			RequestFiles.DESCRIPTOR,
			SendFiles.DESCRIPTOR,
			Chunker.DESCRIPTOR
		));
		
		BCLibPatch.register();
		Configs.save();
		
		RuleSource rule = SurfaceRuleBuilder
			.start()
			.biome(Biomes.END_HIGHLANDS)
			.filler(Blocks.STONE.defaultBlockState())
			.subsurface(Blocks.DIRT.defaultBlockState(), 3)
			.surface(Blocks.GRASS_BLOCK.defaultBlockState())
			.ceil(Blocks.DEEPSLATE.defaultBlockState())
			.build();
		BiomeAPI.addSurfaceRule(Biomes.END_HIGHLANDS.location(), rule);
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
