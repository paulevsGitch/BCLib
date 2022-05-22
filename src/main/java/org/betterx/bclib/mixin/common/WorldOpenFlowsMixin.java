package org.betterx.bclib.mixin.common;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;

import com.mojang.datafixers.util.Pair;
import org.betterx.bclib.api.LifeCycleAPI;
import org.betterx.bclib.api.biomes.BiomeAPI;
import org.betterx.bclib.api.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.datafixer.DataFixerAPI;
import org.betterx.bclib.config.Configs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {

    @Shadow
    @Final
    private LevelStorageSource levelSource;

    @Shadow
    protected abstract void doLoadLevel(Screen screen, String levelID, boolean safeMode, boolean canAskForBackup);

    @Inject(method = "loadLevel", cancellable = true, at = @At("HEAD"))
    private void bcl_callFixerOnLoad(Screen screen, String levelID, CallbackInfo ci) {
        DataExchangeAPI.prepareServerside();
        BiomeAPI.prepareNewLevel();

        if (DataFixerAPI.fixData(this.levelSource, levelID, true, (appliedFixes) -> {
            LifeCycleAPI._runBeforeLevelLoad();
            this.doLoadLevel(screen, levelID, false, false);
        })) {
            //cancel call when fix-screen is presented
            ci.cancel();
        } else {
            LifeCycleAPI._runBeforeLevelLoad();
            if (Configs.CLIENT_CONFIG.suppressExperimentalDialog()) {
                this.doLoadLevel(screen, levelID, false, false);
                //cancel call as we manually start the level load here
                ci.cancel();
            }
        }
    }

    @Inject(method = "createFreshLevel", at = @At("HEAD"))
    public void bcl_createFreshLevel(String levelID,
                                     LevelSettings levelSettings,
                                     RegistryAccess registryAccess,
                                     WorldGenSettings worldGenSettings,
                                     CallbackInfo ci) {
        DataExchangeAPI.prepareServerside();
        BiomeAPI.prepareNewLevel();

        DataFixerAPI.createWorldData(this.levelSource, levelID, worldGenSettings);
        LifeCycleAPI._runBeforeLevelLoad();
    }

    @Inject(method = "createLevelFromExistingSettings", at = @At("HEAD"))
    public void bcl_createLevelFromExistingSettings(LevelStorageSource.LevelStorageAccess levelStorageAccess,
                                                    ReloadableServerResources reloadableServerResources,
                                                    RegistryAccess.Frozen frozen,
                                                    WorldData worldData,
                                                    CallbackInfo ci) {
        DataExchangeAPI.prepareServerside();
        BiomeAPI.prepareNewLevel();

        DataFixerAPI.createWorldData(levelStorageAccess, worldData.worldGenSettings());
        LifeCycleAPI._runBeforeLevelLoad();
    }

    @Inject(method = "loadWorldStem(Lnet/minecraft/server/WorldLoader$PackConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;)Lnet/minecraft/server/WorldStem;", at = @At("RETURN"))
    public void bcl_loadWorldStem(WorldLoader.PackConfig packConfig,
                                  WorldLoader.WorldDataSupplier<WorldData> worldDataSupplier,
                                  CallbackInfoReturnable<WorldStem> cir) {
        WorldStem result = cir.getReturnValue();
    }

    @Inject(method = "method_41887", at = @At("RETURN"))
    private static void bcl_loadWorldStem(LevelStorageSource.LevelStorageAccess levelStorageAccess,
                                          ResourceManager resourceManager,
                                          DataPackConfig dataPackConfig,
                                          CallbackInfoReturnable<Pair> cir) {
        RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
        RegistryOps<Tag> dynamicOps = RegistryOps.create(NbtOps.INSTANCE, writable);
        WorldData worldData = levelStorageAccess.getDataTag(dynamicOps,
                dataPackConfig,
                writable.allElementsLifecycle());
        Pair<WorldData, RegistryAccess> p = cir.getReturnValue();
    }
}
