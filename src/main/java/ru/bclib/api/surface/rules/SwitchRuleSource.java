package ru.bclib.api.surface.rules;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.SurfaceRules.SurfaceRule;
import org.jetbrains.annotations.Nullable;
import ru.bclib.interfaces.NumericProvider;
import ru.bclib.mixin.common.SurfaceRulesContextAccessor;

import java.util.List;

public record SwitchRuleSource(NumericProvider selector, List<RuleSource> collection) implements RuleSource {
	@Override
	public Codec<? extends RuleSource> codec() {
		return RuleSource.CODEC;
	}
	
	@Override
	public SurfaceRule apply(Context context) {
		
		return new SurfaceRule() {
			@Nullable
			@Override
			public BlockState tryApply(int x, int y, int z) {
				final SurfaceRulesContextAccessor ctx = SurfaceRulesContextAccessor.class.cast(context);
				int nr = Math.max(0, selector.getNumber(ctx)) % collection.size();
				
				return collection.get(nr).apply(context).tryApply(x, y, z);
			}
		};
		
	}
}
