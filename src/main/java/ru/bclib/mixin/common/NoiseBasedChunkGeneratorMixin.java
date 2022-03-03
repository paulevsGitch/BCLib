package ru.bclib.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSampler;
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
import java.util.function.Supplier;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin implements SurfaceProvider, NoiseGeneratorSettingsProvider {
	@Final
	@Shadow
	private NoiseSampler sampler;
	
	@Final
	@Shadow
	protected Supplier<NoiseGeneratorSettings> settings;
	
	@Final
	@Shadow
	private Aquifer.FluidPicker globalFluidPicker;
	
	private static BlockState bclib_air = Blocks.AIR.defaultBlockState();
	private static Constructor<?> bclib_constructor;

	@Override
	public NoiseGeneratorSettings bclib_getNoiseGeneratorSettings(){
		return settings.get();
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BlockState bclib_getSurface(BlockPos pos, Biome biome, ServerLevel level) {
		ChunkAccess chunkAccess = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		StructureFeatureManager structureFeatureManager = level.structureFeatureManager();
		NoiseBasedChunkGenerator generator = NoiseBasedChunkGenerator.class.cast(this);
		if (bclib_constructor == null) {
			bclib_constructor = Beardifier.class.getConstructors()[0];
			bclib_constructor.setAccessible(true);
		}
		
		Beardifier beardifier = null;
		try {
			beardifier = (Beardifier) bclib_constructor.newInstance(structureFeatureManager, chunkAccess);
		}
		catch (Exception e) {
			BCLib.LOGGER.error(e.getLocalizedMessage());
		}
		
		if (beardifier == null) {
			return bclib_air;
		}
		
		Beardifier finalBeardifier = beardifier;
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(this.sampler, () -> finalBeardifier, this.settings.get(), this.globalFluidPicker, Blender.empty());
		CarvingContext carvingContext = new CarvingContext(generator, level.registryAccess(), chunkAccess.getHeightAccessorForGeneration(), noiseChunk);
		Optional<BlockState> optional = carvingContext.topMaterial(bpos -> biome, chunkAccess, pos, false);
		return optional.isPresent() ? optional.get() : bclib_air;
	}
}
