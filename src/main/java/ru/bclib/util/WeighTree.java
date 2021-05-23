package ru.bclib.util;

import java.util.Random;

public class WeighTree<T> {
	private final float maxWeight;
	private final Node root;
	
	public WeighTree(WeightedList<T> list) {
		maxWeight = list.getMaxWeight();
		root = getNode(list);
	}
	
	/**
	 * Get eandom value from tree.
	 * @param random - {@link Random}.
	 * @return {@link T} value.
	 */
	public T get(Random random) {
		return root.get(random.nextFloat() * maxWeight);
	}
	
	private Node getNode(WeightedList<T> biomes) {
		int size = biomes.size();
		if (size == 1) {
			return new Leaf(biomes.get(0));
		}
		else if (size == 2) {
			T first = biomes.get(0);
			return new Branch(biomes.getWeight(0), new Leaf(first), new Leaf(biomes.get(1)));
		}
		else {
			int index = size >> 1;
			float separator = biomes.getWeight(index);
			Node a = getNode(biomes.subList(0, index + 1));
			Node b = getNode(biomes.subList(index, size));
			return new Branch(separator, a, b);
		}
	}
	
	private abstract class Node {
		abstract T get(float value);
	}
	
	private class Branch extends Node {
		final float separator;
		final Node min;
		final Node max;
		
		public Branch(float separator, Node min, Node max) {
			this.separator = separator;
			this.min = min;
			this.max = max;
		}

		@Override
		T get(float value) {
			return value < separator ? min.get(value) : max.get(value);
		}
	}
	
	private class Leaf extends Node {
		final T biome;
		
		Leaf(T biome) {
			this.biome = biome;
		}

		@Override
		T get(float value) {
			return biome;
		}
	}
}
