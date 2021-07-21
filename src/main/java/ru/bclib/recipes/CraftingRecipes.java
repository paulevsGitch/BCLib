package ru.bclib.recipes;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import ru.bclib.BCLib;
import ru.bclib.api.TagAPI;
import ru.bclib.config.Configs;

public class CraftingRecipes {
	public static void init() {
		GridRecipe.make(BCLib.MOD_ID, "tag_smith_table", Blocks.SMITHING_TABLE)
				  .setShape("II", "##", "##")
				  .addMaterial('#', ItemTags.PLANKS)
				  .addMaterial('I', TagAPI.ITEM_IRON_INGOTS)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
		GridRecipe.make(BCLib.MOD_ID, "tag_cauldron", Blocks.CAULDRON)
				  .setShape("I I", "I I", "III")
				  .addMaterial('I', TagAPI.ITEM_IRON_INGOTS)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
		GridRecipe.make(BCLib.MOD_ID, "tag_hopper", Blocks.HOPPER)
				  .setShape("I I", "ICI", " I ")
				  .addMaterial('I', TagAPI.ITEM_IRON_INGOTS)
				  .addMaterial('C', TagAPI.ITEM_CHEST)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
		GridRecipe.make(BCLib.MOD_ID, "tag_piston", Blocks.PISTON)
				  .setShape("WWW", "CIC", "CDC")
				  .addMaterial('I', TagAPI.ITEM_IRON_INGOTS)
				  .addMaterial('D', Items.REDSTONE)
				  .addMaterial('C', Items.COBBLESTONE)
				  .addMaterial('W', ItemTags.PLANKS)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
		GridRecipe.make(BCLib.MOD_ID, "tag_rail", Blocks.RAIL)
				  .setOutputCount(16)
				  .setShape("I I", "ISI", "I I")
				  .addMaterial('I', TagAPI.ITEM_IRON_INGOTS)
				  .addMaterial('S', Items.STICK)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
		GridRecipe.make(BCLib.MOD_ID, "tag_stonecutter", Blocks.STONECUTTER)
				  .setShape(" I ", "SSS")
				  .addMaterial('I', TagAPI.ITEM_IRON_INGOTS)
				  .addMaterial('S', Items.STONE)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
		GridRecipe.make(BCLib.MOD_ID, "tag_bucket", Items.BUCKET)
				  .setShape("I I", " I ")
				  .addMaterial('I', TagAPI.ITEM_IRON_INGOTS)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
		GridRecipe.make(BCLib.MOD_ID, "tag_compass", Items.COMPASS)
				  .setShape(" I ", "IDI", " I ")
				  .addMaterial('I', TagAPI.ITEM_IRON_INGOTS)
				  .addMaterial('D', Items.REDSTONE)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
		GridRecipe.make(BCLib.MOD_ID, "tag_minecart", Items.MINECART)
				  .setShape("I I", "III")
				  .addMaterial('I', TagAPI.ITEM_IRON_INGOTS)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
		GridRecipe.make(BCLib.MOD_ID, "tag_shield", Items.SHIELD)
				  .setShape("WIW", "WWW", " W ")
				  .addMaterial('I', TagAPI.ITEM_IRON_INGOTS)
				  .addMaterial('W', ItemTags.PLANKS)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
		
		GridRecipe.make(BCLib.MOD_ID, "tag_hopper", Blocks.HOPPER)
				  .setShape("I I", "ICI", " I ")
				  .addMaterial('I', TagAPI.ITEM_IRON_INGOTS)
				  .addMaterial('C', TagAPI.ITEM_CHEST)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
		
		GridRecipe.make(BCLib.MOD_ID, "tag_shulker_box", Blocks.SHULKER_BOX)
				  .setShape("S", "C", "S")
				  .addMaterial('S', Items.SHULKER_SHELL)
				  .addMaterial('C', TagAPI.ITEM_CHEST)
				  .checkConfig(Configs.RECIPE_CONFIG)
				  .build();
	}
}
