package ru.bclib.mixin.common;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import ru.bclib.interfaces.SurfaceRuleProvider;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsMixin implements SurfaceRuleProvider {
	@Mutable
	@Final
	@Shadow
	private SurfaceRules.RuleSource surfaceRule;
	
	private SurfaceRules.RuleSource bclib_originalSurfaceRule;
	
	@Override
	public void clearCustomRules() {
		if (bclib_originalSurfaceRule != null){
			this.surfaceRule = bclib_originalSurfaceRule;
			bclib_originalSurfaceRule = null;
		}
	}
	
	@Override
	public void addCustomRules(List<RuleSource> rules) {
		RuleSource org = getOriginalSurfaceRule();
		if (org instanceof SurfaceRules.SequenceRuleSource sequenceRule){
			List<RuleSource> currentSequence = sequenceRule.sequence();
			rules = rules.stream().filter(r -> currentSequence.indexOf(r)<0).collect(Collectors.toList());
			rules.addAll(sequenceRule.sequence());
		} else {
			rules.add(org);
		}
		setSurfaceRule(SurfaceRules.sequence(rules.toArray(new RuleSource[rules.size()])));
	}
	
	void setSurfaceRule(SurfaceRules.RuleSource surfaceRule) {
		if (bclib_originalSurfaceRule == null){
			bclib_originalSurfaceRule = this.surfaceRule;
		}
		this.surfaceRule = surfaceRule;
	}
	
	RuleSource getOriginalSurfaceRule() {
		if (bclib_originalSurfaceRule ==null) {
			return surfaceRule;
		}
		
		return bclib_originalSurfaceRule;
	}
	
//	@Inject(method = "surfaceRule", at = @At("HEAD"), cancellable = true)
//	private void bclib_surfaceRule(CallbackInfoReturnable<SurfaceRules.RuleSource> info) {
//		if (bclib_surfaceRule != null) {
//			info.setReturnValue(bclib_surfaceRule);
//		}
//	}
}
