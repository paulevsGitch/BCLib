package ru.bclib.interfaces;

import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;

import java.util.List;

public interface SurfaceRuleProvider {
	void clearCustomRules();
	void addCustomRules(List<RuleSource> rules);
}
