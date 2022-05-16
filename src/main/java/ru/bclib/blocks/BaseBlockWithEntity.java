package ru.bclib.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.bclib.interfaces.LootProvider;

public class BaseBlockWithEntity extends BaseEntityBlock implements LootProvider {
	public BaseBlockWithEntity(Properties settings) {
		super(settings);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return null;
	}
}
