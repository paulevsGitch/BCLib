package ru.bclib.items.tool;

import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import ru.bclib.api.tag.CommonItemTags;

public class BaseShearsItem extends ShearsItem {
	public BaseShearsItem(Properties properties) {
		super(properties);
	}

	public static boolean isShear(ItemStack tool){
		return tool.is(Items.SHEARS) | tool.is(CommonItemTags.SHEARS) || tool.is(FabricToolTags.SHEARS);
	}

	public static boolean isShear(ItemStack itemStack, Item item){
		if (item == Items.SHEARS){
			return itemStack.is(item) | itemStack.is(CommonItemTags.SHEARS) || itemStack.is(FabricToolTags.SHEARS);
		} else {
			return itemStack.is(item);
		}
	}
}
