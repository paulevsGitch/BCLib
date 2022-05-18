package org.betterx.bclib.api;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.betterx.bclib.mixin.common.ShovelItemAccessor;

import java.util.Map;

public class ShovelAPI {
    /**
     * Will add left-click behaviour to shovel: when it is targeting cetrain {@link Block} it will be converting to new
     * {@link BlockState} on usage. Example: grass converting to path.
     *
     * @param target  {@link Block} that will be converted.
     * @param convert {@link BlockState} to convert block into.
     */
    public static void addShovelBehaviour(Block target, BlockState convert) {
        Map<Block, BlockState> map = ShovelItemAccessor.bclib_getFlattenables();
        map.put(target, convert);
    }
}
