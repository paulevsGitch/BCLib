package ru.bclib.mixin.client;

import com.mojang.datafixers.util.Function4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Minecraft.ExperimentalDialogType;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.main.GameConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.RegistryHolder;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.api.LifeCycleAPI;
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.datafixer.DataFixerAPI;
import ru.bclib.interfaces.CustomColorProvider;

import java.util.function.Function;

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
	
	@Shadow
	protected abstract void doLoadLevel(String string, RegistryHolder registryHolder, Function<LevelStorageAccess, DataPackConfig> function, Function4<LevelStorageAccess, RegistryHolder, ResourceManager, DataPackConfig, WorldData> function4, boolean bl, ExperimentalDialogType experimentalDialogType);
	
	@Shadow
	@Final
	private LevelStorageSource levelSource;
	
	@Inject(method = "loadLevel", cancellable = true, at = @At("HEAD"))
	private void bclib_callFixerOnLoad(String levelID, CallbackInfo ci) {
		DataExchangeAPI.prepareServerside();
		
		if (DataFixerAPI.fixData(this.levelSource, levelID, true, (appliedFixes) -> {
			LifeCycleAPI._runBeforeLevelLoad();
			this.doLoadLevel(levelID, RegistryAccess.builtin(), Minecraft::loadDataPacks, Minecraft::loadWorldData, false, appliedFixes?ExperimentalDialogType.NONE:ExperimentalDialogType.BACKUP);
		})) {
			ci.cancel();
		} else {
			LifeCycleAPI._runBeforeLevelLoad();
		}
	}
	
	@Inject(method = "createLevel", at = @At("HEAD"))
	private void bclib_initPatchData(String levelID, LevelSettings levelSettings, RegistryHolder registryHolder, WorldGenSettings worldGenSettings, CallbackInfo ci) {
		DataFixerAPI.initializeWorldData(this.levelSource, levelID, true);
		LifeCycleAPI._runBeforeLevelLoad();
	}
}
