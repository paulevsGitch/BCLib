package ru.bclib.world.surface;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import ru.bclib.noise.OpenSimplexNoise;
import ru.bclib.util.MHelper;

import java.util.Random;

public class DoubleBlockSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	private static final OpenSimplexNoise NOISE = new OpenSimplexNoise(4141);
	private SurfaceBuilderBaseConfiguration config1;
	private SurfaceBuilderBaseConfiguration config2;
	
	private DoubleBlockSurfaceBuilder() {
		super(SurfaceBuilderBaseConfiguration.CODEC);
	}
	
	public DoubleBlockSurfaceBuilder setBlock1(Block block) {
		BlockState stone = Blocks.END_STONE.defaultBlockState();
		config1 = new SurfaceBuilderBaseConfiguration(block.defaultBlockState(), stone, stone);
		return this;
	}
	
	public DoubleBlockSurfaceBuilder setBlock2(Block block) {
		BlockState stone = Blocks.END_STONE.defaultBlockState();
		config2 = new SurfaceBuilderBaseConfiguration(block.defaultBlockState(), stone, stone);
		return this;
	}
	
	public static DoubleBlockSurfaceBuilder register(String name) {
		return Registry.register(Registry.SURFACE_BUILDER, name, new DoubleBlockSurfaceBuilder());
	}
	
	public ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> configured() {
		BlockState stone = Blocks.END_STONE.defaultBlockState();
		return this.configured(new SurfaceBuilderBaseConfiguration(config1.getTopMaterial(), stone, stone));
	}
	
	@Override
	public void apply(Random random, ChunkAccess chunkAccess, Biome biome, int x, int z, int height, double noise, BlockState defaultBlock, BlockState defaultFluid, int l, int m, long seed, SurfaceBuilderBaseConfiguration surfaceBuilderConfiguration) {
		noise = NOISE.eval(x * 0.1, z * 0.1) + MHelper.randRange(-0.4, 0.4, random);
		SurfaceBuilder.DEFAULT.apply(
			random,
			chunkAccess,
			biome,
			x,
			z,
			height,
			noise,
			defaultBlock,
			defaultFluid,
			l,
			m,
			seed,
			noise > 0 ? config1 : config2
		);
	}
}