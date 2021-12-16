package ru.bclib;

import net.minecraft.nbt.CompoundTag;
import ru.bclib.api.datafixer.DataFixerAPI;
import ru.bclib.api.datafixer.ForcedLevelPatch;
import ru.bclib.api.datafixer.MigrationProfile;
import ru.bclib.api.datafixer.Patch;
import ru.bclib.config.Configs;
import ru.bclib.interfaces.PatchFunction;



public final class BCLibPatch {
	public static void register(){
		DataFixerAPI.registerPatch(BiomeSourcePatch::new);
		if (Configs.MAIN_CONFIG.repairBiomes()) {
			DataFixerAPI.registerPatch(BiomeFixPatch::new);
		}
	}
}

final class BiomeFixPatch extends ForcedLevelPatch{
	protected BiomeFixPatch() {
		super(BCLib.MOD_ID, "0.5.0");
	}
	
	@Override
	protected Boolean runLevelDatPatch(CompoundTag root, MigrationProfile profile) {
		CompoundTag worldGenSettings = root.getCompound("Data").getCompound("WorldGenSettings");
		CompoundTag dimensions = worldGenSettings.getCompound("dimensions");
		long seed = worldGenSettings.getLong("seed");
		boolean result = false;
		
		if (!dimensions.contains("minecraft:the_nether")) {
			BCLib.LOGGER.info("Repairing Nether biome source");
			result = true;
			
			CompoundTag dimRoot = new CompoundTag();
			dimRoot.put("generator", BiomeSourcePatch.makeNetherGenerator(seed));
			dimRoot.putString("type", "minecraft:the_nether");
			dimensions.put("minecraft:the_nether", dimRoot);
		} else {
			result |= BiomeSourcePatch.repairNetherSource(dimensions, seed);
		}
		
		if (!dimensions.contains("minecraft:the_end")) {
			BCLib.LOGGER.info("Repairing End biome source");
			result = true;
			
			CompoundTag dimRoot = new CompoundTag();
			dimRoot.put("generator", BiomeSourcePatch.makeEndGenerator(seed));
			dimRoot.putString("type", "minecraft:the_end");
			dimensions.put("minecraft:the_end", dimRoot);
		} else {
			result |= BiomeSourcePatch.repairEndSource(dimensions, seed);
		}
		
		return result;
	}
}

final class BiomeSourcePatch extends Patch {
	private static final String NETHER_BIOME_SOURCE = "bclib:nether_biome_source";
	private static final String END_BIOME_SOURCE = "bclib:end_biome_source";
	
	protected BiomeSourcePatch() {
		super(BCLib.MOD_ID, "0.4.0");
	}
	
	public PatchFunction<CompoundTag, Boolean> getLevelDatPatcher() {
		return BiomeSourcePatch::fixBiomeSources;
	}
	
	private static boolean fixBiomeSources(CompoundTag root, MigrationProfile profile) {
		CompoundTag worldGenSettings = root.getCompound("Data").getCompound("WorldGenSettings");
		CompoundTag dimensions = worldGenSettings.getCompound("dimensions");
		long seed = worldGenSettings.getLong("seed");
		boolean result = false;
		
		if (dimensions.contains("minecraft:the_nether")) {
			result |= repairNetherSource(dimensions, seed);
		}
		
		if (dimensions.contains("minecraft:the_end")) {
			result |= repairEndSource(dimensions, seed);
		}
		
		return result;
	}
	
	public static boolean repairEndSource(CompoundTag dimensions, long seed) {
		CompoundTag dimRoot = dimensions.getCompound("minecraft:the_end");
		CompoundTag biomeSource = dimRoot.getCompound("generator").getCompound("biome_source");
		if (!biomeSource.getString("type").equals(END_BIOME_SOURCE)) {
			BCLib.LOGGER.info("Applying End biome source patch");
			dimRoot.put("generator", makeEndGenerator(seed));
			return true;
		}
		
		return false;
	}
	
	public static boolean repairNetherSource(CompoundTag dimensions, long seed) {
		CompoundTag dimRoot = dimensions.getCompound("minecraft:the_nether");
		CompoundTag biomeSource = dimRoot.getCompound("generator").getCompound("biome_source");
		if (!biomeSource.getString("type").equals(NETHER_BIOME_SOURCE)) {
			BCLib.LOGGER.info("Applying Nether biome source patch");
			dimRoot.put("generator", makeNetherGenerator(seed));
			return true;
		}
		
		return false;
	}
	
	;
	
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
