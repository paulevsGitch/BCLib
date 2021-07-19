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
		return BaseRegistry.getRegisteredBlocks().values().stream().filter(item -> item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BaseChestBlock).map(item -> ((BlockItem) item).getBlock()).toArray(Block[]::new);
	}
	
	public static Block[] getBarrels() {
		return BaseRegistry.getRegisteredBlocks().values().stream().filter(item -> item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BaseBarrelBlock).map(item -> ((BlockItem) item).getBlock()).toArray(Block[]::new);
	}
	
	public static Block[] getSigns() {
		return BaseRegistry.getRegisteredBlocks().values().stream().filter(item -> item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BaseSignBlock).map(item -> ((BlockItem) item).getBlock()).toArray(Block[]::new);
	}
	
	public static Block[] getFurnaces() {
		return BaseRegistry.getRegisteredBlocks().values().stream().filter(item -> item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof BaseFurnaceBlock).map(item -> ((BlockItem) item).getBlock()).toArray(Block[]::new);
	}
	
	public static boolean registerSpecialBlock(Block block) {
		if (block instanceof BaseChestBlock) {
			BaseBlockEntities.CHEST.registerBlock(block);
			return true;
		}
		if (block instanceof BaseSignBlock) {
			BaseBlockEntities.SIGN.registerBlock(block);
			return true;
		}
		if (block instanceof BaseBarrelBlock) {
			BaseBlockEntities.BARREL.registerBlock(block);
			return true;
		}
		if (block instanceof BaseFurnaceBlock) {
			BaseBlockEntities.FURNACE.registerBlock(block);
			return true;
		}
		
		return false;
	}
}
