package ru.bclib.mixin.common;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import ru.bclib.api.DataFixerAPI;
import ru.bclib.api.WorldDataAPI;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
	private static String bcl_lastWorld = null;
	
	protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, DimensionType dimensionType, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l) {
		super(writableLevelData, resourceKey, dimensionType, supplier, bl, bl2, l);
	}
	
	@Inject(method = "<init>*", at = @At("TAIL"))
	private void bcl_onServerWorldInit(MinecraftServer server, Executor workerExecutor, LevelStorageSource.LevelStorageAccess session, ServerLevelData properties, ResourceKey<Level> registryKey, DimensionType dimensionType, ChunkProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long l, List<CustomSpawner> list, boolean bl, CallbackInfo info) {
		if (bcl_lastWorld != null && bcl_lastWorld.equals(session.getLevelId())) {
			return;
		}
		
		bcl_lastWorld = session.getLevelId();
		
		ServerLevel world = ServerLevel.class.cast(this);
		File dir = session.getDimensionPath(world.dimension());
		if (!new File(dir, "level.dat").exists()) {
			dir = dir.getParentFile();
		}
		
		DataFixerAPI.fixData(dir);
		WorldDataAPI.load(new File(dir, "data"));
	}
}
