package ru.bclib.world.generator.map.hex;

import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.generator.BiomePicker;

import java.util.Random;

public class HexBiomeChunk {
	private static final short SIDE = 32;
	private static final short SIZE = SIDE * SIDE;
	private static final byte SIDE_MASK = SIDE - 1;
	private static final byte SIDE_OFFSET = (byte) Math.round(Math.log(SIDE) / Math.log(2));
	private static final short[][] NEIGHBOURS;
	public static final short SCALE = SIDE / 4;
	
	private final BCLBiome[] biomes;
	
	public HexBiomeChunk(Random random, BiomePicker picker) {
		BCLBiome[][] buffers = new BCLBiome[2][SIZE];
		
		byte scale = SIDE / 4;
		for (byte x = 0; x < 4; x++) {
			for (byte z = 0; z < 4; z++) {
				byte px = (byte) (x * scale + random.nextInt(scale));
				byte pz = (byte) (z * scale + random.nextInt(scale));
				circle(buffers[0], getIndex(px, pz), picker.getBiome(random), null);
			}
		}
		
		short maxSide = SIZE - SIDE;
		boolean hasEmptyCells = true;
		byte bufferIndex = 0;
		while (hasEmptyCells) {
			BCLBiome[] inBuffer = buffers[bufferIndex];
			bufferIndex = (byte) ((bufferIndex + 1) & 1);
			BCLBiome[] outBuffer = buffers[bufferIndex];
			hasEmptyCells = false;
			
			for (short index = SIDE; index < maxSide; index++) {
				byte z = (byte) (index & SIDE_MASK);
				if (z == 0 || z == SIDE_MASK) {
					continue;
				}
				if (inBuffer[index] != null) {
					outBuffer[index] = inBuffer[index];
					short[] neighbours = getNeighbours(index & SIDE_MASK);
					short indexSide = (short) (index + neighbours[random.nextInt(6)]);
					if (indexSide >= 0 && indexSide < SIZE && outBuffer[indexSide] == null) {
						outBuffer[indexSide] = inBuffer[index];
					}
				}
				else {
					hasEmptyCells = true;
				}
			}
		}
		
		BCLBiome[] outBuffer = buffers[bufferIndex];
		byte preN = (byte) (SIDE_MASK - 2);
		for (byte index = 0; index < SIDE; index++) {
			outBuffer[getIndex(index, (byte) 0)] = outBuffer[getIndex(index, (byte) 2)];
			outBuffer[getIndex((byte) 0, index)] = outBuffer[getIndex((byte) 2, index)];
			outBuffer[getIndex(index, SIDE_MASK)] = outBuffer[getIndex(index, preN)];
			outBuffer[getIndex(SIDE_MASK, index)] = outBuffer[getIndex(preN, index)];
		}
		
		for (short index = 0; index < SIZE; index++) {
			if (outBuffer[index] != null && random.nextInt(4) == 0) {
				circle(outBuffer, index, outBuffer[index].getSubBiome(random), outBuffer[index]);
			}
		}
		
		this.biomes = outBuffer;
	}
	
	private void circle(BCLBiome[] buffer, short center, BCLBiome biome, BCLBiome mask) {
		if (buffer[center] == mask) {
			buffer[center] = biome;
		}
		short[] neighbours = getNeighbours(center & SIDE_MASK);
		for (short i: neighbours) {
			short index = (short) (center + i);
			if (index >= 0 && index < SIZE && buffer[index] == mask) {
				buffer[index] = biome;
			}
		}
	}
	
	private static byte wrap(int value) {
		return (byte) (value & SIDE_MASK);
	}
	
	private short getIndex(byte x, byte z) {
		return (short) ((short) x << SIDE_OFFSET | z);
	}
	
	public BCLBiome getBiome(int x, int z) {
		return biomes[getIndex(wrap(x), wrap(z))];
	}
	
	public static int scaleCoordinate(int value) {
		return value >> SIDE_OFFSET;
	}
	
	public static boolean isBorder(int value) {
		return wrap(value) == SIDE_MASK;
	}
	
	private short[] getNeighbours(int z) {
		return NEIGHBOURS[z & 1];
	}
	
	static {
		NEIGHBOURS = new short[2][6];
		
		NEIGHBOURS[0][0] = 1;
		NEIGHBOURS[0][1] = -1;
		NEIGHBOURS[0][2] = SIDE;
		NEIGHBOURS[0][3] = -SIDE;
		NEIGHBOURS[0][4] = SIDE + 1;
		NEIGHBOURS[0][5] = SIDE - 1;
		
		NEIGHBOURS[1][0] = 1;
		NEIGHBOURS[1][1] = -1;
		NEIGHBOURS[1][2] = SIDE;
		NEIGHBOURS[1][3] = -SIDE;
		NEIGHBOURS[1][4] = -SIDE + 1;
		NEIGHBOURS[1][5] = -SIDE - 1;
	}
}
