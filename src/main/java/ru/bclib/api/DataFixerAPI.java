package ru.bclib.api;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;

public class DataFixerAPI {
	private static final Map<String, String> REPLACEMENT = Maps.newHashMap();
	private static final Map<String, Integer> FIX_VERSIONS = Maps.newHashMap();
	
	public static void fixData(File dir) {
		if (REPLACEMENT.isEmpty()) {
			return;
		}
		
		boolean shoudFix = false;
		Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
		for (ModContainer mod: mods) {
			String name = mod.getMetadata().getId();
			int version = getModVersion(mod.getMetadata().getVersion().toString());
			if (version > 0) {
				shoudFix |= FIX_VERSIONS.getOrDefault(name, version) < version;
			}
		};
		if (!shoudFix) {
			return;
		}
		
		List<File> regions = getAllRegions(dir, null);
		regions.parallelStream().forEach((file) -> {
			try {
				System.out.println("Fixing " + file);
				boolean[] changed = new boolean[1];
				RegionFile region = new RegionFile(file, file.getParentFile(), true);
				for (int x = 0; x < 32; x++) {
					for (int z = 0; z < 32; z++) {
						ChunkPos pos = new ChunkPos(x, z);
						changed[0] = false;
						if (region.hasChunk(pos)) {
							DataInputStream input = region.getChunkDataInputStream(pos);
							CompoundTag root = NbtIo.read(input);
							input.close();
							ListTag sections = root.getCompound("Level").getList("Sections", 10);
							sections.forEach((tag) -> {
								ListTag palette = ((CompoundTag) tag).getList("Palette", 10);
								palette.forEach((blockTag) -> {
									CompoundTag blockTagCompound = ((CompoundTag) blockTag);
									String name = blockTagCompound.getString("Name");
									String replace = REPLACEMENT.get(name);
									if (replace != null) {
										blockTagCompound.putString("Name", replace);
										changed[0] = true;
									}
								});
							});
							if (changed[0]) {
								System.out.println("Write!");
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
				e.printStackTrace();
			}
		});
	}
	
	/**
	 * Register block data fix. Fix will be applied on world load if current mod version will be newer than specified one.
	 * @param modID - {@link String} mod id;
	 * @param modVersion - {@link String} mod version, should be in format: %d.%d.%d
	 * @param result - {@link String} new block name;
	 * @param names - array of {@link String}, old block names to convert.
	 */
	protected static void addFix(String modID, String modVersion, String result, String... names) {
		FIX_VERSIONS.put(modID, getModVersion(modVersion));
		for (String name: names) {
			REPLACEMENT.put(name, result);
		}
	}
	
	private static List<File> getAllRegions(File dir, List<File> list) {
		if (list == null) {
			list = Lists.newArrayList();
		}
		for (File file: dir.listFiles()) {
			if (file.isDirectory()) {
				getAllRegions(file, list);
			}
			else if (file.isFile() && file.getName().endsWith(".mca")) {
				list.add(file);
			}
		}
		return list;
	}
	
	/**
	 * Get mod version from string. String should be in format: %d.%d.%d
	 * @param version - {@link String} mod version.
	 * @return
	 */
	public static int getModVersion(String version) {
		if (version.isEmpty()) {
			return 0;
		}
		try {
			String[] values = version.split("\\.");
			return Integer.parseInt(values[0]) << 12 | Integer.parseInt(values[1]) << 6 | Integer.parseInt(values[2]);
		}
		catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * Get mod version from integer. String will be in format %d.%d.%d
	 * @param version
	 * @return
	 */
	public static String getModVersion(int version) {
		int a = (version >> 12) & 63;
		int b = (version >> 6) & 63;
		int c = version & 63;
		return String.format(Locale.ROOT, "%d.%d.%d", a, b, c);
	}
}
