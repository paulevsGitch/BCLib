package ru.bclib.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;

public class TranslationHelper {
	public static void printMissingNames(String modID) {
		List<String> missingNamesEn = Lists.newArrayList();
		List<String> missingNamesRu = Lists.newArrayList();
		
		Gson gson = new Gson();
		InputStream streamEn = TranslationHelper.class.getResourceAsStream("/assets/" + modID + "/lang/en_us.json");
		InputStream streamRu = TranslationHelper.class.getResourceAsStream("/assets/" + modID + "/lang/ru_ru.json");
		JsonObject translationEn = gson.fromJson(new InputStreamReader(streamEn), JsonObject.class);
		JsonObject translationRu = gson.fromJson(new InputStreamReader(streamRu), JsonObject.class);
		
		Registry.BLOCK.forEach(block -> {
			if (Registry.BLOCK.getKey(block).getNamespace().equals(modID)) {
				String name = block.getName().getString();
				if (!translationEn.has(name)) {
					missingNamesEn.add(name);
				}
				if (!translationRu.has(name)) {
					missingNamesRu.add(name);
				}
			}
		});
		
		Registry.ITEM.forEach(item -> {
			if (Registry.ITEM.getKey(item).getNamespace().equals(modID)) {
				String name = item.getDescription().getString();
				if (!translationEn.has(name)) {
					missingNamesEn.add(name);
				}
				if (!translationRu.has(name)) {
					missingNamesRu.add(name);
				}
			}
		});
		
		BuiltinRegistries.BIOME.forEach(biome -> {
			ResourceLocation id = BuiltinRegistries.BIOME.getKey(biome);
			if (id.getNamespace().equals(modID)) {
				String name = "biome." + modID + "." + id.getPath();
				if (!translationEn.has(name)) {
					missingNamesEn.add(name);
				}
				if (!translationRu.has(name)) {
					missingNamesRu.add(name);
				}
			}
		});
		
		Registry.ENTITY_TYPE.forEach((entity) -> {
			ResourceLocation id = Registry.ENTITY_TYPE.getKey(entity);
			if (id.getNamespace().equals(modID)) {
				String name = "entity." + modID + "." + id.getPath();
				if (!translationEn.has(name)) {
					missingNamesEn.add(name);
				}
				if (!translationRu.has(name)) {
					missingNamesRu.add(name);
				}
			}
		});
		
		if (!missingNamesEn.isEmpty() || !missingNamesRu.isEmpty()) {
			
			System.out.println("========================================");
			System.out.println("           MISSING NAMES LIST");
			
			if (!missingNamesEn.isEmpty()) {
				Collections.sort(missingNamesEn);
				System.out.println("========================================");
				System.out.println("                 ENGLISH");
				System.out.println("========================================");
				missingNamesEn.forEach((name) -> {
					System.out.println("	\"" + name + "\": \"" + fastTranslateEn(name) + "\",");
				});
			}
			
			if (!missingNamesRu.isEmpty()) {
				Collections.sort(missingNamesRu);
				System.out.println("========================================");
				System.out.println("                 RUSSIAN");
				System.out.println("========================================");
				missingNamesRu.forEach((name) -> {
					System.out.println("	\"" + name + "\": \"\",");
				});
			}
			
			System.out.println("========================================");
		}
	}
	
	public static String fastTranslateEn(String text) {
		String[] words = text.substring(text.lastIndexOf('.') + 1).split("_");
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			builder.append(Character.toUpperCase(word.charAt(0)));
			builder.append(word, 1, word.length());
			if (i < words.length - 1) {
				builder.append(' ');
			}
		}
		return builder.toString();
	}
}
