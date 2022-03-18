package ru.bclib.interfaces;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.Collectors;

public interface SurvivesOnBlocks extends SurvivesOnSpecialGround{
    List<Block> getSurvivableBlocks();

    @Override
    default String getSurvivableBlocksString(){
        return getSurvivableBlocks()
                .stream()
                .filter(block -> block!= Blocks.AIR)
                .map(block ->new ItemStack(block).getHoverName().getString())
                .collect(Collectors.joining(", "));
    }

    @Override
    default boolean isSurvivable(BlockState state){
        return getSurvivableBlocks().contains(state.getBlock());
    }
}
