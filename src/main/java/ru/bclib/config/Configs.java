package ru.bclib.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.bclib.BCLib;

public class Configs {
	public static final PathConfig GENERATOR_CONFIG = new PathConfig(BCLib.MOD_ID, "generator");
	public static final PathConfig MAIN_CONFIG = new PathConfig(BCLib.MOD_ID, "main", true, true);
	public static final String MAIN_PATCH_CATEGORY = "patches";
	
	public static final PathConfig RECIPE_CONFIG = new PathConfig(BCLib.MOD_ID, "recipes");

	@Environment(EnvType.CLIENT)
	public static final PathConfig CLIENT_CONFIG = new PathConfig(BCLib.MOD_ID, "client", false);
	public static final PathConfig SERVER_CONFIG = new PathConfig(BCLib.MOD_ID, "server", false);
	
	public static void save() {
		MAIN_CONFIG.saveChanges();
		RECIPE_CONFIG.saveChanges();
	}
}
