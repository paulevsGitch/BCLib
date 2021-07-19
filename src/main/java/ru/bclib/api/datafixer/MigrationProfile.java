package ru.bclib.api.datafixer;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import ru.bclib.config.Configs;
import ru.bclib.config.PathConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class MigrationProfile {
	final Set<String> mods;
	final Map<String, String> idReplacements;
	private final PathConfig config;
	
	MigrationProfile(PathConfig config) {
		this.config = config;
		
		this.mods = Collections.unmodifiableSet(Patch.getALL()
													 .stream()
													 .map(p -> p.modID)
													 .collect(Collectors.toSet()));
		
		HashMap<String, String> replacements = new HashMap<String, String>();
		for (String modID : mods) {
			Patch.getALL()
				 .stream()
				 .filter(p -> p.modID.equals(modID))
				 .forEach(patch -> {
					 if (currentPatchLevel(modID) < patch.level) {
						 replacements.putAll(patch.getIDReplacements());
						 DataFixerAPI.LOGGER.info("Applying " + patch);
					 }
					 else {
						 DataFixerAPI.LOGGER.info("Ignoring " + patch);
					 }
				 });
		}
		
		this.idReplacements = Collections.unmodifiableMap(replacements);
	}
	
	final public void markApplied() {
		for (String modID : mods) {
			DataFixerAPI.LOGGER.info("Updating Patch-Level for '{}' from {} to {}", modID, currentPatchLevel(modID), Patch.maxPatchLevel(modID));
			config.setString(Configs.MAIN_PATCH_CATEGORY, modID, Patch.maxPatchVersion(modID));
		}
		
		config.saveChanges();
	}
	
	public String currentPatchVersion(@NotNull String modID) {
		return config.getString(Configs.MAIN_PATCH_CATEGORY, modID, "0.0.0");
	}
	
	public int currentPatchLevel(@NotNull String modID) {
		return DataFixerAPI.getModVersion(currentPatchVersion(modID));
	}
	
	public boolean hasAnyFixes() {
		return idReplacements.size() > 0;
	}
	
	public boolean replaceStringFromIDs(@NotNull CompoundTag tag, @NotNull String key) {
		if (!tag.contains(key)) return false;
		
		final String val = tag.getString(key);
		final String replace = idReplacements.get(val);
		
		if (replace != null) {
			DataFixerAPI.LOGGER.warning("Replacing ID '{}' with '{}'.", val, replace);
			tag.putString(key, replace);
			return true;
		}
		
		return false;
	}
}
