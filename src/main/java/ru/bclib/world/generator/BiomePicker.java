package ru.bclib.world.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.util.WeighTree;
import ru.bclib.util.WeightedList;
import ru.bclib.world.biomes.BCLBiome;

import java.util.*;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class BiomePicker {
	public final Map<BCLBiome, Entry> all = new HashMap<>();
	public class Entry {
		public final BCLBiome bclBiome;
		public final Holder<Biome> actual;
		public final ResourceKey<Biome> key;

		private final WeightedList<Entry> subbiomes = new WeightedList<>();
		private final Entry edge;
		private final Entry parent;

		private Entry(BCLBiome bclBiome){
			all.put(bclBiome, this);
			this.bclBiome = bclBiome;

			this.key = biomeRegistry.getResourceKey(biomeRegistry.get(bclBiome.getID())).orElseThrow();
			this.actual = biomeRegistry.getOrCreateHolder(key);

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
			Entry entry = (Entry) o;
			return bclBiome.equals(entry.bclBiome);
		}

		@Override
		public int hashCode() {
			return Objects.hash(bclBiome);
		}

		public Entry getSubBiome(WorldgenRandom random) {
			return subbiomes.get(random);
		}

		public Entry getEdge(){
			return edge;
		}

		public Entry getParentBiome(){
			return parent;
		}

		public boolean isSame(Entry e){
			return bclBiome.isSame(e.bclBiome);
		}
	}

	private Entry create(BCLBiome bclBiome){
		Entry e = all.get(bclBiome);
		if (e!=null) return e;
		return new Entry(bclBiome);
	}

	private final List<Entry> biomes = Lists.newArrayList();
	public final Registry<Biome> biomeRegistry;
	private WeighTree<Entry> tree;

	public BiomePicker(Registry<Biome> biomeRegistry){
		this.biomeRegistry = biomeRegistry;
	}

	public void addBiome(BCLBiome biome) {
		biomes.add(create(biome));
	}
	
	public Entry getBiome(WorldgenRandom random) {
		return biomes.isEmpty() ? null : tree.get(random);
	}

	public void rebuild() {
		if (biomes.isEmpty()) {
			return;
		}
		WeightedList<Entry> list = new WeightedList<>();
		biomes.forEach(biome -> {
			list.add(biome, biome.bclBiome.getGenChance());
		});
		tree = new WeighTree<>(list);
	}
}
