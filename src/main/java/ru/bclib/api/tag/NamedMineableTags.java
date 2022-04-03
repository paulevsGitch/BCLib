package ru.bclib.api.tag;

import net.minecraft.world.level.block.Block;
import ru.bclib.api.tag.TagAPI.TagLocation;

public class NamedMineableTags {
	public static final TagLocation<Block> AXE = new TagLocation<>("mineable/axe");
	public static final TagLocation<Block> HOE = new TagLocation<>("mineable/hoe");
	public static final TagLocation<Block> PICKAXE = new TagLocation<>("mineable/pickaxe");
	public static final TagLocation<Block> SHEARS = new TagLocation<>("fabric", "mineable/shears");
	public static final TagLocation<Block> SHOVEL = new TagLocation<>("mineable/shovel");
	public static final TagLocation<Block> SWORD = new TagLocation<>("fabric", "mineable/sword");
	public static final TagLocation<Block> HAMMER = new TagLocation<>("c", "mineable/hammer");
}
