package ru.bclib.recipes;

import com.google.common.collect.Maps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import ru.bclib.util.CollectionsUtil;

import java.util.Map;
import java.util.Map.Entry;

public class BCLRecipeManager {
	private static final Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> RECIPES = Maps.newHashMap();
	
	public static void addRecipe(RecipeType<?> type, Recipe<?> recipe) {
		Map<ResourceLocation, Recipe<?>> list = RECIPES.get(type);
		if (list == null) {
			list = Maps.newHashMap();
			RECIPES.put(type, list);
		}
		list.put(recipe.getId(), recipe);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Recipe<?>> T getRecipe(RecipeType<T> type, ResourceLocation id) {
		if (RECIPES.containsKey(type)) {
			return (T) RECIPES.get(type).get(id);
		}
		return null;
	}
	
	public static Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> getMap(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes) {
		Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> result = Maps.newHashMap();
		
		for (RecipeType<?> type : recipes.keySet()) {
			Map<ResourceLocation, Recipe<?>> typeList = Maps.newHashMap();
			typeList.putAll(recipes.get(type));
			result.put(type, typeList);
		}
		
		for (RecipeType<?> type : RECIPES.keySet()) {
			Map<ResourceLocation, Recipe<?>> list = RECIPES.get(type);
			if (list != null) {
				Map<ResourceLocation, Recipe<?>> typeList = result.get(type);
				if (typeList == null) {
					typeList = Maps.newHashMap();
					result.put(type, typeList);
				}
				for (Entry<ResourceLocation, Recipe<?>> entry : list.entrySet()) {
					ResourceLocation id = entry.getKey();
					if (!typeList.containsKey(id)) typeList.put(id, entry.getValue());
				}
			}
		}
		
		return result;
	}
	
	public static Map<ResourceLocation, Recipe<?>> getMapByName(Map<ResourceLocation, Recipe<?>> recipes) {
		Map<ResourceLocation, Recipe<?>> result = CollectionsUtil.getMutable(recipes);
		RECIPES.values().forEach(map -> map.forEach((location, recipe) -> {
			if (!recipes.containsKey(location)) {
				result.put(location, recipe);
			}
		}));
		return result;
	}
	
	public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S registerSerializer(String modID, String id, S serializer) {
		return Registry.register(Registry.RECIPE_SERIALIZER, modID + ":" + id, serializer);
	}
	
	public static <T extends Recipe<?>> RecipeType<T> registerType(String modID, String type) {
		ResourceLocation recipeTypeId = new ResourceLocation(modID, type);
		return Registry.register(Registry.RECIPE_TYPE, recipeTypeId, new RecipeType<T>() {
			public String toString() {
				return type;
			}
		});
	}
	
	public static boolean exists(ItemLike item) {
		if (item instanceof Block) {
			return Registry.BLOCK.getKey((Block) item) != Registry.BLOCK.getDefaultKey();
		}
		else {
			return Registry.ITEM.getKey(item.asItem()) != Registry.ITEM.getDefaultKey();
		}
	}
	
	public static boolean exists(ItemLike... items) {
		for (ItemLike item : items) {
			if (!exists(item)) {
				return false;
			}
		}
		return true;
	}
}