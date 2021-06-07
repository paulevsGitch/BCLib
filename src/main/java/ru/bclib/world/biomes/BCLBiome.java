package ru.bclib.world.biomes;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import ru.bclib.util.JsonFactory;
import ru.bclib.util.StructureHelper;
import ru.bclib.util.WeightedList;
import ru.bclib.world.features.BCLFeature;
import ru.bclib.world.features.ListFeature;
import ru.bclib.world.features.ListFeature.StructureInfo;
import ru.bclib.world.features.NBTStructureFeature.TerrainMerge;

public class BCLBiome {
	protected WeightedList<BCLBiome> subbiomes = new WeightedList<BCLBiome>();

	protected final Biome biome;
	protected final ResourceLocation mcID;
	protected BCLBiome edge;
	protected int edgeSize;

	protected BCLBiome biomeParent;
	protected float maxSubBiomeChance = 1;
	protected final float genChance;

	private final Map<String, Object> customData;
	private final float fogDensity;
	private BCLFeature structuresFeature;
	private Biome actualBiome;

	public BCLBiome(BCLBiomeDef definition) {
		this.mcID = definition.getID();
		this.readStructureList();
		if (structuresFeature != null) {
			definition.addFeature(structuresFeature);
		}
		this.biome = definition.build();
		this.genChance = definition.getGenChance();
		this.fogDensity = definition.getFodDensity();
		this.customData = definition.getCustomData();
	}

	public BCLBiome(ResourceLocation id, Biome biome, float fogDensity, float genChance) {
		this.mcID = id;
		this.biome = biome;
		this.genChance = genChance;
		this.fogDensity = fogDensity;
		this.readStructureList();
		this.customData = Maps.newHashMap();
	}

	public BCLBiome getEdge() {
		return edge == null ? this : edge;
	}

	public void setEdge(BCLBiome edge) {
		this.edge = edge;
		edge.biomeParent = this;
	}

	public int getEdgeSize() {
		return edgeSize;
	}

	public void setEdgeSize(int size) {
		edgeSize = size;
	}

	public void addSubBiome(BCLBiome biome) {
		biome.biomeParent = this;
		subbiomes.add(biome, biome.getGenChance());
	}
	
	public boolean containsSubBiome(BCLBiome biome) {
		return subbiomes.contains(biome);
	}

	public BCLBiome getSubBiome(Random random) {
		BCLBiome biome = subbiomes.get(random);
		return biome == null ? this : biome;
	}

	public BCLBiome getParentBiome() {
		return this.biomeParent;
	}

	public boolean hasEdge() {
		return edge != null;
	}

	public boolean hasParentBiome() {
		return biomeParent != null;
	}

	public boolean isSame(BCLBiome biome) {
		return biome == this || (biome.hasParentBiome() && biome.getParentBiome() == this);
	}

	public Biome getBiome() {
		return biome;
	}

	@Override
	public String toString() {
		return mcID.toString();
	}

	public ResourceLocation getID() {
		return mcID;
	}

	public float getFogDensity() {
		return fogDensity;
	}
	
	protected void readStructureList() {
		String ns = mcID.getNamespace();
		String nm = mcID.getPath();

		String path = "/data/" + ns + "/structures/biome/" + nm + "/";
		InputStream inputstream = StructureHelper.class.getResourceAsStream(path + "structures.json");
		if (inputstream != null) {
			JsonObject obj = JsonFactory.getJsonObject(inputstream);
			JsonArray enties = obj.getAsJsonArray("structures");
			if (enties != null) {
				List<StructureInfo> list = Lists.newArrayList();
				enties.forEach((entry) -> {
					JsonObject e = entry.getAsJsonObject();
					String structure = path + e.get("nbt").getAsString() + ".nbt";
					TerrainMerge terrainMerge = TerrainMerge.getFromString(e.get("terrainMerge").getAsString());
					int offsetY = e.get("offsetY").getAsInt();
					list.add(new StructureInfo(structure, offsetY, terrainMerge));
				});
				if (!list.isEmpty()) {
					structuresFeature = BCLFeature.makeChansedFeature(new ResourceLocation(ns, nm + "_structures"), new ListFeature(list), 10);
				}
			}
		}
	}
	
	public BCLFeature getStructuresFeature() {
		return structuresFeature;
	}
	
	public Biome getActualBiome() {
		return this.actualBiome;
	}

	public float getGenChance() {
		return this.genChance;
	}
	
	public void updateActualBiomes(Registry<Biome> biomeRegistry) {
		subbiomes.forEach((sub) -> {
			if (sub != this) {
				sub.updateActualBiomes(biomeRegistry);
			}
		});
		if (edge != null && edge != this) {
			edge.updateActualBiomes(biomeRegistry);
		}
		this.actualBiome = biomeRegistry.get(mcID);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		BCLBiome biome = (BCLBiome) obj;
		return biome == null ? false : biome.mcID.equals(mcID);
	}
	
	@Override
	public int hashCode() {
		return mcID.hashCode();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getCustomData(String name, T defaultValue) {
		return (T) customData.getOrDefault(name, defaultValue);
	}
	
	public void addCustomData(String name, Object obj) {
		customData.put(name, obj);
	}
}
