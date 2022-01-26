package ru.bclib.interfaces;

import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;

import java.util.List;

public interface SurfaceRuleProvider {
	void bclib_addBiomeSource(BiomeSource source);
	void bclib_clearBiomeSources();
}
