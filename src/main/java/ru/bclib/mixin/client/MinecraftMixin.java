package ru.bclib.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.main.GameConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess.RegistryHolder;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.api.datafixer.DataFixerAPI;
import ru.bclib.interfaces.CustomColorProvider;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Final
	@Shadow
	private BlockColors blockColors;
	
	@Final
	@Shadow
	private ItemColors itemColors;
	
	@Inject(method = "<init>*", at = @At("TAIL"))
	private void bclib_onMCInit(GameConfig args, CallbackInfo info) {
		Registry.BLOCK.forEach(block -> {
			if (block instanceof CustomColorProvider) {
				CustomColorProvider provider = (CustomColorProvider) block;
				blockColors.register(provider.getProvider(), block);
				itemColors.register(provider.getItemProvider(), block.asItem());
			}
		});
	}
	
	
	@Shadow @Final private LevelStorageSource levelSource;
	@Shadow public abstract void loadLevel(String string);
	private final String BCLIB_RECURSION = "$@BCLIB:";
	
	@Inject(method="loadLevel", cancellable = true, at=@At("HEAD"))
	private void bclib_callFixerOnLoad(String levelID, CallbackInfo ci){
		boolean recursiveCall = false;
		if (levelID.startsWith(BCLIB_RECURSION)) {
			levelID = levelID.substring(BCLIB_RECURSION.length());
			recursiveCall = true;
		}

		final String recursiveLevelID = BCLIB_RECURSION + levelID;
		if (!recursiveCall && DataFixerAPI.fixData(this.levelSource, levelID, true, (appliedFixes)->{
			this.loadLevel(recursiveLevelID);
		})){
			ci.cancel();
		}
	}
	
	@ModifyArg(method="loadLevel", at=@At(value="INVOKE",  target="Lnet/minecraft/client/Minecraft;doLoadLevel(Ljava/lang/String;Lnet/minecraft/core/RegistryAccess$RegistryHolder;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/client/Minecraft$ExperimentalDialogType;)V"))
	private String bclib_correctLevelID(String levelID){
		if (levelID.startsWith(BCLIB_RECURSION)) {
			levelID = levelID.substring(BCLIB_RECURSION.length());
		}
		
		return levelID;
	}
	
	@Inject(method="createLevel", at=@At("HEAD"))
	private void bclib_initPatchData(String levelID, LevelSettings levelSettings, RegistryHolder registryHolder, WorldGenSettings worldGenSettings, CallbackInfo ci) {
		DataFixerAPI.initializeWorldData(this.levelSource, levelID, true);
	}
	
	
//	@Inject(method="doLoadLevel", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at=@At(value="INVOKE", target="Lnet/minecraft/client/Minecraft;makeServerStem(Lnet/minecraft/core/RegistryAccess$RegistryHolder;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;)Lnet/minecraft/client/Minecraft$ServerStem;"))
//	private void bclib_onCallFixer(
//		String string,
//		RegistryHolder registryHolder,
//		Function<LevelStorageAccess, DataPackConfig> function,
//		Function4<LevelStorageAccess, RegistryHolder, ResourceManager, DataPackConfig, WorldData> function4,
//		boolean bl,
//		ExperimentalDialogType experimentalDialogType,
//		CallbackInfo ci,
//		LevelStorageSource.LevelStorageAccess levelStorageAccess) {
//
//			DataFixerAPI.fixData(levelStorageAccess);
//			ci.cancel();
//
//	}
}
