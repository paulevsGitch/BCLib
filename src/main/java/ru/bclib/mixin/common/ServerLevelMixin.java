package ru.bclib.mixin.common;

import net.minecraft.core.Holder;
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
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.api.LifeCycleAPI;
import ru.bclib.api.biomes.BiomeAPI;
import ru.bclib.world.generator.BCLibNetherBiomeSource;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
	private static String bclib_lastWorld = null;
	
	protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> dimensionType, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l) {
		super(writableLevelData, resourceKey, dimensionType, supplier, bl, bl2, l);
	}

	@Inject(method = "<init>*", at = @At("TAIL"))
	private void bclib_onServerWorldInit(MinecraftServer server, Executor executor, LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey resourceKey, Holder<DimensionType> dimensionType, ChunkProgressListener chunkProgressListener, ChunkGenerator chunkGenerator, boolean bl, long l, List list, boolean bl2, CallbackInfo ci) {
		ServerLevel level = ServerLevel.class.cast(this);
		LifeCycleAPI._runLevelLoad(level, server, executor, levelStorageAccess, serverLevelData, resourceKey, dimensionType, chunkProgressListener, chunkGenerator, bl, l, list, bl2);
		
		BiomeAPI.applyModifications(ServerLevel.class.cast(this));
		
		if (level.dimension() == Level.NETHER) {
			BCLibNetherBiomeSource.setWorldHeight(level.getChunkSource().getGenerator().getGenDepth());
		}
		
		if (bclib_lastWorld != null && bclib_lastWorld.equals(levelStorageAccess.getLevelId())) {
			return;
		}
		
		bclib_lastWorld = levelStorageAccess.getLevelId();
	}
}
