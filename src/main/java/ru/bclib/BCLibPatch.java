package ru.bclib;

import net.minecraft.nbt.CompoundTag;
import ru.bclib.api.datafixer.DataFixerAPI;
import ru.bclib.api.datafixer.ForcedLevelPatch;
import ru.bclib.api.datafixer.MigrationProfile;
import ru.bclib.world.generator.GeneratorOptions;

public final class BCLibPatch {
	public static void register(){
		if (GeneratorOptions.fixBiomeSource()) {
			DataFixerAPI.registerPatch(BiomeSourcePatch::new);
		}
	}
}

final class BiomeSourcePatch extends ForcedLevelPatch{
	private static final String NETHER_BIOME_SOURCE = "bclib:nether_biome_source";
	private static final String END_BIOME_SOURCE = "bclib:end_biome_source";
	
	protected BiomeSourcePatch() {
		super(BCLib.MOD_ID, "1.2.1");
	}
	
	@Override
	protected Boolean runLevelDatPatch(CompoundTag root, MigrationProfile profile) {
		CompoundTag worldGenSettings = root.getCompound("Data").getCompound("WorldGenSettings");
		CompoundTag dimensions = worldGenSettings.getCompound("dimensions");
		long seed = worldGenSettings.getLong("seed");
		boolean result = false;
		
		if (!dimensions.contains("minecraft:the_nether") || !isBCLibEntry(dimensions.getCompound("minecraft:the_nether"))) {
			CompoundTag dimRoot = new CompoundTag();
			dimRoot.put("generator", makeNetherGenerator(seed));
			dimRoot.putString("type", "minecraft:the_nether");
			dimensions.put("minecraft:the_nether", dimRoot);
			result = true;
		}
		
		if (!dimensions.contains("minecraft:the_end") || !isBCLibEntry(dimensions.getCompound("minecraft:the_end"))) {
			CompoundTag dimRoot = new CompoundTag();
			dimRoot.put("generator", makeEndGenerator(seed));
			dimRoot.putString("type", "minecraft:the_end");
			dimensions.put("minecraft:the_end", dimRoot);
			result = true;
		}
		
		return result;
	}
	
	private boolean isBCLibEntry(CompoundTag dimRoot) {
		String type = dimRoot.getCompound("generator").getCompound("biome_source").getString("type");
		if (type.isEmpty() || type.length() < 5) {
			return false;
		}
		return type.substring(0, 5).equals("bclib");
	}
	
	public static CompoundTag makeNetherGenerator(long seed) {
		CompoundTag generator = new CompoundTag();
		generator.putString("type", "minecraft:noise");
		generator.putString("settings", "minecraft:nether");
		generator.putLong("seed", seed);
		
		CompoundTag biomeSource = new CompoundTag();
		biomeSource.putString("type", NETHER_BIOME_SOURCE);
		biomeSource.putLong("seed", seed);
		generator.put("biome_source", biomeSource);
		
		return generator;
	}
	
	public static CompoundTag makeEndGenerator(long seed) {
		CompoundTag generator = new CompoundTag();
		generator.putString("type", "minecraft:noise");
		generator.putString("settings", "minecraft:end");
		generator.putLong("seed", seed);
		
		CompoundTag biomeSource = new CompoundTag();
		biomeSource.putString("type", END_BIOME_SOURCE);
		biomeSource.putLong("seed", seed);
		generator.put("biome_source", biomeSource);
		
		return generator;
	}
}
