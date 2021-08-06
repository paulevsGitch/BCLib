package ru.bclib.api.datafixer;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Patch {
	
	private static List<Patch> ALL = new ArrayList<>(10);
	
	/**
	 * The Patch-Level derived from {@link #version}
	 */
	public final int level;
	
	/**
	 * The Patch-Version string
	 */
	public final String version;
	
	/**
	 * The Mod-ID that registered this Patch
	 */
	
	@NotNull
	public final String modID;
	
	static List<Patch> getALL() {
		return ALL;
	}
	
	/**
	 * Returns the highest Patch-Version that is available for the given mod. If no patches were
	 * registerd for the mod, this will return 0.0.0
	 *
	 * @param modID The ID of the mod you want to query
	 * @return The highest Patch-Version that was found
	 */
	public static String maxPatchVersion(@NotNull String modID) {
		return ALL.stream()
				  .filter(p -> p.modID
								.equals(modID))
				  .map(p -> p.version)
				  .reduce((p, c) -> c)
				  .orElse("0.0.0");
	}
	
	/**
	 * Returns the highest patch-level that is available for the given mod. If no patches were
	 * registerd for the mod, this will return 0
	 *
	 * @param modID The ID of the mod you want to query
	 * @return The highest Patch-Level that was found
	 */
	public static int maxPatchLevel(@NotNull String modID) {
		return ALL.stream()
				  .filter(p -> p.modID
								.equals(modID))
				  .mapToInt(p -> p.level)
				  .max()
				  .orElse(0);
	}
	
	/**
	 * Called by inheriting classes.
	 * <p>
	 * Performs some sanity checks on the values and might throw a #RuntimeException if any
	 * inconsistencies are found.
	 *
	 * @param modID   The ID of the Mod you want to register a patch for. This should be your
	 *                ModID only. The ModID can not be {@code null} or an empty String.
	 * @param version The mod-version that introduces the patch. This needs Semantic-Version String
	 *                like x.x.x. Developers are responsible for registering their patches in the correct
	 *                order (with increasing versions). You are not allowed to register a new
	 *                Patch with a version lower or equal than
	 *                {@link Patch#maxPatchVersion(String)}
	 */
	protected Patch(@NotNull String modID, String version) {
		//Patchlevels need to be unique and registered in ascending order
		if (modID == null || "".equals(modID)) {
			throw new RuntimeException("[INTERNAL ERROR] Patches need a valid modID!");
		}
		
		if (version == null || "".equals(version)) {
			throw new RuntimeException("Invalid Mod-Version");
		}
		
		this.version = version;
		this.level = DataFixerAPI.getModVersion(version);
		if (!ALL.stream()
				.filter(p -> p.modID
							  .equals(modID))
				.noneMatch(p -> p.level >= this.level) || this.level <= 0) {
			throw new RuntimeException("[INTERNAL ERROR] Patch-levels need to be created in ascending order beginning with 1.");
		}
		
		this.modID = modID;
	}
	
	@Override
	public String toString() {
		return "Patch{" + modID + ':' +version + ':' + level + '}';
	}
	
	
	/**
	 * Return block data fixes. Fixes will be applied on world load if current patch-level for
	 * the linked mod is lower than the {@link #level}.
	 * <p>
	 * The default implementation of this method returns an empty map.
	 *
	 * @return The returned Map should contain the replacements. All occurences of the
	 * {@code KeySet} are replaced with the associated value.
	 */
	public Map<String, String> getIDReplacements() {
		return new HashMap<String, String>();
	}
	
	/**
	 * Return a {@link PatchFunction} that is called with the content of <i>level.dat</i>.
	 * <p>
	 * The function needs to return {@code true}, if changes were made to the data.
	 * If an error occurs, the method should throw a {@link PatchDidiFailException}
	 *
	 * The default implementation of this method returns null.
	 *
	 * @return {@code true} if changes were applied and we need to save the data
	 */
	public PatchFunction<CompoundTag, Boolean> getLevelDatPatcher() { return null; }

	/**
	 * Return a {@link PatchFunction} that is called with the content from the
	 * {@link ru.bclib.api.WorldDataAPI} for this Mod.
	 * The function needs to return {@code true}, if changes were made to the data.
	 * If an error occurs, the method should throw a {@link PatchDidiFailException}
	 *
	 * The default implementation of this method returns null.
	 *
	 * @return {@code true} if changes were applied and we need to save the data
	 */
	public PatchFunction<CompoundTag, Boolean> getWorldDataPatcher() { return null; }
	
	/**
	 * Generates ready to use data for all currently registered patches. The list of
	 * patches is selected by the current patch-level of the world.
	 * <p>
	 * A {@link #Patch} with a given {@link #level} is only included if the patch-level of the
	 * world is less
	 * @param config The current patch-level configuration
	 * @return a new {@link MigrationProfile} Object.
	 */
	static MigrationProfile createMigrationData(CompoundTag config) {
		return new MigrationProfile(config);
	}
	
}
