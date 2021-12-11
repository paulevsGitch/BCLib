package ru.bclib.world.generator.map.square;

import ru.bclib.interfaces.BiomeChunk;
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.generator.BiomePicker;

import java.util.Random;

public class SquareBiomeChunk implements BiomeChunk {
	private static final int BIT_OFFSET = 4;
	protected static final int WIDTH = 1 << BIT_OFFSET;
	private static final int SM_WIDTH = WIDTH >> 1;
	private static final int SM_BIT_OFFSET = BIT_OFFSET >> 1;
	private static final int MASK_OFFSET = SM_WIDTH - 1;
	protected static final int MASK_WIDTH = WIDTH - 1;
	
	private static final int SM_CAPACITY = SM_WIDTH * SM_WIDTH;
	private static final int CAPACITY = WIDTH * WIDTH;
	
	private final BCLBiome[] biomes;
	
	public SquareBiomeChunk(Random random, BiomePicker picker) {
		BCLBiome[] PreBio = new BCLBiome[SM_CAPACITY];
		biomes = new BCLBiome[CAPACITY];
		
		for (int x = 0; x < SM_WIDTH; x++) {
			int offset = x << SM_BIT_OFFSET;
			for (int z = 0; z < SM_WIDTH; z++) {
				PreBio[offset | z] = picker.getBiome(random);
			}
		}
		
		for (int x = 0; x < WIDTH; x++) {
			int offset = x << BIT_OFFSET;
			for (int z = 0; z < WIDTH; z++) {
				biomes[offset | z] = PreBio[getSmIndex(offsetXZ(x, random), offsetXZ(z, random))].getSubBiome(random);
			}
		}
	}
	
	@Override
	public BCLBiome getBiome(int x, int z) {
		return biomes[getIndex(x & MASK_WIDTH, z & MASK_WIDTH)];
	}
	
	@Override
	public void setBiome(int x, int z, BCLBiome biome) {
		biomes[getIndex(x & MASK_WIDTH, z & MASK_WIDTH)] = biome;
	}
	
	@Override
	public int getSide() {
		return WIDTH;
	}
	
	private int offsetXZ(int x, Random random) {
		return ((x + random.nextInt(2)) >> 1) & MASK_OFFSET;
	}
	
	private int getIndex(int x, int z) {
		return x << BIT_OFFSET | z;
	}
	
	private int getSmIndex(int x, int z) {
		return x << SM_BIT_OFFSET | z;
	}
}
