package ru.bclib.world.generator.map.square;

import com.google.common.collect.Maps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import ru.bclib.noise.OpenSimplexNoise;
import ru.bclib.util.MHelper;
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.generator.BiomePicker;

import java.util.Map;

@Deprecated
public class SquareBiomeMap {
	private final WorldgenRandom RANDOM;
	
	private final Map<ChunkPos, SquareBiomeChunk> maps = Maps.newHashMap();
	private final int size;
	private final int sizeXZ;
	private final int depth;
	private final OpenSimplexNoise noiseX;
	private final OpenSimplexNoise noiseZ;
	private final BiomePicker picker;
	private final long seed;
	
	public SquareBiomeMap(long seed, int size, BiomePicker picker) {
		maps.clear();
		RANDOM = new WorldgenRandom(new LegacyRandomSource(seed));
		noiseX = new OpenSimplexNoise(RANDOM.nextLong());
		noiseZ = new OpenSimplexNoise(RANDOM.nextLong());
		this.sizeXZ = size;
		depth = (int) Math.ceil(Math.log(size) / Math.log(2)) - 2;
		this.size = 1 << depth;
		this.picker = picker;
		this.seed = seed;
	}
	
	public long getSeed() {
		return seed;
	}
	
	public void clearCache() {
		if (maps.size() > 32) {
			maps.clear();
		}
	}
	
	private BCLBiome getRawBiome(int bx, int bz) {
		double x = (double) bx * size / sizeXZ;
		double z = (double) bz * size / sizeXZ;
		double nx = x;
		double nz = z;
		
		double px = bx * 0.2;
		double pz = bz * 0.2;
		
		for (int i = 0; i < depth; i++) {
			nx = (x + noiseX.eval(px, pz)) / 2F;
			nz = (z + noiseZ.eval(px, pz)) / 2F;
			
			x = nx;
			z = nz;
			
			px = px / 2 + i;
			pz = pz / 2 + i;
		}
		
		bx = MHelper.floor(x);
		bz = MHelper.floor(z);
		if ((bx & SquareBiomeChunk.MASK_WIDTH) == SquareBiomeChunk.MASK_WIDTH) {
			x += (bz / 2) & 1;
		}
		if ((bz & SquareBiomeChunk.MASK_WIDTH) == SquareBiomeChunk.MASK_WIDTH) {
			z += (bx / 2) & 1;
		}
		
		ChunkPos cpos = new ChunkPos(MHelper.floor(x / SquareBiomeChunk.WIDTH), MHelper.floor(z / SquareBiomeChunk.WIDTH));
		SquareBiomeChunk chunk = maps.get(cpos);
		if (chunk == null) {
			RANDOM.setLargeFeatureWithSalt(0, cpos.x, cpos.z, 0);
			chunk = new SquareBiomeChunk(this, RANDOM, picker);
			maps.put(cpos, chunk);
		}
		
		return chunk.getBiome(MHelper.floor(x), MHelper.floor(z));
	}
	
	public BCLBiome getBiome(int x, int z) {
		BCLBiome biome = getRawBiome(x, z);
		
		if (biome.getEdge() != null || (biome.getParentBiome() != null && biome.getParentBiome().getEdge() != null)) {
			BCLBiome search = biome;
			if (biome.getParentBiome() != null) {
				search = biome.getParentBiome();
			}
			int d = (int) Math.ceil(search.getEdgeSize() / 4F) << 2;
			
			boolean edge = !search.isSame(getRawBiome(x + d, z));
			edge = edge || !search.isSame(getRawBiome(x - d, z));
			edge = edge || !search.isSame(getRawBiome(x, z + d));
			edge = edge || !search.isSame(getRawBiome(x, z - d));
			edge = edge || !search.isSame(getRawBiome(x - 1, z - 1));
			edge = edge || !search.isSame(getRawBiome(x - 1, z + 1));
			edge = edge || !search.isSame(getRawBiome(x + 1, z - 1));
			edge = edge || !search.isSame(getRawBiome(x + 1, z + 1));
			
			if (edge) {
				biome = search.getEdge();
			}
		}
		
		return biome;
	}
}
