package ru.bclib.mixin.client;

import com.mojang.datafixers.util.Function4;
import net.minecraft.client.Minecraft;
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
import ru.bclib.api.dataexchange.DataExchangeAPI;
import ru.bclib.api.datafixer.DataFixerAPI;
import ru.bclib.interfaces.CustomColorProvider;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
	
//	@Shadow
//	protected abstract void doLoadLevel(String string, RegistryHolder registryHolder, Function<LevelStorageAccess, DataPackConfig> function, Function4<LevelStorageAccess, RegistryHolder, ResourceManager, DataPackConfig, WorldData> function4, boolean bl, ExperimentalDialogType experimentalDialogType);
//
	@Shadow
	@Final
	private LevelStorageSource levelSource;
	Method doLoadLevel = null;
	Object experimentalDialogType_BACKUP = null;

	private void bclib_doLoadLevel_BACKUP(String levelID, RegistryHolder registryHolder, Function<LevelStorageAccess, DataPackConfig> function, Function4<LevelStorageAccess, RegistryHolder, ResourceManager, DataPackConfig, WorldData> function4, boolean bl){
		if (experimentalDialogType_BACKUP==null) {
			try {
				Class experimentalDialogType = Class.forName("net.minecraft.client.Minecraft$ExperimentalDialogType");
				Field f = experimentalDialogType.getDeclaredField("$VALUES");
				f.setAccessible(true);
				experimentalDialogType_BACKUP = Array.get(f.get(null), 2);
			} catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
				e.printStackTrace();
			}
		}

		if (doLoadLevel==null) {
			for (Method m : Minecraft.class.getDeclaredMethods()) {
				if (m.getName().equals("doLoadLevel")) {
					doLoadLevel = m;
					break;
				}
			}
		}

		if (doLoadLevel!=null && experimentalDialogType_BACKUP!=null){
			doLoadLevel.setAccessible(true);
			try {
				doLoadLevel.invoke(this, new Object[]{levelID, registryHolder, function, function4, bl, experimentalDialogType_BACKUP});
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	@Inject(method = "loadLevel", cancellable = true, at = @At("HEAD"))
	private void bclib_callFixerOnLoad(String levelID, CallbackInfo ci) {
		DataExchangeAPI.prepareServerside();
		
		if (DataFixerAPI.fixData(this.levelSource, levelID, true, (appliedFixes) -> {
			this.doLoadLevel(levelID, RegistryAccess.builtin(), Minecraft::loadDataPacks, Minecraft::loadWorldData, false, appliedFixes?ExperimentalDialogType.NONE:ExperimentalDialogType.BACKUP);
		})) {
			ci.cancel();
		}
	}
	
	@Inject(method = "createLevel", at = @At("HEAD"))
	private void bclib_initPatchData(String levelID, LevelSettings levelSettings, RegistryHolder registryHolder, WorldGenSettings worldGenSettings, CallbackInfo ci) {
		DataFixerAPI.initializeWorldData(this.levelSource, levelID, true);
	}
}
