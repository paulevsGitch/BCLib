package ru.bclib.config;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelStorageSource;
import ru.bclib.BCLib;

import java.io.File;

public class SessionConfig extends PathConfig {
	private static File getWorldFolder(LevelStorageSource.LevelStorageAccess session, ServerLevel world) {
		File dir = session.getDimensionPath(world.dimension());
		if (!new File(dir, "level.dat").exists()) {
			dir = dir.getParentFile();
		}
		return dir;
	}
	
	public final File levelFolder;
	
	public SessionConfig(String modID, String group, LevelStorageSource.LevelStorageAccess session, ServerLevel world) {
		super(modID, group, new File(getWorldFolder(session, world), BCLib.MOD_ID+"-config"));
		
		this.levelFolder = new File(getWorldFolder(session, world), BCLib.MOD_ID+"-config");
	}
}
