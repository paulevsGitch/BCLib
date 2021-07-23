package ru.bclib.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.client.render.CustomBackgroundRenderer;
import ru.bclib.util.BackgroundInfo;

@Mixin(FogRenderer.class)
public class BackgroundRendererMixin {
	private static final MutableBlockPos BCLIB_LAST_POS = new MutableBlockPos(0, -100, 0);
	private static final MutableBlockPos BCLIB_MUT_POS = new MutableBlockPos();
	private static final float[] BCLIB_FOG_DENSITY = new float[8];
	
	@Shadow
	private static float fogRed;
	@Shadow
	private static float fogGreen;
	@Shadow
	private static float fogBlue;
	
	@Inject(method = "setupColor", at = @At("RETURN"))
	private static void bclib_onRender(Camera camera, float tickDelta, ClientLevel world, int i, float f, CallbackInfo info) {
		FogType fogType = camera.getFluidInCamera();
		if (fogType != FogType.WATER && world.dimension().equals(Level.END)) {
			Entity entity = camera.getEntity();
			boolean skip = false;
			if (entity instanceof LivingEntity) {
				MobEffectInstance effect = ((LivingEntity) entity).getEffect(MobEffects.NIGHT_VISION);
				skip = effect != null && effect.getDuration() > 0;
			}
			if (!skip) {
				fogRed *= 4;
				fogGreen *= 4;
				fogBlue *= 4;
			}
		}
		
		BackgroundInfo.fogColorRed = fogRed;
		BackgroundInfo.fogColorGreen = fogGreen;
		BackgroundInfo.fogColorBlue = fogBlue;
	}
	
	@Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
	private static void bclib_fogDensity(Camera camera, FogRenderer.FogMode fogMode, float viewDistance, boolean thickFog, CallbackInfo info) {
		if (CustomBackgroundRenderer.applyFogDensity(camera, fogMode, viewDistance, thickFog)) {
			info.cancel();
		}
	}
}
