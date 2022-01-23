package ru.bclib.api.tag;

import net.minecraft.world.item.Item;
import ru.bclib.api.tag.TagAPI.TagNamed;

public class CommonItemTags {
	public final static TagNamed<Item> HAMMERS = TagAPI.makeCommonItemTag("hammers");
	public static final TagNamed<Item> BARREL = TagAPI.makeCommonItemTag("barrel");
	public static final TagNamed<Item> CHEST = TagAPI.makeCommonItemTag("chest");
	public static final TagNamed<Item> SHEARS = TagAPI.makeCommonItemTag("shears");
	public static final TagNamed<Item> FURNACES = TagAPI.makeCommonItemTag("furnaces");
	public static final TagNamed<Item> IRON_INGOTS = TagAPI.makeCommonItemTag("iron_ingots");
	public static final TagNamed<Item> LEAVES = TagAPI.makeCommonItemTag("leaves");
	public static final TagNamed<Item> SAPLINGS = TagAPI.makeCommonItemTag("saplings");
	public static final TagNamed<Item> SOUL_GROUND = TagAPI.makeCommonItemTag("soul_ground");
	public static final TagNamed<Item> WOODEN_BARREL = TagAPI.makeCommonItemTag("wooden_barrels");
	public static final TagNamed<Item> WOODEN_CHEST = TagAPI.makeCommonItemTag("wooden_chests");
	public static final TagNamed<Item> WORKBENCHES = TagAPI.makeCommonItemTag("workbench");
}
