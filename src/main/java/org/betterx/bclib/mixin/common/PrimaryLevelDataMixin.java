package org.betterx.bclib.mixin.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelVersion;
import net.minecraft.world.level.storage.PrimaryLevelData;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import org.betterx.bclib.api.worldgen.WorldGenUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
    private static final ThreadLocal<Optional<RegistryOps<Tag>>> bcl_lastRegistryAccess = ThreadLocal.withInitial(
            () -> Optional.empty());

    @Inject(method = "parse", at = @At("HEAD"))
    private static void bcl_parse(Dynamic<Tag> dynamic,
                                  DataFixer dataFixer,
                                  int i,
                                  @Nullable CompoundTag compoundTag,
                                  LevelSettings levelSettings,
                                  LevelVersion levelVersion,
                                  WorldGenSettings worldGenSettings,
                                  Lifecycle lifecycle,
                                  CallbackInfoReturnable<PrimaryLevelData> cir) {
        if (dynamic.getOps() instanceof RegistryOps<Tag> regOps) {
            bcl_lastRegistryAccess.set(Optional.of(regOps));
        }
    }

    @ModifyArg(method = "parse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/PrimaryLevelData;<init>(Lcom/mojang/datafixers/DataFixer;ILnet/minecraft/nbt/CompoundTag;ZIIIFJJIIIZIZZZLnet/minecraft/world/level/border/WorldBorder$Settings;IILjava/util/UUID;Ljava/util/Set;Lnet/minecraft/world/level/timers/TimerQueue;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldGenSettings;Lcom/mojang/serialization/Lifecycle;)V"))
    private static WorldGenSettings bcl_fixSettings(WorldGenSettings settings) {
        Optional<RegistryOps<Tag>> registryOps = bcl_lastRegistryAccess.get();
        settings = WorldGenUtil.fixSettingsInCurrentWorld(registryOps, settings);

        bcl_lastRegistryAccess.set(Optional.empty());
        return settings;
    }
}
