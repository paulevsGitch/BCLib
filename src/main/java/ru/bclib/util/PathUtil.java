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
	
	private static Map<String, ModMetadata> mods;
	
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
	 * @return A map of all found mods. (key=ModID, value={@link ModMetadata})
	 */
	public static Map<String, ModMetadata> getMods() {
		if (mods!=null) return mods;
		
		mods = new HashMap<>();
		org.apache.logging.log4j.Logger logger = LogManager.getFormatterLogger("BCLib|ModLoader");
		PathUtil.fileWalker(MOD_FOLDER.toFile(), false, (file -> {
			try {
				URI uri = URI.create("jar:" + file.toUri());
				
				try (FileSystem fs = FileSystems.getFileSystem(uri)) {
					final JarFile jarFile = new JarFile(file.toString());
					Path modMetaFile = fs.getPath("fabric.mod.json");
					
					ModMetadata mc = ModMetadataParser.parseMetadata(logger, modMetaFile);
					mods.put(mc.getId(), mc);
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
	
	public static ModContainer getModContainer(String modID) {
		Optional<ModContainer> optional = FabricLoader.getInstance()
													  .getModContainer(modID);
		return optional.orElse(null);
	}
}
