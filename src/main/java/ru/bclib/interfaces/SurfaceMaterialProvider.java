package ru.bclib.interfaces;

import net.minecraft.world.level.block.state.BlockState;

public interface SurfaceMaterialProvider {
	BlockState getTopMaterial();
	BlockState getUnderMaterial();
	BlockState getAltTopMaterial();
}
