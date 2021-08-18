package ru.bclib.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.metadata.ModMetadataParser;
import net.fabricmc.loader.metadata.ParseMetadataException;
import org.apache.logging.log4j.LogManager;
import ru.bclib.BCLib;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.JarFile;

public class PathUtil {
	public final static Path GAME_FOLDER = FabricLoader.getInstance()
													   .getGameDir()
													   .normalize();
	
	public final static Path MOD_FOLDER = FabricLoader.getInstance()
													  .getGameDir()
													  .resolve("mods")
													  .normalize();
	
	public final static Path MOD_BAK_FOLDER = MOD_FOLDER.resolve("_bclib_deactivated")
														.normalize();
	
	/**
	 * Tests if a path is a child-path.
	 * <p>
	 * A path is a child of another if it is located in the parent or any of the parents subdirectories
	 *
	 * @param parent The folder we search for the {@code child}
	 * @param child  The folder you want to test
	 * @return {@code true} if {@code child} is in {@code parent} or any of its sub directories
	 */
	public static boolean isChildOf(Path parent, Path child) {
		if (child == null || parent == null) return false;
		
		final int pCount = parent.getNameCount();
		final int cCount = child.getNameCount();
		
		if (cCount > pCount) return isChildOf(parent, child.getParent());
		if (cCount < pCount) return false;
		
		return child.equals(parent);
	}
	
	/**
	 * A simple directory walker that ignores dot-files
	 *
	 * @param path         The path where you want to start
	 * @param pathConsumer The consumer called for each valid file. The consumer will get an absolute {@link Path}-Object
	 *                     for each visited file
	 */
	public static void fileWalker(File path, Consumer<Path> pathConsumer) {
		fileWalker(path, true, pathConsumer);
	}
	
	/**
	 * A simple directory walker that ignores dot-files
	 *
	 * @param path         The path where you want to start
	 * @param recursive    if {@code false}, only the {@code path} is traversed
	 * @param pathConsumer The consumer called for each valid file. The consumer will get an absolute {@link Path}-Object
	 *                     for each visited file
	 */
	public static void fileWalker(File path, boolean recursive, Consumer<Path> pathConsumer) {
		if (!path.exists()) return;
		for (final File f : path.listFiles()) {
			if (f.getName()
				 .startsWith(".")) continue;
			if (f.isDirectory()) {
				fileWalker(f, pathConsumer);
			}
			else if (f.isFile()) {
				pathConsumer.accept(f.toPath());
			}
		}
	}
	
	public static class ModInfo {
		public final ModMetadata metadata;
		public final Path jarPath;
		
		ModInfo(ModMetadata metadata, Path jarPath) {
			this.metadata = metadata;
			this.jarPath = jarPath;
		}
		
		@Override
		public String toString() {
			return "ModInfo{" + "id=" + metadata.getId() + ", version=" + metadata.getVersion() + ", jarPath=" + jarPath + '}';
		}
		
		public String getVersion() {
			if (metadata == null) return "0.0.0";
			return metadata.getVersion()
						   .toString();
		}
	}
	
	private static Map<String, ModInfo> mods;
	
	/**
	 * Unloads the cache of available mods created from {@link #getMods()}
	 */
	public static void invalidateCachedMods() {
		mods = null;
	}
	
	/**
	 * return a map of all mods that were found in the 'mods'-folder.
	 * <p>
	 * The method will cache the results. You can clear that cache (and free the memory) by
	 * calling {@link #invalidateCachedMods()}
	 * <p>
	 * An error message is printed if a mod fails to load, but the parsing will continue.
	 *
	 * @return A map of all found mods. (key=ModID, value={@link ModInfo})
	 */
	public static Map<String, ModInfo> getMods() {
		if (mods != null) return mods;
		
		mods = new HashMap<>();
		org.apache.logging.log4j.Logger logger = LogManager.getFormatterLogger("BCLib|ModLoader");
		PathUtil.fileWalker(MOD_FOLDER.toFile(), false, (file -> {
			try {
				URI uri = URI.create("jar:" + file.toUri());
				
				try (FileSystem fs = FileSystems.getFileSystem(uri)) {
					final JarFile jarFile = new JarFile(file.toString());
					Path modMetaFile = fs.getPath("fabric.mod.json");
					
					ModMetadata mc = ModMetadataParser.parseMetadata(logger, modMetaFile);
					mods.put(mc.getId(), new ModInfo(mc, file));
				}
				catch (ParseMetadataException e) {
					BCLib.LOGGER.error(e.getMessage());
				}
			}
			catch (IOException e) {
				BCLib.LOGGER.error(e.getMessage());
			}
		}));
		
		return mods;
	}
	
	/**
	 * Returns the {@link ModInfo} or {@code null} if the mod was not found.
	 * <p>
	 * The call will also return null if the mode-Version in the jar-File is not the same
	 * as the version of the loaded Mod.
	 *
	 * @param modID The mod ID to query
	 * @return A {@link ModInfo}-Object for the querried Mod.
	 */
	public static ModInfo getModInfo(String modID) {
		return getModInfo(modID, true);
	}
	
	public static ModInfo getModInfo(String modID, boolean matchVersion) {
		getMods();
		final ModInfo mi = mods.get(modID);
		if (mi == null || !getModVersion(modID).equals(mi.getVersion())) return null;
		return mi;
	}
	
	/**
	 * Local Mod Version for the queried Mod
	 *
	 * @param modID The mod ID to query
	 * @return The version of the locally installed Mod
	 */
	public static String getModVersion(String modID) {
		Optional<ModContainer> optional = FabricLoader.getInstance()
													  .getModContainer(modID);
		if (optional.isPresent()) {
			ModContainer modContainer = optional.get();
			return modContainer.getMetadata()
							   .getVersion()
							   .toString();
		}
		return "0.0.0";
	}
	
	/**
	 * Creates a human readable File-Size
	 *
	 * @param size Filesize in bytes
	 * @return A Human readable String
	 */
	public static String humanReadableFileSize(long size) {
		final int threshold = 2;
		final int factor = 1024;
		if (size < 0) return "? Byte";
		if (size < factor * threshold) {
			return size + " Byte";
		}
		char[] units = {'K', 'M', 'G', 'T', 'P'};
		int unitIndex = 0;
		double fSize = size;
		do {
			unitIndex++;
			fSize /= 1024;
		} while (fSize > factor * threshold && unitIndex < units.length);
		
		return String.format("%.1f %ciB", fSize, units[unitIndex - 1]);
	}
}
