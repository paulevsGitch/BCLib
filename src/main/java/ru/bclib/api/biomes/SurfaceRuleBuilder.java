package ru.bclib.api.biomes;

import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.ConditionSource;

import java.util.List;

public class SurfaceRuleBuilder {
	private static final SurfaceRuleBuilder INSTANCE = new SurfaceRuleBuilder();
	private List<ConditionSource> conditions;
	
	private SurfaceRuleBuilder() {}
	
	public static SurfaceRuleBuilder start() {
		return INSTANCE;
	}
	
	public static SurfaceRules.RuleSource build() {
		return null;
	}
}
