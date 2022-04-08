package ru.bclib.api.tag;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CommonItemTags {
	public final static TagKey<Item> HAMMERS = TagAPI.makeCommonItemTag("hammers");
	public static final TagKey<Item> BARREL = TagAPI.makeCommonItemTag("barrel");
	public static final TagKey<Item> CHEST = TagAPI.makeCommonItemTag("chest");
	public static final TagKey<Item> SHEARS = TagAPI.makeCommonItemTag("shears");
	public static final TagKey<Item> FURNACES = TagAPI.makeCommonItemTag("furnaces");
	public static final TagKey<Item> IRON_INGOTS = TagAPI.makeCommonItemTag("iron_ingots");
	public static final TagKey<Item> LEAVES = TagAPI.makeCommonItemTag("leaves");
	public static final TagKey<Item> SAPLINGS = TagAPI.makeCommonItemTag("saplings");
	public static final TagKey<Item> SOUL_GROUND = TagAPI.makeCommonItemTag("soul_ground");
	public static final TagKey<Item> WOODEN_BARREL = TagAPI.makeCommonItemTag("wooden_barrels");
	public static final TagKey<Item> WOODEN_CHEST = TagAPI.makeCommonItemTag("wooden_chests");
	public static final TagKey<Item> WORKBENCHES = TagAPI.makeCommonItemTag("workbench");
}
