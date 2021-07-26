package ru.bclib.api.datafixer;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class MigrationProfile {
	final Set<String> mods;
	final Map<String, String> idReplacements;
	final List<PatchFunction<CompoundTag, Boolean>> levelPatchers;
	
	private final CompoundTag config;
	
	MigrationProfile(CompoundTag config) {
		this.config = config;
		
		this.mods = Collections.unmodifiableSet(Patch.getALL()
													 .stream()
													 .map(p -> p.modID)
													 .collect(Collectors.toSet()));
		
		HashMap<String, String> replacements = new HashMap<String, String>();
		List<PatchFunction<CompoundTag, Boolean>> levelPatches = new LinkedList<>();
		for (String modID : mods) {
			Patch.getALL()
				 .stream()
				 .filter(p -> p.modID.equals(modID))
				 .forEach(patch -> {
					 if (currentPatchLevel(modID) < patch.level) {
						 replacements.putAll(patch.getIDReplacements());
						 if (patch.getLevelDatPatcher()!=null)
							 levelPatches.add(patch.getLevelDatPatcher());
						 DataFixerAPI.LOGGER.info("Applying " + patch);
					 }
					 else {
						 DataFixerAPI.LOGGER.info("Ignoring " + patch);
					 }
				 });
		}
		
		this.idReplacements = Collections.unmodifiableMap(replacements);
		this.levelPatchers = Collections.unmodifiableList(levelPatches);
	}
	
	final public void markApplied() {
		for (String modID : mods) {
			DataFixerAPI.LOGGER.info("Updating Patch-Level for '{}' from {} to {}", modID, currentPatchLevel(modID), Patch.maxPatchLevel(modID));
			config.putString(modID, Patch.maxPatchVersion(modID));
		}
	}
	
	public String currentPatchVersion(@NotNull String modID) {
		if (!config.contains(modID)) return "0.0.0";
		return config.getString(modID);
	}
	
	public int currentPatchLevel(@NotNull String modID) {
		return DataFixerAPI.getModVersion(currentPatchVersion(modID));
	}
	
	public boolean hasAnyFixes() {
		return idReplacements.size() > 0 || levelPatchers.size() > 0;
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
	
	public boolean patchLevelDat(@NotNull CompoundTag level) throws PatchDidiFailException {
		boolean changed = false;
		for (PatchFunction<CompoundTag, Boolean> f : levelPatchers) {
			changed |= f.apply(level);
		}
		return changed;
	}
}
