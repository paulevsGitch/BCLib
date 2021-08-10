package ru.bclib.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.bclib.BCLib;

public class Configs {
	public static final PathConfig MAIN_CONFIG = new PathConfig(BCLib.MOD_ID, "main");
	public static final String MAIN_PATCH_CATEGORY = "patches";
	public static final String MAIN_SYNC_CATEGORY = "client_sync";
	
	public static final PathConfig RECIPE_CONFIG = new PathConfig(BCLib.MOD_ID, "recipes");

	@Environment(EnvType.CLIENT)
	public static final PathConfig CLIENT_CONFIG = new PathConfig(BCLib.MOD_ID, "client");
	
	public static void save() {
		MAIN_CONFIG.saveChanges();
		RECIPE_CONFIG.saveChanges();

		if (BCLib.isClient()) {
			CLIENT_CONFIG.saveChanges();
		}
	}
}
