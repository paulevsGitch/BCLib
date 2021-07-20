package ru.bclib.mixin.client;

import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.client.render.EmissiveTexturesInfo;

import java.util.Arrays;

@Mixin(value = AoCalculator.class, remap = false)
public class AoCalculatorMixin {
	@Final
	@Shadow
	public int[] light;
	
	@Final
	@Shadow
	public float[] ao;
	
	@Inject(method = "compute", at = @At("HEAD"), cancellable = true)
	private void bclib_computeLight(MutableQuadViewImpl quad, boolean isVanilla, CallbackInfo info) {
		/*float u = quad.spriteU(0, 0);
		float v = quad.spriteV(0, 0);
		if (EmissiveTexturesInfo.isEmissive(u, v)) {
			quad.material(EmissiveTexturesInfo.getEmissiveMaterial());
			for (int i = 0; i < 4; i++) {
				quad.lightmap(0, 255);
			}
			Arrays.fill(light, 255);
			Arrays.fill(ao, 1F);
			info.cancel();
		}*/
		
		/*quad.shade(false);
		for (int i = 0; i < 4; i++) {
			quad.lightmap(i, 255);
		}*/
	}
}
