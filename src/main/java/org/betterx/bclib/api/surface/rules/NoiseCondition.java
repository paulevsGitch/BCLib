package org.betterx.bclib.api.surface.rules;

import net.minecraft.world.level.levelgen.SurfaceRules;

import org.betterx.bclib.mixin.common.SurfaceRulesContextAccessor;

public interface NoiseCondition extends SurfaceRules.ConditionSource {
    boolean test(SurfaceRulesContextAccessor context);
}
