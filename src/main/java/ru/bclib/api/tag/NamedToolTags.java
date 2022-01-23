package ru.bclib.api.tag;

import net.minecraft.world.item.Item;
import ru.bclib.api.tag.TagAPI.TagLocation;

public class NamedToolTags {
	public static final TagLocation<Item> FABRIC_AXES = new TagLocation<>("fabric", "axes");
	public static final TagLocation<Item> FABRIC_HOES = new TagLocation<>("fabric", "hoes");
	public static final TagLocation<Item> FABRIC_PICKAXES = new TagLocation<>("fabric", "pickaxes");
	public static final TagLocation<Item> FABRIC_SHEARS = new TagLocation<>("fabric", "shears");
	public static final TagLocation<Item> FABRIC_SHOVELS = new TagLocation<>("fabric", "shovels");
	public static final TagLocation<Item> FABRIC_SWORDS = new TagLocation<>("fabric", "swords");
}
