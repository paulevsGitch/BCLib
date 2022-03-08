package ru.bclib.items.tool;


import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import ru.bclib.api.tag.CommonItemTags;
import ru.bclib.api.tag.TagAPI;

public class BaseShearsItem extends ShearsItem {
	public BaseShearsItem(Properties properties) {
		super(properties);
	}

	public static boolean isShear(ItemStack tool){
		return tool.is(Items.SHEARS) | tool.is(CommonItemTags.SHEARS) || TagAPI.isToolWithMineableTag(tool, FabricMineableTags.SHEARS_MINEABLE);
	}

	public static boolean isShear(ItemStack itemStack, Item item){
		if (item == Items.SHEARS){
			//TODO: 1.18.2 see if removing SHEARS_MINEABLE causes any problems... It should not, since it is a Block-Tag
			return itemStack.is(item) | itemStack.is(CommonItemTags.SHEARS);
		} else {
			return itemStack.is(item);
		}
	}
}
