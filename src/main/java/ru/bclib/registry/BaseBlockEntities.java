package ru.bclib.registry;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ru.bclib.BCLib;
import ru.bclib.blockentities.BaseBarrelBlockEntity;
import ru.bclib.blockentities.BaseChestBlockEntity;
import ru.bclib.blockentities.BaseFurnaceBlockEntity;
import ru.bclib.blockentities.BaseSignBlockEntity;
import ru.bclib.blocks.BaseBarrelBlock;
import ru.bclib.blocks.BaseChestBlock;
import ru.bclib.blocks.BaseFurnaceBlock;
import ru.bclib.blocks.BaseSignBlock;

public class BaseBlockEntities {
	public static final BlockEntityType<BaseChestBlockEntity> CHEST = registerBlockEntityType(BCLib.makeID("chest"),
			BlockEntityType.Builder.of(BaseChestBlockEntity::new, getChests()));
	public static final BlockEntityType<BaseBarrelBlockEntity> BARREL = registerBlockEntityType(BCLib.makeID("barrel"),
			BlockEntityType.Builder.of(BaseBarrelBlockEntity::new, getBarrels()));
	public static final BlockEntityType<BaseSignBlockEntity> SIGN = registerBlockEntityType(BCLib.makeID("sign"),
			BlockEntityType.Builder.of(BaseSignBlockEntity::new, getSigns()));
	public static final BlockEntityType<BaseFurnaceBlockEntity> FURNACE = registerBlockEntityType(BCLib.makeID("furnace"),
			BlockEntityType.Builder.of(BaseFurnaceBlockEntity::new, getFurnaces()));

	public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntityType(ResourceLocation blockId, BlockEntityType.Builder<T> builder) {
		return Registry.register(Registry.BLOCK_ENTITY_TYPE, blockId, builder.build(null));
	}
	
	public static void register() {}

	private static Block[] getChests() {
		List<Block> result = Lists.newArrayList();
		BaseRegistry.getModBlocks().forEach((item) -> {
			if (item instanceof BlockItem) {
				Block block = ((BlockItem) item).getBlock();
				if (block instanceof BaseChestBlock) {
					result.add(block);
				}
			}
		});
		return result.toArray(new Block[] {});
	}

	private static Block[] getBarrels() {
		List<Block> result = Lists.newArrayList();
		BaseRegistry.getModBlocks().forEach((item) -> {
			if (item instanceof BlockItem) {
				Block block = ((BlockItem) item).getBlock();
				if (block instanceof BaseBarrelBlock) {
					result.add(block);
				}
			}
		});
		return result.toArray(new Block[] {});
	}

	private static Block[] getSigns() {
		List<Block> result = Lists.newArrayList();
		BaseRegistry.getModBlocks().forEach((item) -> {
			if (item instanceof BlockItem) {
				Block block = ((BlockItem) item).getBlock();
				if (block instanceof BaseSignBlock) {
					result.add(block);
				}
			}
		});
		return result.toArray(new Block[] {});
	}

	private static Block[] getFurnaces() {
		List<Block> result = Lists.newArrayList();
		BaseRegistry.getModBlocks().forEach((item) -> {
			if (item instanceof BlockItem) {
				Block block = ((BlockItem) item).getBlock();
				if (block instanceof BaseFurnaceBlock) {
					result.add(block);
				}
			}
		});
		return result.toArray(new Block[] {});
	}
}
