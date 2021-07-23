package ru.bclib.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import ru.bclib.BCLib;
import ru.bclib.blockentities.BaseBarrelBlockEntity;
import ru.bclib.blockentities.BaseChestBlockEntity;
import ru.bclib.blockentities.BaseFurnaceBlockEntity;
import ru.bclib.blockentities.BaseSignBlockEntity;
import ru.bclib.blockentities.DynamicBlockEntityType;
import ru.bclib.blockentities.DynamicBlockEntityType.BlockEntitySupplier;
import ru.bclib.blocks.BaseBarrelBlock;
import ru.bclib.blocks.BaseChestBlock;
import ru.bclib.blocks.BaseFurnaceBlock;
import ru.bclib.blocks.BaseSignBlock;

public class BaseBlockEntities {
	public static final DynamicBlockEntityType<BaseChestBlockEntity> CHEST = registerBlockEntityType(BCLib.makeID("chest"), BaseChestBlockEntity::new);
	public static final DynamicBlockEntityType<BaseBarrelBlockEntity> BARREL = registerBlockEntityType(BCLib.makeID("barrel"), BaseBarrelBlockEntity::new);
	public static final DynamicBlockEntityType<BaseSignBlockEntity> SIGN = registerBlockEntityType(BCLib.makeID("sign"), BaseSignBlockEntity::new);
	public static final DynamicBlockEntityType<BaseFurnaceBlockEntity> FURNACE = registerBlockEntityType(BCLib.makeID("furnace"), BaseFurnaceBlockEntity::new);
	
	public static <T extends BlockEntity> DynamicBlockEntityType<T> registerBlockEntityType(ResourceLocation typeId, BlockEntitySupplier<? extends T> supplier) {
		return Registry.register(Registry.BLOCK_ENTITY_TYPE, typeId, new DynamicBlockEntityType<>(supplier));
	}
	
	public static void register() {}
	
	public static Block[] getChests() {
		return Registry.BLOCK
			.stream()
			.filter(block -> block instanceof BaseChestBlock)
			.toArray(Block[]::new);
	}
	
	public static Block[] getBarrels() {
		return Registry.BLOCK
			.stream()
			.filter(block -> block instanceof BaseBarrelBlock)
			.toArray(Block[]::new);
	}
	
	public static Block[] getSigns() {
		return Registry.BLOCK
			.stream()
			.filter(block -> block instanceof BaseSignBlock)
			.toArray(Block[]::new);
	}
	
	public static Block[] getFurnaces() {
		return Registry.BLOCK
			.stream()
			.filter(block -> block instanceof BaseFurnaceBlock)
			.toArray(Block[]::new);
	}
}
