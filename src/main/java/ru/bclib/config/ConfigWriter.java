package ru.bclib.config;

import java.io.File;
import java.nio.file.Path;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.loader.api.FabricLoader;
import ru.bclib.util.JsonFactory;

public class ConfigWriter {
	private final static Path GAME_CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

	private final File configFile;
	private JsonObject configObject;

	public ConfigWriter(String modID, String configFile) {
		this.configFile = new File(new File(GAME_CONFIG_DIR.toFile(), modID), configFile + ".json");
		File parent = this.configFile.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		this.load();
	}

	public JsonObject getConfig() {
		return configObject;
	}

	public void save() {
		if (configObject == null) {
			return;
		}
		save(configFile, configObject);
	}

	public JsonObject load() {
		if (configObject == null) {
			configObject = load(configFile);
		}
		return configObject;
	}

	public void save(JsonElement config) {
		this.configObject = config.getAsJsonObject();
		save(configFile, config);
	}

	public static JsonObject load(File configFile) {
		return JsonFactory.getJsonObject(configFile);
	}

	public static void save(File configFile, JsonElement config) {
		JsonFactory.storeJson(configFile, config);
	}

	public static String scrubFileName(String input) {
		input = input.replaceAll("[/\\ ]+", "_");
		input = input.replaceAll("[,:&\"\\|\\<\\>\\?\\*]", "_");
		return input;
	}
}
