package org.betterx.bclib.api.tag;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;


public class NamedToolTags {
    public static final TagKey<Item> FABRIC_AXES = TagAPI.makeItemTag("fabric", "axes");
    public static final TagKey<Item> FABRIC_HOES = TagAPI.makeItemTag("fabric", "hoes");
    public static final TagKey<Item> FABRIC_PICKAXES = TagAPI.makeItemTag("fabric", "pickaxes");
    public static final TagKey<Item> FABRIC_SHEARS = TagAPI.makeItemTag("fabric", "shears");
    public static final TagKey<Item> FABRIC_SHOVELS = TagAPI.makeItemTag("fabric", "shovels");
    public static final TagKey<Item> FABRIC_SWORDS = TagAPI.makeItemTag("fabric", "swords");
}
