package org.betterx.bclib.interfaces;

import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.SurfaceRules;

public interface SurfaceRuleProvider {
    void bclib_addBiomeSource(BiomeSource source);
    void bclib_clearBiomeSources();
    void bclib_overwrite(SurfaceRules.RuleSource surfaceRule);
}
