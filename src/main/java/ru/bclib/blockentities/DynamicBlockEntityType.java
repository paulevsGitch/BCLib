package ru.bclib.blockentities;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Sets;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class DynamicBlockEntityType<T extends BlockEntity> extends BlockEntityType<T> {

	private final Set<Block> validBlocks = Sets.newHashSet();

	public DynamicBlockEntityType(Supplier<? extends T> supplier) {
		super(supplier, Collections.emptySet(), null);
	}

	@Override
	public boolean isValid(Block block) {
		return validBlocks.contains(block);
	}

	public void registerBlock(Block block) {
		validBlocks.add(block);
	}
}
