package ru.bclib.world.biomes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.Nullable;
import ru.bclib.BCLib;
import ru.bclib.api.biomes.BiomeAPI;
import ru.bclib.util.WeightedList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BCLBiome extends BCLBiomeSettings {
	private final List<ConfiguredStructureFeature> structures = Lists.newArrayList();
	private final WeightedList<BCLBiome> subbiomes = new WeightedList<>();
	private final Map<String, Object> customData = Maps.newHashMap();
	private final ResourceLocation biomeID;
	private final Biome biome;
	
	private Consumer<Biome> surfaceInit;
	private BCLBiome biomeParent;
	private Biome actualBiome;
	
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
		this(BuiltinRegistries.BIOME.getKey(biome), biome, settings);
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
	public BCLBiome getSubBiome(Random random) {
		return subbiomes.get(random);
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
	
	
	/**
	 * Getter for biome from buil-in registry. For datapack biomes will be same as actual biome.
	 * @return {@link Biome}.
	 */
	public Biome getBiome() {
		return biome;
	}
	
	/**
	 * Getter for actual biome (biome from current world registry with same {@link ResourceLocation} id).
	 * @return {@link Biome}.
	 */
	public Biome getActualBiome() {
		return this.actualBiome;
	}
	
	
	
	/**
	 * Recursively update biomes to correct world biome registry instances, for internal usage only.
	 * @param biomeRegistry {@link Registry} for {@link Biome}.
	 */
	public void updateActualBiomes(Registry<Biome> biomeRegistry) {
		subbiomes.forEach((sub) -> {
			if (sub != this) {
				sub.updateActualBiomes(biomeRegistry);
			}
		});
		if (edge != null && edge != this) {
			edge.updateActualBiomes(biomeRegistry);
		}
		this.actualBiome = biomeRegistry.get(biomeID);
		if (actualBiome==null) {
			BCLib.LOGGER.error("Unable to find actual Biome for " + biomeID);
		}
		
		if (!this.structures.isEmpty()) {
			structures.forEach(s -> BiomeAPI.addBiomeStructure(BiomeAPI.getBiomeKey(actualBiome), s));
		}
		
		if (this.surfaceInit != null) {
			surfaceInit.accept(actualBiome);
		}
	}
	
	/**
	 * Getter for custom data. Will get custom data object or null if object doesn't exists.
	 * @param name {@link String} name of data object.
	 * @return object value or null.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
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
	public void attachStructures(List<ConfiguredStructureFeature> structures) {
		this.structures.addAll(structures);
	}
	
	/**
	 * Sets biome surface rule.
	 * @param surface {@link SurfaceRules.RuleSource} rule.
	 */
	public void setSurface(RuleSource surface) {
		this.surfaceInit = (actualBiome) -> {
			ResourceKey key = BiomeAPI.getBiomeKey(actualBiome);
			if (key == null) {
				BCLib.LOGGER.warning("BCL Biome " + biomeID + " don't have registry key!");
			}
			else {
				BiomeAPI.addSurfaceRule(biomeID, SurfaceRules.ifTrue(SurfaceRules.isBiome(key), surface));
			}
		};
	}
	
	private Map<Decoration, List<Supplier<PlacedFeature>>> features = new HashMap<>(0);
	
	/**
	 * Sets the biome features.
	 * @param features the feature list.
	 */
	public void setFeatures(Map<Decoration, List<Supplier<PlacedFeature>>> features) {
		this.features = features;
		BiomeAPI.addStepFeaturesToBiome(getBiome(), features);
	}
	
	/**
	 * Returns the built-in set of Features for this biome (as they were set with {@link #setFeatures(Map)})
	 * @return List of all features
	 */
	public Map<Decoration, List<Supplier<PlacedFeature>>> getFeatures(){
		return features;
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
	
//	/**
//	 * Set gen chance for this biome, default value is 1.0.
//	 * @param genChance chance of this biome to be generated.
//	 * @return same {@link BCLBiome}.
//	 */
//	public BCLBiome setGenChance(float genChance) {
//		super.setGenChance(genChance);
//		return this;
//	}
//
//	/**
//	 * Setter for terrain height, can be used in custom terrain generator.
//	 * @param terrainHeight a relative float terrain height value.
//	 * @return same {@link BCLBiome}.
//	 */
//	public BCLBiome setTerrainHeight(float terrainHeight) {
//		super.setTerrainHeight(genChance);
//		return this;
//	}
//
//	/**
//	 * Set biome vertical distribution (for tall Nether only).
//	 * @return same {@link BCLBiome}.
//	 */
//	public BCLBiome setVertical() {
//		return setVertical(true);
//	}
//
//	/**
//	 * Set biome vertical distribution (for tall Nether only).
//	 * @param vertical {@code boolean} value.
//	 * @return same {@link BCLBiome}.
//	 */
//	public BCLBiome setVertical(boolean vertical) {
//		super.setVertical(vertical);
//		return this;
//	}
//
//	/**
//	 * Sets fog density for this biome.
//	 * @param fogDensity
//	 * @return same {@link BCLBiome}.
//	 */
//	public BCLBiome setFogDensity(float fogDensity) {
//		super.setFogDensity(fogDensity);
//		return this;
//	}
//
//	/**
//	 * Set edges size for this biome. Size is in blocks.
//	 * @param size as a float value.
//	 * @return same {@link BCLBiome}.
//	 */
//	public BCLBiome setEdgeSize(int size) {
//		edgeSize = size;
//		return this;
//	}
}
