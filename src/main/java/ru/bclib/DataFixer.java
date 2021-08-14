package ru.bclib;

import net.minecraft.nbt.CompoundTag;
import ru.bclib.api.datafixer.DataFixerAPI;
import ru.bclib.api.datafixer.Patch;
import ru.bclib.api.datafixer.PatchFunction;

public class DataFixer {
	private static final String NETHER_BIOME_SOURCE = "bclib:nether_biome_source";
	private static final String END_BIOME_SOURCE = "bclib:end_biome_source";
	
	public static void register() {
		DataFixerAPI.registerPatch(() -> new BCLibPatch());
	}
	
	private static final class BCLibPatch extends Patch {
		protected BCLibPatch() {
			super(BCLib.MOD_ID, "0.4.0");
		}
		
		public PatchFunction<CompoundTag, Boolean> getLevelDatPatcher() {
			return (root, profile) -> {
				CompoundTag worldGenSettings = root.getCompound("Data").getCompound("WorldGenSettings");
				CompoundTag dimensions = worldGenSettings.getCompound("dimensions");
				long seed = worldGenSettings.getLong("seed");
				boolean result = false;
				
				if (dimensions.contains("minecraft:the_nether")) {
					CompoundTag dimRoot = dimensions.getCompound("minecraft:the_nether");
					CompoundTag biomeSource = dimRoot.getCompound("generator").getCompound("biome_source");
					if (!biomeSource.getString("type").equals(NETHER_BIOME_SOURCE)) {
						BCLib.LOGGER.info("Applying Nether biome source patch");
						dimRoot.put("generator", makeNetherGenerator(seed));
						result = true;
					}
				}
				
				if (dimensions.contains("minecraft:the_end")) {
					CompoundTag dimRoot = dimensions.getCompound("minecraft:the_end");
					CompoundTag biomeSource = dimRoot.getCompound("generator").getCompound("biome_source");
					if (!biomeSource.getString("type").equals(END_BIOME_SOURCE)) {
						BCLib.LOGGER.info("Applying End biome source patch");
						dimRoot.put("generator", makeEndGenerator(seed));
						result = true;
					}
				}
				
				return result;
			};
		}
		
		private CompoundTag makeNetherGenerator(long seed) {
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
		
		private CompoundTag makeEndGenerator(long seed) {
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
}
