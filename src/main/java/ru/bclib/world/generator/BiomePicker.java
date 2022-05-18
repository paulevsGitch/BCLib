package ru.bclib.world.generator;

import com.google.common.collect.Lists;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import ru.bclib.util.WeighTree;
import ru.bclib.util.WeightedList;
import ru.bclib.world.biomes.BCLBiome;

import java.util.*;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class BiomePicker {
	public final Map<BCLBiome, ActualBiome> all = new HashMap<>();
	public class ActualBiome {
		public final BCLBiome bclBiome;
		public final Holder<Biome> biome;
		public final ResourceKey<Biome> key;

		private final WeightedList<ActualBiome> subbiomes = new WeightedList<>();
		private final ActualBiome edge;
		private final ActualBiome parent;

		private ActualBiome(BCLBiome bclBiome){
			all.put(bclBiome, this);
			this.bclBiome = bclBiome;

			this.key = biomeRegistry.getResourceKey(biomeRegistry.get(bclBiome.getID())).orElseThrow();
			this.biome = biomeRegistry.getOrCreateHolder(key);

			bclBiome.forEachSubBiome((b, w)->{
				subbiomes.add(create(b), w);
			});

			edge = bclBiome.getEdge()!=null?create(bclBiome.getEdge()):null;
			parent = bclBiome.getParentBiome()!=null?create(bclBiome.getParentBiome()):null;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ActualBiome entry = (ActualBiome) o;
			return bclBiome.equals(entry.bclBiome);
		}

		@Override
		public int hashCode() {
			return Objects.hash(bclBiome);
		}

		public ActualBiome getSubBiome(WorldgenRandom random) {
			return subbiomes.get(random);
		}

		public ActualBiome getEdge(){
			return edge;
		}

		public ActualBiome getParentBiome(){
			return parent;
		}

		public boolean isSame(ActualBiome e){
			return bclBiome.isSame(e.bclBiome);
		}
	}

	private ActualBiome create(BCLBiome bclBiome){
		ActualBiome e = all.get(bclBiome);
		if (e!=null) return e;
		return new ActualBiome(bclBiome);
	}

	private final List<ActualBiome> biomes = Lists.newArrayList();
	public final Registry<Biome> biomeRegistry;
	private WeighTree<ActualBiome> tree;

	public BiomePicker(Registry<Biome> biomeRegistry){
		this.biomeRegistry = biomeRegistry;
	}

	public void addBiome(BCLBiome biome) {
		biomes.add(create(biome));
	}
	
	public ActualBiome getBiome(WorldgenRandom random) {
		return biomes.isEmpty() ? null : tree.get(random);
	}

	public void rebuild() {
		if (biomes.isEmpty()) {
			return;
		}
		WeightedList<ActualBiome> list = new WeightedList<>();
		biomes.forEach(biome -> {
			list.add(biome, biome.bclBiome.getGenChance());
		});
		tree = new WeighTree<>(list);
	}
}
