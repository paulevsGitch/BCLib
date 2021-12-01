package ru.bclib.world.biomes;

import com.google.common.collect.Maps;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import ru.bclib.util.WeightedList;

import java.util.Map;
import java.util.Random;

public class BCLBiome {
	private final WeightedList<BCLBiome> subbiomes = new WeightedList<>();
	private final Map<String, Object> customData = Maps.newHashMap();
	private final ResourceLocation biomeID;
	private final Biome biome;
	
	private BCLBiome biomeParent;
	private Biome actualBiome;
	private BCLBiome edge;
	
	private float terrainHeight = 0.1F;
	private float fogDensity = 1.0F;
	private float genChance = 1.0F;
	private float edgeSize = 0.0F;
	
	/**
	 * Create wrapper for existing biome using its {@link ResourceLocation} identifier.
	 * @param biomeID {@link ResourceLocation} biome ID.
	 */
	public BCLBiome(ResourceLocation biomeID) {
		this(biomeID, BuiltinRegistries.BIOME.get(biomeID));
	}
	
	/**
	 * Create wrapper for existing biome using biome instance from {@link BuiltinRegistries}.
	 * @param biome {@link Biome} to wrap.
	 */
	public BCLBiome(Biome biome) {
		this(BuiltinRegistries.BIOME.getKey(biome), biome);
	}
	
	public BCLBiome(ResourceLocation biomeID, Biome biome) {
		this.biomeID = biomeID;
		this.biome = biome;
	}
	
	/**
	 * Get current bime edge.
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
	public BCLBiome setEdge(BCLBiome edge) {
		this.edge = edge;
		edge.biomeParent = this;
		return this;
	}
	
	/**
	 * Getter for biome edge size.
	 * @return edge size.
	 */
	public float getEdgeSize() {
		return edgeSize;
	}
	
	/**
	 * Set edges size for this biome. Size is in relative units to work fine with biome scale.
	 * @param size as a float value.
	 * @return same {@link BCLBiome}.
	 */
	public BCLBiome setEdgeSize(float size) {
		edgeSize = size;
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
	 * Getter for fog density, used in custom for renderer.
	 * @return fog density as a float.
	 */
	public float getFogDensity() {
		return fogDensity;
	}
	
	/**
	 * Sets fog density for this biome.
	 * @param fogDensity
	 * @return same {@link BCLBiome}.
	 */
	public BCLBiome setFogDensity(float fogDensity) {
		this.fogDensity = fogDensity;
		return this;
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
	 * Getter for biome generation chance, used in {@link ru.bclib.world.generator.BiomePicker} and in custom generators.
	 * @return biome generation chance as float.
	 */
	public float getGenChance() {
		return this.genChance;
	}
	
	/**
	 * Set gen chance for this biome, default value is 1.0.
	 * @param genChance chance of this biome to be generated.
	 * @return same {@link BCLBiome}.
	 */
	public BCLBiome setGenChance(float genChance) {
		this.genChance = genChance;
		return this;
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
	
	/**
	 * Getter for terrain height, can be used in custom terrain generator.
	 * @return terrain height.
	 */
	public float getTerrainHeight() {
		return terrainHeight;
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
}
