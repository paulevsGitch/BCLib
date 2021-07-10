package ru.bclib.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ru.bclib.registry.BaseBlockEntities;

public class BaseSignBlockEntity extends SignBlockEntity {
	public BaseSignBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(blockPos, blockState);
	}
	
	@Override
	public BlockEntityType<?> getType() {
		return BaseBlockEntities.SIGN;
	}
}