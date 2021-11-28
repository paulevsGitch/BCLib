package ru.bclib.api.spawning;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.mixin.object.builder.SpawnRestrictionAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements.SpawnPredicate;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public class SpawnRuleBulder<M extends Mob> {
	private static final Map<String, SpawnRuleEntry> RULES_CACHE = Maps.newHashMap();
	private static final SpawnRuleBulder INSTANCE = new SpawnRuleBulder();
	private List<SpawnRuleEntry> rules = Lists.newArrayList();
	private SpawnRuleEntry entryInstance;
	private EntityType<M> entityType;
	
	private SpawnRuleBulder() {}
	
	/**
	 * Starts new rule building process.
	 * @return prepared {@link SpawnRuleBulder} instance
	 */
	public static SpawnRuleBulder start(EntityType<? extends Mob> entityType) {
		INSTANCE.entityType = entityType;
		INSTANCE.rules.clear();
		return INSTANCE;
	}
	
	/**
	 * Stop entity spawn on peaceful {@link Difficulty}
	 * @return same {@link SpawnRuleBulder} instance
	 */
	public SpawnRuleBulder notPeaceful() {
		entryInstance = getFromCache("not_peaceful", () -> {
			return new SpawnRuleEntry(0, (type, world, spawnReason, pos, random) -> world.getDifficulty() != Difficulty.PEACEFUL);
		});
		rules.add(entryInstance);
		return this;
	}
	
	/**
	 * Restricts entity spawn above world surface (flying mobs).
	 * @param minHeight minimal spawn height.
	 * @return same {@link SpawnRuleBulder} instance
	 */
	public SpawnRuleBulder aboveGround(int minHeight) {
		entryInstance = getFromCache("above_ground", () -> {
			return new SpawnRuleEntry(0, (type, world, spawnReason, pos, random) -> {
				if (pos.getY() < world.getMinBuildHeight() + 2) {
					return false;
				}
				return pos.getY() > world.getHeight(Types.WORLD_SURFACE, pos.getX(), pos.getZ()) + minHeight;
			});
		});
		rules.add(entryInstance);
		return this;
	}
	
	/**
	 * Restricts entity spawn below world logical height (useful for Nether mobs).
	 * @return same {@link SpawnRuleBulder} instance
	 */
	public SpawnRuleBulder belowMaxHeight() {
		entryInstance = getFromCache("below_max_height", () -> {
			return new SpawnRuleEntry(0, (type, world, spawnReason, pos, random) -> pos.getY() < world.dimensionType().logicalHeight());
		});
		rules.add(entryInstance);
		return this;
	}
	
	/**
	 * Will spawn entity with 1 / chance probability (randomly).
	 * @param chance probability limit.
	 * @return same {@link SpawnRuleBulder} instance
	 */
	public SpawnRuleBulder withChance(int chance) {
		entryInstance = getFromCache("with_chance_" + chance, () -> {
			return new SpawnRuleEntry(1, (type, world, spawnReason, pos, random) -> random.nextInt(chance) == 0);
		});
		rules.add(entryInstance);
		return this;
	}
	
	/**
	 * Will spawn entity only below specified brightness value.
	 * @param lightLevel light level upper limit.
	 * @return same {@link SpawnRuleBulder} instance
	 */
	public SpawnRuleBulder belowBrightness(int lightLevel) {
		entryInstance = getFromCache("below_brightness_" + lightLevel, () -> {
			return new SpawnRuleEntry(2, (type, world, spawnReason, pos, random) -> world.getMaxLocalRawBrightness(pos) <= lightLevel);
		});
		rules.add(entryInstance);
		return this;
	}
	
	/**
	 * Will spawn entity only above specified brightness value.
	 * @param lightLevel light level lower limit.
	 * @return same {@link SpawnRuleBulder} instance
	 */
	public SpawnRuleBulder aboveBrightness(int lightLevel) {
		entryInstance = getFromCache("above_brightness_" + lightLevel, () -> {
			return new SpawnRuleEntry(2, (type, world, spawnReason, pos, random) -> world.getMaxLocalRawBrightness(pos) >= lightLevel);
		});
		rules.add(entryInstance);
		return this;
	}
	
	/**
	 * Entity spawn will follow common vanilla spawn rules - spawn in darkness and not on peaceful level.
	 * @param lightLevel light level upper limit.
	 * @return same {@link SpawnRuleBulder} instance
	 */
	public SpawnRuleBulder hostile(int lightLevel) {
		return notPeaceful().belowBrightness(lightLevel);
	}
	
	/**
	 * Entity spawn will follow common vanilla spawn rules - spawn in darkness (below light level 7) and not on peaceful level.
	 * @return same {@link SpawnRuleBulder} instance
	 */
	public SpawnRuleBulder vanillaHostile() {
		return hostile(7);
	}
	
	/**
	 * Will spawn entity only if count of nearby entities will be lower than specified.
	 * @param selectorType selector {@link EntityType} to search.
	 * @param count max entity count.
	 * @param side side of box to search in.
	 * @return same {@link SpawnRuleBulder} instance
	 */
	public SpawnRuleBulder maxNearby(EntityType<?> selectorType, int count, int side) {
		final Class<? extends Entity> baseClass = selectorType.getBaseClass();
		entryInstance = getFromCache("max_nearby_" + selectorType.getDescriptionId(), () -> {
			return new SpawnRuleEntry(3, (type, world, spawnReason, pos, random) -> {
				try {
					final AABB box = new AABB(pos).inflate(side, world.getHeight(), side);
					final List<?> list = world.getEntitiesOfClass(baseClass, box, (entity) -> true);
					return list.size() < count;
				}
				catch (Exception e) {
					return true;
				}
			});
		});
		rules.add(entryInstance);
		return this;
	}
	
	/**
	 * Will spawn entity only if count of nearby entities with same type will be lower than specified.
	 * @param count max entity count.
	 * @param side side of box to search in.
	 * @return same {@link SpawnRuleBulder} instance
	 */
	public SpawnRuleBulder maxNearby(int count, int side) {
		return maxNearby(entityType, count, side);
	}
	
	/**
	 * Will spawn entity only if count of nearby entities with same type will be lower than specified.
	 * @param count max entity count.
	 * @return same {@link SpawnRuleBulder} instance
	 */
	public SpawnRuleBulder maxNearby(int count) {
		return maxNearby(entityType, count, 256);
	}
	
	/**
	 * Finalize spawning rule creation.
	 * @param spawnType {@link Type} of spawn.
	 * @param heightmapType {@link Types} heightmap type.
	 */
	public void build(Type spawnType, Types heightmapType) {
		final List<SpawnRuleEntry> rulesCopy = Lists.newArrayList(this.rules);
		Collections.sort(rulesCopy);
		
		SpawnPredicate<M> predicate = (entityType, serverLevelAccessor, mobSpawnType, blockPos, random) -> {
			for (SpawnRuleEntry rule: rulesCopy) {
				if (!rule.canSpawn(entityType, serverLevelAccessor, mobSpawnType, blockPos, random)) {
					return false;
				}
			}
			return true;
		};
		
		SpawnRestrictionAccessor.callRegister(entityType, spawnType, heightmapType, predicate);
	}
	
	/**
	 * Finalize spawning rule creation with No Restrictions spawn type, useful for flying entities.
	 * @param heightmapType {@link Types} heightmap type.
	 */
	public void buildNoRestrictions(Types heightmapType) {
		build(Type.NO_RESTRICTIONS, heightmapType);
	}
	
	/**
	 * Finalize spawning rule creation with On Ground spawn type, useful for common entities.
	 * @param heightmapType {@link Types} heightmap type.
	 */
	public void buildOnGround(Types heightmapType) {
		build(Type.ON_GROUND, heightmapType);
	}
	
	/**
	 * Finalize spawning rule creation with In Water spawn type, useful for water entities.
	 * @param heightmapType {@link Types} heightmap type.
	 */
	public void buildInWater(Types heightmapType) {
		build(Type.IN_WATER, heightmapType);
	}
	
	/**
	 * Finalize spawning rule creation with In Lava spawn type, useful for lava entities.
	 * @param heightmapType {@link Types} heightmap type.
	 */
	public void buildInLava(Types heightmapType) {
		build(Type.IN_LAVA, heightmapType);
	}
	
	/**
	 * Internal function, will take entry from cache or create it if necessary.
	 * @param name {@link String} entry internal name.
	 * @param supplier {@link Supplier} for {@link SpawnRuleEntry}.
	 * @return new or existing {@link SpawnRuleEntry}.
	 */
	private static SpawnRuleEntry getFromCache(String name, Supplier<SpawnRuleEntry> supplier) {
		SpawnRuleEntry entry = RULES_CACHE.get(name);
		if (entry == null) {
			entry = supplier.get();
			RULES_CACHE.put(name, entry);
		}
		return entry;
	}
}
