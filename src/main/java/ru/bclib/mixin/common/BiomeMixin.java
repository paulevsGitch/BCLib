package ru.bclib.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Biome.class)
public class BiomeMixin {
	private int bclib_featureIteratorSeed;
	
	@ModifyArg(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldgenRandom;setFeatureSeed(JII)J"))
	private long bclib_updateFeatureSeed(long seed) {
		return Long.rotateRight(seed, bclib_featureIteratorSeed++);
	}
	
	@Inject(method = "generate", at = @At("HEAD"))
	private void bclib_obBiomeGenerate(StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, WorldGenRegion worldGenRegion, long l, WorldgenRandom worldgenRandom, BlockPos blockPos, CallbackInfo info) {
		bclib_featureIteratorSeed = 0;
	}
}
