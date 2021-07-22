package ru.bclib.api.datafixer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import ru.bclib.BCLib;
import ru.bclib.api.WorldDataAPI;
import ru.bclib.config.Configs;
import ru.bclib.util.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataFixerAPI {
	static final Logger LOGGER = new Logger("DataFixerAPI");
	
	public static void fixData(File dir) {
		if (!Configs.MAIN_CONFIG.getBoolean(Configs.MAIN_PATCH_CATEGORY, "applyPatches", true)) {
			LOGGER.info("World Patches are disabled");
			return;
		}
		
		final CompoundTag patchConfig = WorldDataAPI.getCompoundTag(BCLib.MOD_ID, Configs.MAIN_PATCH_CATEGORY);
		MigrationProfile data = Patch.createMigrationData(patchConfig);
		if (!data.hasAnyFixes()) {
			LOGGER.info("Everything up to date");
			return;
		}

		boolean[] allOk = {true};
		
		List<File> regions = getAllRegions(dir, null);
		regions.parallelStream().forEach((file) -> fixRegion(data, allOk, file));
		
		if (allOk[0]) {
			data.markApplied();
			WorldDataAPI.saveFile(BCLib.MOD_ID);
		}
	}

	private static void fixRegion(MigrationProfile data, boolean[] allOk, File file) {
		try {
			LOGGER.info("Inspecting " + file);
			boolean[] changed = new boolean[1];
			RegionFile region = new RegionFile(file, file.getParentFile(), true);
			for (int x = 0; x < 32; x++) {
				for (int z = 0; z < 32; z++) {
					ChunkPos pos = new ChunkPos(x, z);
					changed[0] = false;
					if (region.hasChunk(pos)) {
						DataInputStream input = region.getChunkDataInputStream(pos);
						CompoundTag root = NbtIo.read(input);
						if ((root.toString().contains("betternether:chest") || root.toString().contains("bclib:chest"))) {
						   NbtIo.write(root, new File(file.toString() + "-" + x + "-" + z + ".nbt"));
						}
						input.close();

						//Checking TileEntities
						ListTag tileEntities = root.getCompound("Level")
												   .getList("TileEntities", 10);
						fixItemArrayWithID(tileEntities, changed, data, true);

						//Checking Entities
						ListTag entities = root.getList("Entities", 10);
						fixItemArrayWithID(entities, changed, data, true);


						//Checking Block Palette
						ListTag sections = root.getCompound("Level")
											   .getList("Sections", 10);
						sections.forEach((tag) -> {
							ListTag palette = ((CompoundTag) tag).getList("Palette", 10);
							palette.forEach((blockTag) -> {
								CompoundTag blockTagCompound = ((CompoundTag) blockTag);
								changed[0] |= data.replaceStringFromIDs(blockTagCompound, "Name");
							});
						});

						if (changed[0]) {
							LOGGER.warning("Writing '{}': {}/{}", file, x, z);
							NbtIo.write(root, new File(file.toString() + "-" + x + "-" + z + "-changed.nbt"));
							DataOutputStream output = region.getChunkDataOutputStream(pos);
							NbtIo.write(root, output);
							output.close();

						}
					}
				}
			}
			region.close();
		}
		catch (Exception e) {
			allOk[0] = false;
			e.printStackTrace();
		}
	}

	static CompoundTag patchConfTag = null;
	static CompoundTag getPatchData(){
		if (patchConfTag==null) {
			patchConfTag = WorldDataAPI.getCompoundTag(BCLib.MOD_ID, Configs.MAIN_PATCH_CATEGORY);
		}
		return patchConfTag;
	}
	
	private static boolean fixItemArrayWithID(ListTag items, boolean[] changed, MigrationProfile data, boolean recursive) {
		items.forEach(inTag -> {
			final CompoundTag tag = (CompoundTag) inTag;
			
			changed[0] |= data.replaceStringFromIDs(tag, "id");
			
			if (recursive && tag.contains("Items")) {
				changed[0] |= fixItemArrayWithID(tag.getList("Items", 10), changed, data, true);
			}
			if (recursive && tag.contains("Inventory")) {
				changed[0] |= fixItemArrayWithID(tag.getList("Inventory", 10), changed, data, true);
			}
		});
		
		return changed[0];
	}
	
	private static List<File> getAllRegions(File dir, List<File> list) {
		if (list == null) {
			list = new ArrayList<>();
		}
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				getAllRegions(file, list);
			} else if (file.isFile() && file.getName().endsWith(".mca")) {
				list.add(file);
			}
		}
		return list;
	}
	
	/**
	 * register a new Patch
	 *
	 * @param patch A #Supplier that will instantiate the new Patch Object
	 */
	public static void registerPatch(Supplier<Patch> patch) {
		Patch.getALL().add(patch.get());
	}
	
	/**
	 * Get mod version from string. String should be in format: %d.%d.%d
	 *
	 * @param version - {@link String} mod version.
	 * @return int mod version.
	 */
	public static int getModVersion(String version) {
		if (version.isEmpty()) {
			return 0;
		}
		try {
			int res = 0;
			final String semanticVersionPattern = "(\\d+)\\.(\\d+)\\.(\\d+)\\D*";
			final Matcher matcher = Pattern.compile(semanticVersionPattern)
										   .matcher(version);
			if (matcher.find()) {
				if (matcher.groupCount() > 0) res = (Integer.parseInt(matcher.group(1)) & 0xFF) << 22;
				if (matcher.groupCount() > 1) res |= (Integer.parseInt(matcher.group(2)) & 0xFF) << 14;
				if (matcher.groupCount() > 2) res |= Integer.parseInt(matcher.group(3)) & 0x3FFF;
			}
			
			return res;
		}
		catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * Get mod version from integer. String will be in format %d.%d.%d
	 *
	 * @param version - mod version in integer form.
	 * @return {@link String} mod version.
	 */
	public static String getModVersion(int version) {
		int a = (version >> 22) & 0xFF;
		int b = (version >> 14) & 0xFF;
		int c = version & 0x3FFF;
		return String.format(Locale.ROOT, "%d.%d.%d", a, b, c);
	}
	
}
