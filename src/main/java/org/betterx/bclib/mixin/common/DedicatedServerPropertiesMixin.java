package org.betterx.bclib.mixin.common;

import net.minecraft.server.dedicated.DedicatedServerProperties;

import org.betterx.bclib.presets.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DedicatedServerProperties.class)
public class DedicatedServerPropertiesMixin {
    //Make sure the default server properties use our Default World Preset
    @ModifyArg(method = "<init>", index = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServerProperties$WorldGenProperties;<init>(Ljava/lang/String;Lcom/google/gson/JsonObject;ZLjava/lang/String;)V"))
    private String bcl_foo(String levelType) {
        return WorldPresets.DEFAULT.orElseThrow().location().toString();
    }
}
