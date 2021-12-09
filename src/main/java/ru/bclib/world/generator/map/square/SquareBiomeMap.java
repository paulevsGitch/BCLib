package ru.bclib.world.generator.map.square;

import com.google.common.collect.Maps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import ru.bclib.interfaces.BiomeMap;
import ru.bclib.noise.OpenSimplexNoise;
import ru.bclib.util.MHelper;
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.generator.BiomePicker;

import java.util.Map;

public class SquareBiomeMap implements BiomeMap {
	private final Map<ChunkPos, SquareBiomeChunk> maps = Maps.newHashMap();
	private final OpenSimplexNoise noiseX;
	private final OpenSimplexNoise noiseZ;
	private final WorldgenRandom random;
	private final BiomePicker picker;
	
	private final int sizeXZ;
	private final int depth;
	private final int size;
	
	public SquareBiomeMap(long seed, int size, BiomePicker picker) {
		maps.clear();
		random = new WorldgenRandom(new LegacyRandomSource(seed));
		noiseX = new OpenSimplexNoise(random.nextLong());
		noiseZ = new OpenSimplexNoise(random.nextLong());
		this.sizeXZ = size;
		depth = (int) Math.ceil(Math.log(size) / Math.log(2)) - 2;
		this.size = 1 << depth;
		this.picker = picker;
	}
	
	@Override
	public void clearCache() {
		if (maps.size() > 32) {
			maps.clear();
		}
	}
	
	@Override
	public BCLBiome getBiome(double x, double y, double z) {
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
	
	private BCLBiome getRawBiome(double bx, double bz) {
		double x = bx * size / sizeXZ;
		double z = bz * size / sizeXZ;
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
		
		int ix = MHelper.floor(x);
		int iz = MHelper.floor(z);
		if ((ix & SquareBiomeChunk.MASK_WIDTH) == SquareBiomeChunk.MASK_WIDTH) {
			x += (iz / 2) & 1;
		}
		if ((iz & SquareBiomeChunk.MASK_WIDTH) == SquareBiomeChunk.MASK_WIDTH) {
			z += (ix / 2) & 1;
		}
		
		ChunkPos cpos = new ChunkPos(MHelper.floor(x / SquareBiomeChunk.WIDTH), MHelper.floor(z / SquareBiomeChunk.WIDTH));
		SquareBiomeChunk chunk = maps.get(cpos);
		if (chunk == null) {
			synchronized (random) {
				random.setLargeFeatureWithSalt(0, cpos.x, cpos.z, 0);
				chunk = new SquareBiomeChunk(random, picker);
			}
			maps.put(cpos, chunk);
		}
		
		return chunk.getBiome(MHelper.floor(x), MHelper.floor(z));
	}
}
