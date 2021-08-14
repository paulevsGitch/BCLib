package ru.bclib.mixin.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SimpleReloadableResourceManager.class)
public class SimpleReloadableResourceManagerMixin {
	@Final
	@Shadow
	private Map<String, FallbackResourceManager> namespacedPacks;
	
	private static final String[] BCLIB_MISSING_RESOURCES = new String[] {
		"dimension/the_end.json",
		"dimension/the_nether.json",
		"dimension_type/the_end.json",
		"dimension_type/the_nether.json"
	};
	
	@Inject(method = "hasResource", at = @At("HEAD"), cancellable = true)
	private void hasResource(ResourceLocation resourceLocation, CallbackInfoReturnable<Boolean> info) {
		if (resourceLocation.getNamespace().equals("minecraft")) {
			for (String key: BCLIB_MISSING_RESOURCES) {
				if (resourceLocation.getPath().equals(key)) {
					info.setReturnValue(false);
					return;
				}
			}
		}
	}
}
