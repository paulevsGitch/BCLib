package ru.bclib.mixin.common;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.interfaces.SurfaceRuleProvider;

@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsMixin implements SurfaceRuleProvider {
	@Final
	@Shadow
	private SurfaceRules.RuleSource surfaceRule;
	
	private SurfaceRules.RuleSource bclib_surfaceRule;
	
	@Override
	public void setSurfaceRule(SurfaceRules.RuleSource surfaceRule) {
		bclib_surfaceRule = surfaceRule;
	}
	
	@Override
	public RuleSource getSurfaceRule() {
		return surfaceRule;
	}
	
	@Inject(method = "surfaceRule", at = @At("HEAD"), cancellable = true)
	private void bclib_surfaceRule(CallbackInfoReturnable<SurfaceRules.RuleSource> info) {
		if (bclib_surfaceRule != null) {
			info.setReturnValue(bclib_surfaceRule);
		}
	}
}
