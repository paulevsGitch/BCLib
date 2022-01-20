package ru.bclib.api.surface.rules;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.SurfaceRules.SurfaceRule;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import org.jetbrains.annotations.Nullable;
import ru.bclib.interfaces.NumericProvider;
import ru.bclib.mixin.common.SurfaceRulesContextAccessor;

import java.util.List;
import java.util.function.Function;

//
public record SwitchRuleSource(NumericProvider selector, List<RuleSource> collection) implements RuleSource {
	public static final Codec<SwitchRuleSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			NumericProvider.CODEC.fieldOf("selector").forGetter(SwitchRuleSource::selector),
			RuleSource.CODEC.listOf().fieldOf("collection").forGetter(SwitchRuleSource::collection)
	).apply(instance, SwitchRuleSource::new));

	@Override
	public Codec<? extends RuleSource> codec() {
		return SwitchRuleSource.CODEC;
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

	static {
		Registry.register(Registry.RULE, "bclib_switch_rule", SwitchRuleSource.CODEC);
	}
}
