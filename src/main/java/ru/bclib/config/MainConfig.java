package ru.bclib.config;

import ru.bclib.BCLib;
import ru.bclib.world.generator.GeneratorOptions;

public class MainConfig  extends NamedPathConfig {
	public static final ConfigToken<Boolean> APPLY_PATCHES = ConfigToken.Boolean(true, "applyPatches", Configs.MAIN_PATCH_CATEGORY);

	@ConfigUI(leftPadding = 8)
	public static final ConfigToken<Boolean> REPAIR_BIOMES = DependendConfigToken.Boolean(true, "fixBiomeSource",  Configs.MAIN_PATCH_CATEGORY, (config) -> config.get(MainConfig.APPLY_PATCHES));

	public boolean applyPatches() {
		return get(APPLY_PATCHES);
	}
	
	public MainConfig() {
		super(BCLib.MOD_ID, "main", true, true);
	}
}
