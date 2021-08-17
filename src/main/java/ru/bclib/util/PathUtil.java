package ru.bclib.util;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class PathUtil {
	/**
	 * Tests if a path is a child-path.
	 * <p>
	 * A path is a child of another if it is located in the parent or any of the parents subdirectories
	 * @param parent The folder we search for the {@code child}
	 * @param child The folder you want to test
	 * @return {@code true} if {@code child} is in {@code parent} or any of its sub directories
	 */
	public static boolean isChildOf(Path parent, Path child){
		if (child==null || parent==null) return false;
		
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
	 * @param recursive	   if {@code false}, only the {@code path} is traversed
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
}
