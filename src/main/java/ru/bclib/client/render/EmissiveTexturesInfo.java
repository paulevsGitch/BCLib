package ru.bclib.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.BCLib;

import java.util.List;
import java.util.Set;

public class EmissiveTexturesInfo {
	private static final Set<ResourceLocation> EMISSIVE_TEXTURES = Sets.newHashSet();
	private static final List<UVRectangle> EMISSIVE_UVS = Lists.newArrayList();
	private static RenderMaterial emissiveMaterial;
	
	public static void clear() {
		Renderer renderer = RendererAccess.INSTANCE.getRenderer();
		ResourceLocation materialID = BCLib.makeID("emissive");
		if (renderer.materialById(materialID) == null) {
			emissiveMaterial = renderer.materialFinder().clear().disableAo(0, true).emissive(0, true).find();
			renderer.registerMaterial(materialID, emissiveMaterial);
		}
		EMISSIVE_TEXTURES.clear();
		EMISSIVE_UVS.clear();
	}
	
	public static boolean isEmissive(ResourceLocation location) {
		return EMISSIVE_TEXTURES.contains(location);
	}
	
	public static boolean isEmissive(final float u, final float v) {
		for (UVRectangle rect: EMISSIVE_UVS) {
			if (rect.contains(u, v)) {
				return true;
			}
		}
		return false;
		//return EMISSIVE_UVS.parallelStream().anyMatch(uvRectangle -> uvRectangle.contains(u, v));
	}
	
	public static void add(ResourceLocation texture) {
		EMISSIVE_TEXTURES.add(texture);
	}
	
	public static void add(float x1, float y1, float x2, float y2) {
		EMISSIVE_UVS.add(new UVRectangle(x1, y1, x2, y2));
	}
	
	private static class UVRectangle {
		final float x1, x2, y1, y2;
		
		private UVRectangle(float x1, float y1, float x2, float y2) {
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
		}
		
		private boolean contains(float x, float y) {
			return x >= x1 && y >= y1 && x < x2 && y < y2;
		}
	}
	
	public static RenderMaterial getEmissiveMaterial() {
		return emissiveMaterial;
	}
}
