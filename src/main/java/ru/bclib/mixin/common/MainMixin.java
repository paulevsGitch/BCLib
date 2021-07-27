package ru.bclib.mixin.common;

import net.minecraft.server.Main;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.bclib.api.datafixer.DataFixerAPI;

@Mixin(Main.class)
abstract public class MainMixin {
	@ModifyArg(method="main", at=@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;convertFromRegionFormatIfNeeded(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;)V"))
	private static LevelStorageSource.LevelStorageAccess bclib_callServerFix(LevelStorageSource.LevelStorageAccess session){
		DataFixerAPI.fixData(session, false, (didFix)->{/* not called when showUI==false */});
		return session;
	}
}
