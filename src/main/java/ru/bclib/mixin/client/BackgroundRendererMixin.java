package ru.bclib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FluidState;
import ru.bclib.api.BiomeAPI;
import ru.bclib.util.BackgroundInfo;
import ru.bclib.util.MHelper;
import ru.bclib.world.biomes.BCLBiome;

@Mixin(FogRenderer.class)
public class BackgroundRendererMixin {
	private static final MutableBlockPos BCL_MUT_POS = new MutableBlockPos();
	
	@Shadow
	private static float fogRed;
	@Shadow
	private static float fogGreen;
	@Shadow
	private static float fogBlue;
	
	@Inject(method = "setupColor", at = @At("RETURN"))
	private static void bcl_onRender(Camera camera, float tickDelta, ClientLevel world, int i, float f, CallbackInfo info) {
		FluidState fluidState = camera.getFluidInCamera();
		if (fluidState.isEmpty() && world.dimension().equals(Level.END)) {
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
	private static void bcl_fogDensity(Camera camera, FogRenderer.FogMode fogType, float viewDistance, boolean thickFog, CallbackInfo info) {
		Entity entity = camera.getEntity();
		FluidState fluidState = camera.getFluidInCamera();
		if (fluidState.isEmpty()) {
			float fog = bcl_getFogDensity(entity.level, entity.getX(), entity.getEyeY(), entity.getZ());
			BackgroundInfo.fogDensity = fog;
			float start = viewDistance * 0.75F / fog;
			float end = viewDistance / fog;
			
			if (entity instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) entity;
				MobEffectInstance effect = le.getEffect(MobEffects.BLINDNESS);
				if (effect != null) {
					int duration = effect.getDuration();
					if (duration > 20) {
						start = 0;
						end *= 0.03F;
						BackgroundInfo.blindness = 1;
					}
					else {
						float delta = (float) duration / 20F;
						BackgroundInfo.blindness = delta;
						start = Mth.lerp(delta, start, 0);
						end = Mth.lerp(delta, end, end * 0.03F);
					}
				}
				else {
					BackgroundInfo.blindness = 0;
				}
			}
			
			RenderSystem.fogStart(start);
			RenderSystem.fogEnd(end);
			RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
			RenderSystem.setupNvFogDistance();
			info.cancel();
		}
	}
	
	private static float bcl_getFogDensityI(Level level, int x, int y, int z) {
		BCL_MUT_POS.set(x, y, z);
		Biome biome = level.getBiome(BCL_MUT_POS);
		BCLBiome renderBiome = BiomeAPI.getRenderBiome(biome);
		return renderBiome.getFogDensity();
	}
	
	private static float bcl_getFogDensity(Level level, double x, double y, double z) {
		int x1 = (MHelper.floor(x) >> 3) << 3;
		int y1 = (MHelper.floor(y) >> 3) << 3;
		int z1 = (MHelper.floor(z) >> 3) << 3;
		int x2 = x1 + 8;
		int y2 = y1 + 8;
		int z2 = z1 + 8;
		float dx = (float) (x - x1) / 8F;
		float dy = (float) (y - y1) / 8F;
		float dz = (float) (z - z1) / 8F;
		
		float a = bcl_getFogDensityI(level, x1, y1, z1);
		float b = bcl_getFogDensityI(level, x2, y1, z1);
		float c = bcl_getFogDensityI(level, x1, y2, z1);
		float d = bcl_getFogDensityI(level, x2, y2, z1);
		float e = bcl_getFogDensityI(level, x1, y1, z2);
		float f = bcl_getFogDensityI(level, x2, y1, z2);
		float g = bcl_getFogDensityI(level, x1, y2, z2);
		float h = bcl_getFogDensityI(level, x2, y2, z2);
		
		a = Mth.lerp(dx, a, b);
		b = Mth.lerp(dx, c, d);
		c = Mth.lerp(dx, e, f);
		d = Mth.lerp(dx, g, h);
		
		a = Mth.lerp(dy, a, b);
		b = Mth.lerp(dy, c, d);
		
		return Mth.lerp(dz, c, d);
	}
}
