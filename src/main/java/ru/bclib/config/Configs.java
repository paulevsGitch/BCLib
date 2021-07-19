package ru.bclib.config;

import ru.bclib.BCLib;

public class Configs {
	public static final PathConfig MAIN_CONFIG = new PathConfig(BCLib.MOD_ID, "main");
	public static final String MAIN_PATCH_CATEGORY = "patches";
	
	public static final PathConfig RECIPE_CONFIG = new PathConfig(BCLib.MOD_ID, "recipes");
	
	public static void save() {
		MAIN_CONFIG.saveChanges();
		RECIPE_CONFIG.saveChanges();
	}
}
