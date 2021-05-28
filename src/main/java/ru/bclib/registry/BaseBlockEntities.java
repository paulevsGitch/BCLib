package ru.bclib.registry;

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

	public static Block[] getChests() {
		return BaseRegistry.getRegisteredBlocks().values().stream()
				.filter(item -> item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BaseChestBlock)
				.map(item -> ((BlockItem) item).getBlock()).toArray(Block[]::new);
	}

	private static Block[] getBarrels() {
		return BaseRegistry.getRegisteredBlocks().values().stream()
				.filter(item -> item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BaseBarrelBlock)
				.map(item -> ((BlockItem) item).getBlock()).toArray(Block[]::new);
	}

	public static Block[] getSigns() {
		return BaseRegistry.getRegisteredBlocks().values().stream()
				.filter(item -> item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BaseSignBlock)
				.map(item -> ((BlockItem) item).getBlock()).toArray(Block[]::new);
	}

	private static Block[] getFurnaces() {
		return BaseRegistry.getRegisteredBlocks().values().stream()
				.filter(item -> item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BaseFurnaceBlock)
				.map(item -> ((BlockItem) item).getBlock()).toArray(Block[]::new);
	}
}
