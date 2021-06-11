package ru.bclib.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import ru.bclib.BCLib;

public class WorldDataAPI {
	private static final Map<String, CompoundTag> TAGS = Maps.newHashMap();
	private static final List<String> MODS = Lists.newArrayList();
	private static File dataDir;
	
	public static void load(File dataDir) {
		WorldDataAPI.dataDir = dataDir;
		MODS.stream().parallel().forEach(modID -> {
			File file = new File(dataDir, modID + ".nbt");
			CompoundTag root = new CompoundTag();
			if (file.exists()) {
				try {
					root = NbtIo.readCompressed(file);
				}
				catch (IOException e) {
					BCLib.LOGGER.error("World data loading failed", e);
				}
			}
			TAGS.put(modID, root);
		});
	}
	
	/**
	 * Register mod cache, world cache is located in world data folder.
	 * @param modID - {@link String} modID.
	 */
	public static void registerModCache(String modID) {
		MODS.add(modID);
	}
	
	/**
	 * Get root {@link CompoundTag} for mod cache in world data folder.
	 * @param modID - {@link String} modID.
	 * @return {@link CompoundTag}
	 */
	public static CompoundTag getRootTag(String modID) {
		CompoundTag root = TAGS.get(modID);
		if (root == null) {
			root = new CompoundTag();
			TAGS.put(modID, root);
		}
		return root;
	}
	
	/**
	 * Get {@link CompoundTag} with specified path from mod cache in world data folder.
	 * @param modID - {@link String} path to tag, dot-separated.
	 * @return {@link CompoundTag}
	 */
	public static CompoundTag getCompoundTag(String modID, String path) {
		String[] parts = path.split("\\.");
		CompoundTag tag = getRootTag(modID);
		for (String part: parts) {
			if (tag.contains(part)) {
				tag = tag.getCompound(part);
			}
			else {
				CompoundTag t = new CompoundTag();
				tag.put(part, t);
				tag = t;
			}
		}
		return tag;
	}
	
	public static void saveFile(String modID) {
		try {
			NbtIo.writeCompressed(getRootTag(modID), new File(dataDir, modID + ".nbt"));
		}
		catch (IOException e) {
			BCLib.LOGGER.error("World data saving failed", e);
		}
	}
}
