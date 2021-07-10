package ru.bclib.client.models;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PatternsHelper {
	public static Optional<String> createItemGenerated(ResourceLocation itemId) {
		return createJson(BasePatterns.ITEM_GENERATED, itemId);
	}
	
	public static Optional<String> createItemHandheld(ResourceLocation itemId) {
		return createJson(BasePatterns.ITEM_HANDHELD, itemId);
	}
	
	public static Optional<String> createBlockSimple(ResourceLocation blockId) {
		return createJson(BasePatterns.BLOCK_BASE, blockId);
	}
	
	public static Optional<String> createBlockEmpty(ResourceLocation blockId) {
		return createJson(BasePatterns.BLOCK_EMPTY, blockId);
	}
	
	public static Optional<String> createBlockPillar(ResourceLocation blockId) {
		return createJson(BasePatterns.BLOCK_PILLAR, blockId);
	}
	
	public static Optional<String> createBlockBottomTop(ResourceLocation blockId) {
		return createJson(BasePatterns.BLOCK_BOTTOM_TOP, blockId);
	}
	
	public static Optional<String> createBlockColored(ResourceLocation blockId) {
		return createJson(BasePatterns.BLOCK_COLORED, blockId);
	}
	
	public static Optional<String> createJson(ResourceLocation patternId, ResourceLocation blockId) {
		Map<String, String> textures = Maps.newHashMap();
		textures.put("%modid%", blockId.getNamespace());
		textures.put("%texture%", blockId.getPath());
		return createJson(patternId, textures);
	}
	
	public static Optional<String> createJson(ResourceLocation patternId, Map<String, String> textures) {
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		try (InputStream input = resourceManager.getResource(patternId).getInputStream()) {
			String json = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines().collect(Collectors.joining());
			for (Map.Entry<String, String> texture : textures.entrySet()) {
				json = json.replace(texture.getKey(), texture.getValue());
			}
			return Optional.of(json);
		}
		catch (Exception ex) {
			return Optional.empty();
		}
	}
}
