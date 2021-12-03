package ru.bclib.mixin.common;

import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.api.biomes.BiomeAPI;

@Mixin(SurfaceRuleData.class)
public class SurfaceRuleDataMixin {
	@Inject(method = "nether", at = @At("RETURN"), cancellable = true)
	private static void bclib_addNetherRuleSource(CallbackInfoReturnable<SurfaceRules.RuleSource> info) {
		if (BiomeAPI.getNetherRuleSource() != null) {
			RuleSource source = info.getReturnValue();
			source = SurfaceRules.sequence(source, BiomeAPI.getNetherRuleSource());
			info.setReturnValue(source);
		}
	}
	
	@Inject(method = "end", at = @At("RETURN"), cancellable = true)
	private static void bclib_addEndRuleSource(CallbackInfoReturnable<SurfaceRules.RuleSource> info) {
		if (BiomeAPI.getEndRuleSource() != null) {
			RuleSource source = info.getReturnValue();
			source = SurfaceRules.sequence(source, BiomeAPI.getEndRuleSource());
			info.setReturnValue(source);
		}
	}
}
