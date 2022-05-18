package org.betterx.bclib.interfaces;

import net.minecraft.world.level.biome.BiomeSource;

public interface SurfaceRuleProvider {
    void bclib_addBiomeSource(BiomeSource source);
    void bclib_clearBiomeSources();
}
