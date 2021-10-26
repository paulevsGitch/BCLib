package ru.bclib.registry;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import ru.bclib.api.TagAPI;
import ru.bclib.blocks.BaseLeavesBlock;
import ru.bclib.blocks.FeatureSaplingBlock;
import ru.bclib.interfaces.CustomItemProvider;

public class BlockRegistry extends BaseRegistry<Block> {
	protected BlockRegistry(CreativeModeTab creativeTab) {
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

		block = Registry.register(Registry.BLOCK, id, block);

		if (block instanceof BaseLeavesBlock){
			TagAPI.addTags(block, TagAPI.BLOCK_LEAVES);
			if (item != null){
				TagAPI.addTags(item, TagAPI.ITEM_LEAVES);
			}
		} else if (block instanceof FeatureSaplingBlock){
			TagAPI.addTags(block, TagAPI.BLOCK_SAPLINGS);
			if (item != null){
				TagAPI.addTags(item, TagAPI.ITEM_SAPLINGS);
			}
		}

		return block;
	}
	
	public Block registerBlockOnly(ResourceLocation id, Block block) {
		return Registry.register(Registry.BLOCK, id, block);
	}
	
	private Item registerBlockItem(ResourceLocation id, Item item) {
		registerItem(id, item, BaseRegistry.getModBlocks(id.getNamespace()));
		return item;
	}
}
