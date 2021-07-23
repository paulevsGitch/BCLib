package ru.bclib.mixin.common;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.api.BiomeAPI;
import ru.bclib.api.WorldDataAPI;
import ru.bclib.api.datafixer.DataFixerAPI;
import ru.bclib.recipes.BCLRecipeManager;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Shadow
	private ServerResources resources;
	
	@Final
	@Shadow
	private Map<ResourceKey<Level>, ServerLevel> levels;
	
	@Final
	@Shadow
	protected WorldData worldData;

	@Inject(method="convertFromRegionFormatIfNeeded", at = @At("HEAD"))
	private static void bclib_applyPatches(LevelStorageSource.LevelStorageAccess session, CallbackInfo ci){
		File levelPath = session.getLevelPath(LevelResource.ROOT).toFile();
		WorldDataAPI.load(new File(levelPath, "data"));
		DataFixerAPI.fixData(levelPath);
	}


	@Inject(method = "reloadResources", at = @At(value = "RETURN"), cancellable = true)
	private void bclib_reloadResources(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> info) {
		bclib_injectRecipes();
	}
	
	@Inject(method = "loadLevel", at = @At(value = "RETURN"), cancellable = true)
	private void bclib_loadLevel(CallbackInfo info) {
		bclib_injectRecipes();
		BiomeAPI.initRegistry(MinecraftServer.class.cast(this));
	}
	
	private void bclib_injectRecipes() {
		if (FabricLoader.getInstance().isModLoaded("kubejs")) {
			RecipeManagerAccessor accessor = (RecipeManagerAccessor) resources.getRecipeManager();
			accessor.bclib_setRecipes(BCLRecipeManager.getMap(accessor.bclib_getRecipes()));
		}
	}
}
