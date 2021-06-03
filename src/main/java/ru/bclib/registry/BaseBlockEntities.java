package ru.bclib.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import ru.bclib.BCLib;
import ru.bclib.blockentities.*;
import ru.bclib.blocks.BaseBarrelBlock;
import ru.bclib.blocks.BaseChestBlock;
import ru.bclib.blocks.BaseFurnaceBlock;
import ru.bclib.blocks.BaseSignBlock;

import java.util.Arrays;
import java.util.function.Supplier;

public class BaseBlockEntities {
	public static final DynamicBlockEntityType<BaseChestBlockEntity> CHEST = registerBlockEntityType(BCLib.makeID("chest"), BaseChestBlockEntity::new);
	public static final DynamicBlockEntityType<BaseBarrelBlockEntity> BARREL = registerBlockEntityType(BCLib.makeID("barrel"), BaseBarrelBlockEntity::new);
	public static final DynamicBlockEntityType<BaseSignBlockEntity> SIGN = registerBlockEntityType(BCLib.makeID("sign"), BaseSignBlockEntity::new);
	public static final DynamicBlockEntityType<BaseFurnaceBlockEntity> FURNACE = registerBlockEntityType(BCLib.makeID("furnace"), BaseFurnaceBlockEntity::new);

	public static <T extends BlockEntity> DynamicBlockEntityType<T> registerBlockEntityType(ResourceLocation typeId, Supplier<? extends T> supplier) {
		return Registry.register(Registry.BLOCK_ENTITY_TYPE, typeId, new DynamicBlockEntityType<>(supplier));
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
