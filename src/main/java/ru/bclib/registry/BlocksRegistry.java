package ru.bclib.registry;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.WaterLilyBlockItem;
import net.minecraft.world.level.block.Block;
import ru.bclib.interfaces.ISpetialItem;

public abstract class BlocksRegistry extends BaseRegistry<Block> {

	protected BlocksRegistry(CreativeModeTab creativeTab) {
		super(creativeTab);
	}
	
	@Override
	public Block register(ResourceLocation id, Block block) {
		int maxCount = 64;
		boolean placeOnWater = false;
		if (block instanceof ISpetialItem) {
			ISpetialItem item = (ISpetialItem) block;
			maxCount = item.getStackSize();
			placeOnWater = item.canPlaceOnWater();
		}
		Properties item = makeItemSettings().stacksTo(maxCount);
		if (placeOnWater) {
			registerBlockItem(id, new WaterLilyBlockItem(block, item));
		} else {
			registerBlockItem(id, new BlockItem(block, item));
		}
		if (block.defaultBlockState().getMaterial().isFlammable() && FlammableBlockRegistry.getDefaultInstance().get(block).getBurnChance() == 0) {
			FlammableBlockRegistry.getDefaultInstance().add(block, 5, 5);
		}
		return Registry.register(Registry.BLOCK, id, block);
	}
	
	public Block registerBlockOnly(String name, Block block) {
		return Registry.register(Registry.BLOCK, createModId(name), block);
	}

	public Item registerBlockItem(ResourceLocation id, Item item) {
		registerItem(id, item, BaseRegistry.MOD_BLOCKS);
		return item;
	}
}
