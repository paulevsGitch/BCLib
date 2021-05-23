package ru.bclib.world.biomes;

import java.io.InputStream;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import ru.bclib.config.IdConfig;
import ru.bclib.util.JsonFactory;
import ru.bclib.util.StructureHelper;
import ru.bclib.world.features.BCLFeature;
import ru.bclib.world.features.ListFeature;
import ru.bclib.world.features.ListFeature.StructureInfo;
import ru.bclib.world.features.NBTStructureFeature.TerrainMerge;

public class BCLBiome {
	protected List<BCLBiome> subbiomes = Lists.newArrayList();

	protected final Biome biome;
	protected final ResourceLocation mcID;
	protected BCLBiome edge;
	protected int edgeSize;

	protected BCLBiome biomeParent;
	protected float maxSubBiomeChance = 1;
	protected final float genChanceUnmutable;
	protected float genChance = 1;

	private final float fogDensity;
	private BCLFeature structuresFeature;
	private Biome actualBiome;

	public BCLBiome(BiomeDefinition definition, IdConfig config) {
		this.mcID = definition.getID();
		this.readStructureList();
		if (structuresFeature != null) {
			definition.addFeature(structuresFeature);
		}
		this.biome = definition.build();
		this.fogDensity = config.getFloat(mcID, "fog_density", definition.getFodDensity());
		this.genChanceUnmutable = config.getFloat(mcID, "generation_chance", definition.getGenChance());
		this.edgeSize = config.getInt(mcID, "edge_size", 32);
	}

	public BCLBiome(ResourceLocation id, Biome biome, float fogDensity, float genChance, boolean hasCaves, IdConfig config) {
		this.mcID = id;
		this.readStructureList();
		this.biome = biome;
		this.fogDensity = config.getFloat(mcID, "fog_density", fogDensity);
		this.genChanceUnmutable = config.getFloat(mcID, "generation_chance", genChance);
		this.edgeSize = config.getInt(mcID, "edge_size", 32);
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
		maxSubBiomeChance += biome.mutateGenChance(maxSubBiomeChance);
		biome.biomeParent = this;
		subbiomes.add(biome);
	}
	
	public boolean containsSubBiome(BCLBiome biome) {
		return subbiomes.contains(biome);
	}

	public BCLBiome getSubBiome(Random random) {
		float chance = random.nextFloat() * maxSubBiomeChance;
		for (BCLBiome biome : subbiomes)
			if (biome.canGenerate(chance))
				return biome;
		return this;
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

	public boolean canGenerate(float chance) {
		return chance <= this.genChance;
	}

	public float mutateGenChance(float chance) {
		genChance = genChanceUnmutable;
		genChance += chance;
		return genChance;
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
	
	public float getGenChanceImmutable() {
		return this.genChanceUnmutable;
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
}
