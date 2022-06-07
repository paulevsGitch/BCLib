package org.betterx.bclib.api.v2.tag;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;


public class NamedMineableTags {
    public static final TagKey<Block> AXE = BlockTags.MINEABLE_WITH_AXE;
    public static final TagKey<Block> HOE = BlockTags.MINEABLE_WITH_HOE;
    public static final TagKey<Block> PICKAXE = BlockTags.MINEABLE_WITH_PICKAXE;
    public static final TagKey<Block> SHEARS = TagAPI.makeBlockTag("fabric", "mineable/shears");
    public static final TagKey<Block> SHOVEL = BlockTags.MINEABLE_WITH_SHOVEL;
    public static final TagKey<Block> SWORD = TagAPI.makeBlockTag("fabric", "mineable/sword");
    public static final TagKey<Block> HAMMER = TagAPI.makeCommonBlockTag("mineable/hammer");
}
