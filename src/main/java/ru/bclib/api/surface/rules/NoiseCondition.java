package ru.bclib.api.surface.rules;

import net.minecraft.world.level.levelgen.SurfaceRules;
import ru.bclib.mixin.common.SurfaceRulesContextAccessor;

public interface NoiseCondition extends SurfaceRules.ConditionSource{
	boolean test(SurfaceRulesContextAccessor context);
}
