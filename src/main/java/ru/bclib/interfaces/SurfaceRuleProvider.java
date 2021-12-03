package ru.bclib.interfaces;

import net.minecraft.world.level.levelgen.SurfaceRules;

public interface SurfaceRuleProvider {
	void setSurfaceRule(SurfaceRules.RuleSource surfaceRule);
	
	SurfaceRules.RuleSource getSurfaceRule();
}
