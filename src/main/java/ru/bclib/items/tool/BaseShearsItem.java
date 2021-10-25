package ru.bclib.items.tool;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import ru.bclib.api.TagAPI;

public class BaseShearsItem extends ShearsItem {
	public BaseShearsItem(Properties properties) {
		super(properties);
	}
	
	public static boolean isShear(ItemStack itemStack, Item item){
		if (item == Items.SHEARS){
			return itemStack.is(item) | itemStack.is(TagAPI.ITEM_COMMON_SHEARS) || itemStack.is(TagAPI.ITEM_SHEARS);
		} else {
			return itemStack.is(item);
		}
	}
}
