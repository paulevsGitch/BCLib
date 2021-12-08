package ru.bclib.api.biomes;

import net.minecraft.world.level.block.state.BlockState;

public interface SurfaceMaterialProvider {
    BlockState getTopMaterial();
    BlockState getAltTopMaterial();
}
