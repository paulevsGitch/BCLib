package ru.bclib.mixin.common;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.WorldData;
import ru.bclib.api.BiomeAPI;
import ru.bclib.recipes.BCLRecipeManager;

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

	@Inject(method = "reloadResources", at = @At(value = "RETURN"), cancellable = true)
	private void bcl_reloadResources(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> info) {
		bcl_injectRecipes();
	}

	@Inject(method = "loadLevel", at = @At(value = "RETURN"), cancellable = true)
	private void bcl_loadLevel(CallbackInfo info) {
		bcl_injectRecipes();
		BiomeAPI.initRegistry(MinecraftServer.class.cast(this));
	}

	private void bcl_injectRecipes() {
		if (FabricLoader.getInstance().isModLoaded("kubejs")) {
			RecipeManagerAccessor accessor = (RecipeManagerAccessor) resources.getRecipeManager();
			accessor.bcl_setRecipes(BCLRecipeManager.getMap(accessor.bcl_getRecipes()));
		}
	}
}
