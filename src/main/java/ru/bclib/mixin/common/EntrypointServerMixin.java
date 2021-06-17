package ru.bclib.mixin.common;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.loader.entrypoint.minecraft.hooks.EntrypointServer;
import ru.bclib.api.ModIntegrationAPI;

@Mixin(value = EntrypointServer.class, remap = false)
public class EntrypointServerMixin {
	@Inject(method = "start", at = @At(value = "TAIL"))
	private static void start(File runDir, Object gameInstance, CallbackInfo info) {
		ModIntegrationAPI.registerAll();
	}
}
