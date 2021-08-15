package ru.bclib.util;

import java.nio.file.Path;

public class PathUtil {
	public static boolean isChildOf(Path parent, Path child){
		if (child==null || parent==null) return false;
		
		final int pCount = parent.getNameCount();
		final int cCount = child.getNameCount();
		
		if (cCount > pCount) return isChildOf(parent, child.getParent());
		if (cCount < pCount) return false;
		
		return child.equals(parent);
	}
}
