package org.betterx.bclib.mixin.common;

import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;

import org.betterx.bclib.presets.worldgen.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
    @Shadow
    private LevelSettings settings;

    @ModifyArg(method = "parse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/PrimaryLevelData;<init>(Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/CompoundTag;ZIIIFJJIIIZIZZZLnet/minecraft/world/level/border/WorldBorder$Settings;IILjava/util/UUID;Ljava/util/Set;Lnet/minecraft/world/level/timers/TimerQueue;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldGenSettings;Lcom/mojang/serialization/Lifecycle;)V"))
    private static WorldGenSettings bcl_fixSettings(WorldGenSettings settings) {
        settings = WorldPresets.fixSettingsInCurrentWorld(LevelStem.NETHER, BuiltinDimensionTypes.NETHER, settings);
        settings = WorldPresets.fixSettingsInCurrentWorld(LevelStem.END, BuiltinDimensionTypes.END, settings);
        return settings;
    }
}
