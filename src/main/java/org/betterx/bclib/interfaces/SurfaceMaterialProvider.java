package org.betterx.bclib.interfaces;

import net.minecraft.world.level.block.state.BlockState;

import org.betterx.bclib.api.surface.SurfaceRuleBuilder;

public interface SurfaceMaterialProvider {
    BlockState getTopMaterial();
    BlockState getUnderMaterial();
    BlockState getAltTopMaterial();

    boolean generateFloorRule();
    SurfaceRuleBuilder surface();
}
