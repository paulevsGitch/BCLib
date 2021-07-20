package ru.bclib.mixin.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.fabricmc.fabric.impl.client.texture.FabricSprite;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite.Info;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.BCLib;
import ru.bclib.client.render.EmissiveTexturesInfo;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {
	private boolean bclib_hardCompatMode = false;
	private boolean bclib_modifyAtlas;
	
	@Inject(method = "<init>*", at = @At("TAIL"))
	private void bclib_onAtlasInit(ResourceLocation resourceLocation, CallbackInfo info) {
		boolean hasOptifine = FabricLoader.getInstance().isModLoaded("optifabric");
		bclib_modifyAtlas = !bclib_hardCompatMode && !hasOptifine && resourceLocation.toString().equals("minecraft:textures/atlas/blocks.png");
	}
	
	@Inject(method = "getBasicSpriteInfos", at = @At("HEAD"))
	private void bclib_getBasicSpriteInfos(ResourceManager resourceManager, Set<ResourceLocation> set, CallbackInfoReturnable<Collection<Info>> info) {
		if (bclib_modifyAtlas) {
			EmissiveTexturesInfo.clear();
			EmissiveTexturesInfo.add(new ResourceLocation("minecraft:block/redstone_block"));
			EmissiveTexturesInfo.add(new ResourceLocation("minecraft:block/diamond_block"));
			List<ResourceLocation> locations = Lists.newArrayList();
			set.forEach(location -> {
				ResourceLocation emissiveLocation = new ResourceLocation(
					location.getNamespace(),
					"textures/" + location.getPath() + "_e.png"
				);
				if (resourceManager.hasResource(emissiveLocation)) {
					ResourceLocation texture = new ResourceLocation(location.getNamespace(), location.getPath() + "_e");
					EmissiveTexturesInfo.add(texture);
					locations.add(texture);
				}
			});
			set.addAll(locations);
		}
	}
	
	@Inject(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$Info;IIIII)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", at = @At("HEAD"), cancellable = true)
	private void bclib_loadSprite(ResourceManager resourceManager, TextureAtlasSprite.Info spriteInfo, int atlasWidth, int atlasHeight, int maxLevel, int posX, int posY, CallbackInfoReturnable<TextureAtlasSprite> info) {
		if (!bclib_modifyAtlas) {
			return;
		}
		
		ResourceLocation location = spriteInfo.name();
		if (EmissiveTexturesInfo.isEmissive(location)) {
			float x1 = (float) posX / atlasWidth;
			float y1 = (float) posY / atlasHeight;
			float x2 = x1 + (float) spriteInfo.width() / atlasWidth;
			float y2 = y1 + (float) spriteInfo.height() / atlasHeight;
			EmissiveTexturesInfo.add(x1, y1, x2, y2);
		}
		
		if (!bclib_hardCompatMode) {
			return;
		}
		
		if (bclib_modifyAtlas && location.getPath().startsWith("block")) {
			ResourceLocation emissiveLocation = new ResourceLocation(
				location.getNamespace(),
				"textures/" + location.getPath() + "_e.png"
			);
			if (resourceManager.hasResource(emissiveLocation)) {
				NativeImage sprite = null;
				NativeImage emission = null;
				try {
					ResourceLocation spriteLocation = new ResourceLocation(
						location.getNamespace(),
						"textures/" + location.getPath() + ".png"
					);
					Resource resource = resourceManager.getResource(spriteLocation);
					sprite = NativeImage.read(resource.getInputStream());
					resource.close();
					
					resource = resourceManager.getResource(emissiveLocation);
					emission = NativeImage.read(resource.getInputStream());
					resource.close();
				}
				catch (IOException e) {
					BCLib.LOGGER.warning(e.getMessage());
				}
				if (sprite != null && emission != null) {
					int width = Math.min(sprite.getWidth(), emission.getWidth());
					int height = Math.min(sprite.getHeight(), emission.getHeight());
					for (int x = 0; x < width; x++) {
						for (int y = 0; y < height; y++) {
							int argb = emission.getPixelRGBA(x, y);
							int alpha = (argb >> 24) & 255;
							if (alpha > 127) {
								int r = (argb >> 16) & 255;
								int g = (argb >> 8) & 255;
								int b = argb & 255;
								if (r > 0 || g > 0 || b > 0) {
									argb = (argb & 0x00FFFFFF) | (250 << 24);
									sprite.setPixelRGBA(x, y, argb);
								}
							}
						}
					}
					TextureAtlas self = (TextureAtlas) (Object) this;
					FabricSprite result = new FabricSprite(
						self,
						spriteInfo,
						maxLevel,
						atlasWidth,
						atlasHeight,
						posX,
						posY,
						sprite
					);
					info.setReturnValue(result);
				}
			}
		}
	}
}
