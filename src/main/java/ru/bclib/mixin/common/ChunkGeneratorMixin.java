package ru.bclib.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
	private int bclib_featureIteratorSeed;
	
	@ModifyArg(method = "applyBiomeDecoration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldgenRandom;setFeatureSeed(JII)V"))
	private long bclib_updateFeatureSeed(long seed) {
		return Long.rotateRight(seed, bclib_featureIteratorSeed++);
	}
	
	@Inject(method = "applyBiomeDecoration", at = @At("HEAD"))
	private void bclib_obBiomeGenerate(WorldGenLevel level, ChunkPos pos, StructureFeatureManager manager, CallbackInfo ci) {
		bclib_featureIteratorSeed = 0;
	}
}
