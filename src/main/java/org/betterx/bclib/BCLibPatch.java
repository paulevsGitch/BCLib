package org.betterx.bclib;

import net.minecraft.nbt.CompoundTag;

import org.betterx.bclib.api.datafixer.DataFixerAPI;
import org.betterx.bclib.api.datafixer.ForcedLevelPatch;
import org.betterx.bclib.api.datafixer.MigrationProfile;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.world.generator.GeneratorOptions;

public final class BCLibPatch {
    public static void register() {
        // TODO separate values in config on client side (config screen)
        if (Configs.MAIN_CONFIG.repairBiomes() && (GeneratorOptions.fixEndBiomeSource() || GeneratorOptions.fixNetherBiomeSource())) {
            DataFixerAPI.registerPatch(BiomeSourcePatch::new);
        }
    }
}

final class BiomeSourcePatch extends ForcedLevelPatch {
    private static final String NETHER_BIOME_SOURCE = "bclib:nether_biome_source";
    private static final String END_BIOME_SOURCE = "bclib:end_biome_source";
    private static final String MC_NETHER = "minecraft:the_nether";
    private static final String MC_END = "minecraft:the_end";

    protected BiomeSourcePatch() {
        super(BCLib.MOD_ID, "1.2.1");
    }

    @Override
    protected Boolean runLevelDatPatch(CompoundTag root, MigrationProfile profile) {
        CompoundTag worldGenSettings = root.getCompound("Data").getCompound("WorldGenSettings");
        CompoundTag dimensions = worldGenSettings.getCompound("dimensions");
        long seed = worldGenSettings.getLong("seed");
        boolean result = false;

        if (GeneratorOptions.fixNetherBiomeSource()) {
            if (!dimensions.contains(MC_NETHER) || !isBCLibEntry(dimensions.getCompound(MC_NETHER))) {
                CompoundTag dimRoot = new CompoundTag();
                dimRoot.put("generator", makeNetherGenerator(seed));
                dimRoot.putString("type", MC_NETHER);
                dimensions.put(MC_NETHER, dimRoot);
                result = true;
            }
        }

        if (GeneratorOptions.fixEndBiomeSource()) {
            if (!dimensions.contains(MC_END) || !isBCLibEntry(dimensions.getCompound(MC_END))) {
                CompoundTag dimRoot = new CompoundTag();
                dimRoot.put("generator", makeEndGenerator(seed));
                dimRoot.putString("type", MC_END);
                dimensions.put(MC_END, dimRoot);
                result = true;
            }
        }

        return result;
    }

    private boolean isBCLibEntry(CompoundTag dimRoot) {
        String type = dimRoot.getCompound("generator").getCompound("biome_source").getString("type");
        if (type.isEmpty() || type.length() < 5) {
            return false;
        }
        return type.startsWith("bclib");
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
