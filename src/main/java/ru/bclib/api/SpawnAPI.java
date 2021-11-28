package ru.bclib.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import ru.bclib.util.BlocksHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Deprecated(forRemoval = true)
public class SpawnAPI<T extends Entity> {
	@FunctionalInterface
	public interface SpawnRule<E extends Entity> {
		boolean test(EntityType<? extends E> type, LevelAccessor world, MobSpawnType spawnReason, BlockPos pos, Random random);
	}
	
	public interface IsBlock{
		boolean is(BlockState state);
	}
	
	private Class<T> entityClass;
	public SpawnAPI(Class<T> entityClass){
		this.entityClass = entityClass;
	}
	
	ArrayList<SpawnRule<T>> rules = new ArrayList<>();
	
	public SpawnAPI<T> notPeaceful() {
		rules.add((type, world, spawnReason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL);
		return this;
	}
	
	public SpawnAPI<T> notPeacefulBelowBrightness() {
		return notPeacefulBelowBrightness(7);
	}
	
	public SpawnAPI<T> notPeacefulBelowBrightness(int bright) {
		rules.add((type, world, spawnReason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL && world.getMaxLocalRawBrightness(pos) <= bright);
		return this;
	}
	
	public SpawnAPI<T> belowBrightness() {
		return belowBrightness(7);
	}
	
	public SpawnAPI<T> belowBrightness(int bright) {
		rules.add((type, world, spawnReason, pos, random) -> world.getMaxLocalRawBrightness(pos) <= bright);
		return this;
	}
	
	public SpawnAPI<T> belowMaxHeight() {
		rules.add((type, world, spawnReason, pos, random) -> pos.getY() >= world.dimensionType().logicalHeight());
		return this;
	}
	
	public SpawnAPI<T> maxAlive(){
		return maxAlive(4, 256);
	}
	
	public SpawnAPI<T> maxAlive(int count){
		return maxAlive(count, 256);
	}
	
	public SpawnAPI<T> maxAlive(int count, int size){
		rules.add((type, world, spawnReason, pos, random) -> {
			try {
				final AABB box = new AABB(pos).inflate(size, 256, size);
				final List<T> list = world.getEntitiesOfClass(entityClass, box, (entity) -> true);
				return list.size() < count;
			}
			catch (Exception e) {
				return true;
			}
		});
		return this;
	}
	
	public SpawnAPI<T> maxHeight(int height) {
		rules.add((type, world, spawnReason, pos, random) -> {
			int h = BlocksHelper.downRay(world, pos, height+1);
			return h<=height;
		});
		return this;
	}
	
	public SpawnAPI<T> notAboveBlock(IsBlock blockTest, int height) {
		rules.add((type, world, spawnReason, pos, random) -> {
			int h = BlocksHelper.downRay(world, pos, height+1);
			if  (h>height) return false;
			
			for (int i = 1; i <= h; i++)
				if (blockTest.is(world.getBlockState(pos.below(i))))
					return false;
				
			return true;
		});
		return this;
	}
	
	public SpawnAPI<T> aboveBlock(IsBlock blockTest, int height) {
		rules.add((type, world, spawnReason, pos, random) -> {
			int h = BlocksHelper.downRay(world, pos, height+1);
			if  (h>height) return false;
			
			for (int i = 1; i <= h; i++)
				if (blockTest.is(world.getBlockState(pos.below(i))))
					return true;
			
			return false;
		});
		return this;
	}
	
	public boolean canSpawn(EntityType<? extends T> type, LevelAccessor world, MobSpawnType spawnReason, BlockPos pos, Random random) {
		return rules.stream()
					.map(r -> r.test(type, world, spawnReason, pos, random))
					.reduce(true, (p, c) -> p && c);
	}
}
