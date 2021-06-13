package ru.bclib.config;

import ru.bclib.BCLib;

public class Configs {
	public static final PathConfig RECIPE_CONFIG = new PathConfig(BCLib.MOD_ID, "recipes");
	
	public static void save() {
		RECIPE_CONFIG.saveChanges();
	}
}
