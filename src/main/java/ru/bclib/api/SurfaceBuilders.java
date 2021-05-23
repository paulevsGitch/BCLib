package ru.bclib.api;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;

public class SurfaceBuilders {
	public static SurfaceBuilder<SurfaceBuilderBaseConfiguration> register(String name, SurfaceBuilder<SurfaceBuilderBaseConfiguration> builder) {
		return Registry.register(Registry.SURFACE_BUILDER, name, builder);
	}
	
	public static SurfaceBuilderBaseConfiguration makeSimpleConfig(Block block) {
		BlockState state = block.defaultBlockState();
		return new SurfaceBuilderBaseConfiguration(state, state, state);
	}
	
	public static void register() {}
}
