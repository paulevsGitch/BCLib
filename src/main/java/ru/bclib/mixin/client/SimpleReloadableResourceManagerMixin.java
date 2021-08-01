package ru.bclib.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.BCLib;
import ru.bclib.api.ModIntegrationAPI;
import ru.bclib.client.render.EmissiveTextureInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Mixin(SimpleReloadableResourceManager.class)
public class SimpleReloadableResourceManagerMixin {
	@Final
	@Shadow
	private Map<String, FallbackResourceManager> namespacedPacks;
	
	private ResourceLocation bclib_alphaEmissionMaterial = BCLib.makeID("materialmaps/block/alpha_emission.json");
	
	@Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
	private void bclib_getResource(ResourceLocation resourceLocation, CallbackInfoReturnable<Resource> info) throws IOException {
		if (!ModIntegrationAPI.hasCanvas()) {
			return;
		}
		if (!resourceLocation.getPath().startsWith("materialmaps")) {
			return;
		}
		if (!resourceLocation.getPath().contains("/block/")) {
			return;
		}
		
		String name = resourceLocation.getPath().replace("materialmaps/block/", "").replace(".json", "");
		ResourceLocation blockID = new ResourceLocation(resourceLocation.getNamespace(), name);
		
		if (!EmissiveTextureInfo.isEmissiveBlock(blockID)) {
			return;
		}
		
		ResourceManager resourceManager = this.namespacedPacks.get(resourceLocation.getNamespace());
		if (resourceManager != null && !resourceManager.hasResource(resourceLocation)) {
			info.setReturnValue(resourceManager.getResource(bclib_alphaEmissionMaterial));
		}
	}
}
