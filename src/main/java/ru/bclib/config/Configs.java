package ru.bclib.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.bclib.BCLib;
import ru.bclib.api.dataexchange.handler.autosync.AutoSync.ClientConfig;
import ru.bclib.api.dataexchange.handler.autosync.AutoSync.ServerConfig;

public class Configs {
	public static final PathConfig GENERATOR_CONFIG = new PathConfig(BCLib.MOD_ID, "generator");
	public static final PathConfig MAIN_CONFIG = new PathConfig(BCLib.MOD_ID, "main", true, true);
	public static final String MAIN_PATCH_CATEGORY = "patches";
	
	public static final PathConfig RECIPE_CONFIG = new PathConfig(BCLib.MOD_ID, "recipes");

	@Environment(EnvType.CLIENT)
	public static final ClientConfig CLIENT_CONFIG = new ClientConfig();
	public static final ServerConfig SERVER_CONFIG = new ServerConfig();
	
	public static void save() {
		MAIN_CONFIG.saveChanges();
		RECIPE_CONFIG.saveChanges();
	}
}
