package org.betterx.bclib.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.betterx.bclib.registry.BaseBlockEntities;

public class BaseChestBlockEntity extends ChestBlockEntity {
    public BaseChestBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BaseBlockEntities.CHEST, blockPos, blockState);
    }
}
