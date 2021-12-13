package ru.bclib.api.surface.rules;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.Condition;
import net.minecraft.world.level.levelgen.SurfaceRules.ConditionSource;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;
import net.minecraft.world.level.levelgen.SurfaceRules.LazyCondition;
import ru.bclib.mixin.common.SurfaceRulesContextAccessor;

public abstract class VolumeNoiseCondition implements NoiseCondition{
	@Override
	public Codec<? extends ConditionSource> codec() {
		return SurfaceRules.ConditionSource.CODEC;
	}
	
	@Override
	public final Condition apply(Context context2) {
		final VolumeNoiseCondition self = this;
		
		class Generator extends LazyCondition {
			Generator() {
				super(context2);
			}
			
			@Override
			protected long getContextLastUpdate() {
				final SurfaceRulesContextAccessor ctx = SurfaceRulesContextAccessor.class.cast(this.context);
				return  ctx.getLastUpdateY() + ctx.getLastUpdateXZ();
			}
			
			@Override
			protected boolean compute() {
				final SurfaceRulesContextAccessor context = SurfaceRulesContextAccessor.class.cast(this.context);
				if (context==null) return false;
				return self.test(context);
			}
		}
		
		return new Generator();
	}
	
	public abstract boolean test(SurfaceRulesContextAccessor context);
}
