package ru.bclib.world.biomes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import org.jetbrains.annotations.Nullable;
import ru.bclib.BCLib;
import ru.bclib.api.biomes.BiomeAPI;
import ru.bclib.api.tag.TagAPI;
import ru.bclib.util.WeightedList;

import java.util.List;
import java.util.Map;
import java.util.Random;import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BCLBiome extends BCLBiomeSettings {
	private final Set<TagKey<Biome>> structureTags = Sets.newHashSet();
	private final WeightedList<BCLBiome> subbiomes = new WeightedList<>();
	private final Map<String, Object> customData = Maps.newHashMap();
	private final ResourceLocation biomeID;
	private final Biome biome;

	private final List<Climate.ParameterPoint> parameterPoints = Lists.newArrayList();
	
	private Consumer<Holder<Biome>> surfaceInit;
	private BCLBiome biomeParent;
	
	/**
	 * Create wrapper for existing biome using its {@link ResourceLocation} identifier.
	 * @param biomeKey {@link ResourceKey} for the {@link Biome}.
	 */
	public BCLBiome(ResourceKey<Biome> biomeKey) {
		this(biomeKey.location());
	}
	
	/**
	 * Create wrapper for existing biome using its {@link ResourceLocation} identifier.
	 * @param biomeID {@link ResourceLocation} biome ID.
	 */
	public BCLBiome(ResourceLocation biomeID) {
		this(biomeID, BuiltinRegistries.BIOME.get(biomeID), null);
	}
	
	/**
	 * Create wrapper for existing biome using biome instance from {@link BuiltinRegistries}.
	 * @param biome {@link Biome} to wrap.
	 */
	public BCLBiome(Biome biome) {
		this(biome, null);
	}
	
	/**
	 * Create wrapper for existing biome using biome instance from {@link BuiltinRegistries}.
	 * @param biome {@link Biome} to wrap.
	 * @param settings The Settings for this Biome or {@code null} if you want to apply default settings
	 */
	public BCLBiome(Biome biome, VanillaBiomeSettings settings) {
		this(BiomeAPI.getBiomeID(biome), biome, settings);
	}
	
	public BCLBiome(ResourceLocation biomeID, Biome biome) {
		this(biomeID, biome, null);
	}
	
	/**
	 * Create a new Biome
	 * @param biomeID {@link ResourceLocation} biome ID.
	 * @param biome  {@link Biome} to wrap.
	 * @param defaults The Settings for this Biome or null if you want to apply the defaults
	 */
	public BCLBiome(ResourceLocation biomeID, Biome biome, BCLBiomeSettings defaults) {
		this.subbiomes.add(this, 1.0F);
		this.biomeID = biomeID;
		this.biome = biome;
		
		if (defaults !=null){
			defaults.applyWithDefaults(this);
		}
	}
	
	/**
	 * Get current biome edge.
	 * @return {@link BCLBiome} edge.
	 */
	@Nullable
	public BCLBiome getEdge() {
		return edge;
	}
	
	/**
	 * Set biome edge for this biome instance.
	 * @param edge {@link BCLBiome} as the edge biome.
	 * @return same {@link BCLBiome}.
	 */
	BCLBiome setEdge(BCLBiome edge) {
		this.edge = edge;
		edge.biomeParent = this;
		return this;
	}
	
	/**
	 * Adds sub-biome into this biome instance. Biome chance will be interpreted as a sub-biome generation chance.
	 * Biome itself has chance 1.0 compared to all its sub-biomes.
	 * @param biome {@link Random} to be added.
	 * @return same {@link BCLBiome}.
	 */
	public BCLBiome addSubBiome(BCLBiome biome) {
		biome.biomeParent = this;
		subbiomes.add(biome, biome.getGenChance());
		return this;
	}
	
	/**
	 * Checks if specified biome is a sub-biome of this one.
	 * @param biome {@link Random}.
	 * @return true if this instance contains specified biome as a sub-biome.
	 */
	public boolean containsSubBiome(BCLBiome biome) {
		return subbiomes.contains(biome);
	}
	
	/**
	 * Getter for a random sub-biome from all existing sub-biomes. Will return biome itself if there are no sub-biomes.
	 * @param random {@link Random}.
	 * @return {@link BCLBiome}.
	 */
	public BCLBiome getSubBiome(WorldgenRandom random) {
		return subbiomes.get(random);
	}

	public void forEachSubBiome(BiConsumer<BCLBiome, Float> consumer){
		for (int i=0; i<subbiomes.size();i++)
			consumer.accept(subbiomes.get(i), subbiomes.getWeight(i));
	}
	
	/**
	 * Getter for parent {@link BCLBiome} or null if there are no parent biome.
	 * @return {@link BCLBiome} or null.
	 */
	@Nullable
	public BCLBiome getParentBiome() {
		return this.biomeParent;
	}
	
	/**
	 * Compares biome instances (directly) and their parents. Used in custom world generator.
	 * @param biome {@link BCLBiome}
	 * @return true if biome or its parent is same.
	 */
	public boolean isSame(BCLBiome biome) {
		return biome == this || (biome.biomeParent != null && biome.biomeParent == this);
	}
	
	/**
	 * Getter for biome identifier.
	 * @return {@link ResourceLocation}
	 */
	public ResourceLocation getID() {
		return biomeID;
	}
	

	public Holder<Biome> getBiomeHolder() {
		return BuiltinRegistries.BIOME.getOrCreateHolder(BiomeAPI.getBiomeKey(biome));
	}
	/**
	 * Getter for biome from buil-in registry. For datapack biomes will be same as actual biome.
	 * @return {@link Biome}.
	 */
	public Biome getBiome() {
		return biome;
	}
	
//	/**
//	 * Recursively update biomes to correct world biome registry instances, for internal usage only.
//	 * @param biomeRegistry {@link Registry} for {@link Biome}.
//	 */
//	public void updateActualBiomes(Registry<Biome> biomeRegistry) {
//		subbiomes.forEach((sub) -> {
//			if (sub != this) {
//				sub.updateActualBiomes(biomeRegistry);
//			}
//		});
//		if (edge != null && edge != this) {
//			edge.updateActualBiomes(biomeRegistry);
//		}
//
//		final ResourceKey<Biome> key = biomeRegistry.getResourceKey(biomeRegistry.get(biomeID)).orElseThrow();
//		Holder<Biome> aBiome = biomeRegistry.getOrCreateHolder(key);
//		if (aBiome != actualBiome && actualBiome != null) {
//			System.out.println("Changed actual Biome");
//		}
//		this.actualBiome = aBiome;
//		if (actualBiome == null) {
//			BCLib.LOGGER.error("Unable to find actual Biome for " + biomeID);
//		}
//	}

	/**
	 * For internal use from BiomeAPI only
	 */
	public void afterRegistration(){
		if (!this.structureTags.isEmpty()) {
			structureTags.forEach(tagKey ->
										  TagAPI.addBiomeTag(tagKey, biome)
								 );
		}
		
		if (this.surfaceInit != null) {
			surfaceInit.accept(getBiomeHolder());
		}
	}


	
	/**
	 * Getter for custom data. Will get custom data object or null if object doesn't exists.
	 * @param name {@link String} name of data object.
	 * @return object value or null.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	@Deprecated(forRemoval = true)
	public <T> T getCustomData(String name) {
		return (T) customData.get(name);
	}
	
	/**
	 * Getter for custom data. Will get custom data object or default value if object doesn't exists.
	 * @param name {@link String} name of data object.
	 * @param defaultValue object default value.
	 * @return object value or default value.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated(forRemoval = true)
	public <T> T getCustomData(String name, T defaultValue) {
		return (T) customData.getOrDefault(name, defaultValue);
	}
	
	/**
	 * Adds custom data object to this biome instance.
	 * @param name {@link String} name of data object.
	 * @param obj any data to add.
	 * @return same {@link BCLBiome}.
	 */
	public BCLBiome addCustomData(String name, Object obj) {
		customData.put(name, obj);
		return this;
	}
	
	/**
	 * Adds custom data object to this biome instance.
	 * @param data a {@link Map} with custom data.
	 * @return same {@link BCLBiome}.
	 */
	public BCLBiome addCustomData(Map<String, Object> data) {
		customData.putAll(data);
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		BCLBiome biome = (BCLBiome) obj;
		return biome == null ? false : biomeID.equals(biome.biomeID);
	}
	
	@Override
	public int hashCode() {
		return biomeID.hashCode();
	}
	
	@Override
	public String toString() {
		return biomeID.toString();
	}
	
	/**
	 * Adds structures to this biome. For internal use only.
	 * Used inside {@link ru.bclib.api.biomes.BCLBiomeBuilder}.
	 */
	public void attachStructures(List<TagKey<Biome>> structures) {
		this.structureTags.addAll(structures);
	}

	/**
	 * Adds structures to this biome. For internal use only.
	 * Used inside {@link ru.bclib.api.biomes.BCLBiomeBuilder}.
	 */
	public void addClimateParameters(List<Climate.ParameterPoint> params) {
		this.parameterPoints.addAll(params);
	}

	public void forEachClimateParameter(Consumer<Climate.ParameterPoint> consumer){
		this.parameterPoints.forEach(consumer);
	}
	
	/**
	 * Sets biome surface rule.
	 * @param surface {@link SurfaceRules.RuleSource} rule.
	 */
	public void setSurface(RuleSource surface) {
		this.surfaceInit = (b) -> {
			final ResourceKey key = BiomeAPI.getBiomeKey(b);
			if (key == null) {
				BCLib.LOGGER.warning("BCL Biome " + biomeID + " does not have registry key!");
			}
			else {
				BiomeAPI.addSurfaceRule(biomeID, SurfaceRules.ifTrue(SurfaceRules.isBiome(key), surface));
			}
		};
	}

	/**
	 * Returns the group used in the config Files for this biome
	 *
	 * Example: {@code Configs.BIOMES_CONFIG.getFloat(configGroup(), "generation_chance", 1.0);}
	 * @return The group name
	 */
	public String configGroup() {
		return biomeID.getNamespace() + "." + biomeID.getPath();
	}
	
	private boolean didLoadConfig = false;
}
