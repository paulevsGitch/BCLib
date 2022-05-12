package ru.bclib.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.bclib.BCLib;
import ru.bclib.interfaces.NoiseGeneratorSettingsProvider;
import ru.bclib.interfaces.SurfaceProvider;

import java.lang.reflect.Constructor;
import java.util.Optional;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin implements SurfaceProvider, NoiseGeneratorSettingsProvider {
	@Final
	@Shadow
	protected Holder<NoiseGeneratorSettings> settings;
	
	@Final
	@Shadow
	private Aquifer.FluidPicker globalFluidPicker;
	
	private static BlockState bclib_air = Blocks.AIR.defaultBlockState();
	private static Constructor<?> bclib_constructor;

	@Override
	public NoiseGeneratorSettings bclib_getNoiseGeneratorSettings(){
		return settings.value();
	}

	@Shadow protected abstract NoiseChunk createNoiseChunk(ChunkAccess chunkAccess,
														   StructureManager structureManager,
														   Blender blender,
														   RandomState randomState);

	@Override
	@SuppressWarnings("deprecation")
	public BlockState bclib_getSurface(BlockPos pos, Holder<Biome> biome, ServerLevel level) {
		ChunkAccess chunkAccess = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		StructureManager structureManager = level.structureManager();
		NoiseBasedChunkGenerator generator = NoiseBasedChunkGenerator.class.cast(this);
		RandomState randomState = level.getChunkSource().randomState();

		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(ca -> this.createNoiseChunk(ca, structureManager, Blender.empty(), randomState));

		CarvingContext carvingContext = new CarvingContext(generator, level.registryAccess(), chunkAccess.getHeightAccessorForGeneration(), noiseChunk, randomState, this.settings.value().surfaceRule());
		Optional<BlockState> optional = carvingContext.topMaterial(bpos -> biome, chunkAccess, pos, false);
		return optional.isPresent() ? optional.get() : bclib_air;
	}
}
