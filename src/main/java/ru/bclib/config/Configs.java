package ru.bclib.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.bclib.BCLib;

public class Configs {
	// Client and Server-Config must be the first entries. They are not part of the Auto-Sync process
	// But will be needed by other Auto-Sync Config-Files
	@Environment(EnvType.CLIENT)
	public static final ClientConfig CLIENT_CONFIG = new ClientConfig();
	public static final ServerConfig SERVER_CONFIG = new ServerConfig();
	
	
	public static final PathConfig GENERATOR_CONFIG = new PathConfig(BCLib.MOD_ID, "generator");
	public static final PathConfig MAIN_CONFIG = new PathConfig(BCLib.MOD_ID, "main", true, true);
	public static final String MAIN_PATCH_CATEGORY = "patches";
	
	public static final PathConfig RECIPE_CONFIG = new PathConfig(BCLib.MOD_ID, "recipes");

	
	
	public static void save() {
		MAIN_CONFIG.saveChanges();
		RECIPE_CONFIG.saveChanges();
	}
}
