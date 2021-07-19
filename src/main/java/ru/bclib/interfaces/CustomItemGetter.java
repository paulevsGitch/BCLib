package ru.bclib.interfaces;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import ru.bclib.registry.BlocksRegistry;

public interface CustomItemGetter {
	/**
	 * Used to replace default Block Item when block is registered.
	 *
	 * @return {@link BlockItem}
	 */
	BlockItem getCustomItem(ResourceLocation blockID, FabricItemSettings settings);
}
