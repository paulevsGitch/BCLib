package ru.bclib.registry;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import ru.bclib.interfaces.CustomItemProvider;

public abstract class BlocksRegistry extends BaseRegistry<Block> {
	
	protected BlocksRegistry(CreativeModeTab creativeTab) {
		super(creativeTab);
	}
	
	@Override
	public Block register(ResourceLocation id, Block block) {
		BlockItem item = null;
		if (block instanceof CustomItemProvider) {
			item = ((CustomItemProvider) block).getCustomItem(id, makeItemSettings());
		}
		else {
			item = new BlockItem(block, makeItemSettings());
		}
		registerBlockItem(id, item);
		if (block.defaultBlockState().getMaterial().isFlammable() && FlammableBlockRegistry.getDefaultInstance()
																						   .get(block)
																						   .getBurnChance() == 0) {
			FlammableBlockRegistry.getDefaultInstance().add(block, 5, 5);
		}
		return Registry.register(Registry.BLOCK, id, block);
	}
	
	public Block registerBlockOnly(String name, Block block) {
		return Registry.register(Registry.BLOCK, createModId(name), block);
	}
	
	public Item registerBlockItem(ResourceLocation id, Item item) {
		registerItem(id, item, BaseRegistry.getModBlocks(id.getNamespace()));
		return item;
	}
}
