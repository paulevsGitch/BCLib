package ru.bclib.interfaces;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;

public interface ColorProviderGetter {
	BlockColor getProvider();
	
	ItemColor getItemProvider();
}
