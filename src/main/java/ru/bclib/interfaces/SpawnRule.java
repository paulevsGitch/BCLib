package ru.bclib.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;

import java.util.Random;import net.minecraft.util.RandomSource;

@FunctionalInterface
public interface SpawnRule<M extends Mob> {
	boolean canSpawn(EntityType<M> type, LevelAccessor world, MobSpawnType spawnReason, BlockPos pos, RandomSource random);
}
