package ru.bclib.mixin.client;

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
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.api.BiomeAPI;
import ru.bclib.util.BackgroundInfo;
import ru.bclib.util.MHelper;
import ru.bclib.world.biomes.BCLBiome;

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
		Entity entity = camera.getEntity();
		FogType fogType = camera.getFluidInCamera();
		if (fogType != FogType.WATER) {
			if (bclib_shouldIgnore(entity.level, (int) entity.getX(), (int) entity.getEyeY(), (int) entity.getZ())) {
				return;
			}
			float fog = bclib_getFogDensity(entity.level, entity.getX(), entity.getEyeY(), entity.getZ());
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
			
			RenderSystem.setShaderFogStart(start);
			RenderSystem.setShaderFogEnd(end);
			info.cancel();
		}
	}
	
	private static boolean bclib_shouldIgnore(Level level, int x, int y, int z) {
		Biome biome = level.getBiome(BCLIB_MUT_POS.set(x, y, z));
		return BiomeAPI.getRenderBiome(biome) == BiomeAPI.EMPTY_BIOME;
	}
	
	private static float bclib_getFogDensityI(Level level, int x, int y, int z) {
		Biome biome = level.getBiome(BCLIB_MUT_POS.set(x, y, z));
		BCLBiome renderBiome = BiomeAPI.getRenderBiome(biome);
		return renderBiome.getFogDensity();
	}
	
	private static float bclib_getFogDensity(Level level, double x, double y, double z) {
		int x1 = (MHelper.floor(x) >> 3) << 3;
		int y1 = (MHelper.floor(y) >> 3) << 3;
		int z1 = (MHelper.floor(z) >> 3) << 3;
		float dx = (float) (x - x1) / 8F;
		float dy = (float) (y - y1) / 8F;
		float dz = (float) (z - z1) / 8F;
		
		if (BCLIB_LAST_POS.getX() != x1 || BCLIB_LAST_POS.getY() != y1 || BCLIB_LAST_POS.getZ() != z1) {
			int x2 = x1 + 8;
			int y2 = y1 + 8;
			int z2 = z1 + 8;
			BCLIB_LAST_POS.set(x1, y1, z1);
			BCLIB_FOG_DENSITY[0] = bclib_getFogDensityI(level, x1, y1, z1);
			BCLIB_FOG_DENSITY[1] = bclib_getFogDensityI(level, x2, y1, z1);
			BCLIB_FOG_DENSITY[2] = bclib_getFogDensityI(level, x1, y2, z1);
			BCLIB_FOG_DENSITY[3] = bclib_getFogDensityI(level, x2, y2, z1);
			BCLIB_FOG_DENSITY[4] = bclib_getFogDensityI(level, x1, y1, z2);
			BCLIB_FOG_DENSITY[5] = bclib_getFogDensityI(level, x2, y1, z2);
			BCLIB_FOG_DENSITY[6] = bclib_getFogDensityI(level, x1, y2, z2);
			BCLIB_FOG_DENSITY[7] = bclib_getFogDensityI(level, x2, y2, z2);
		}
		
		float a = Mth.lerp(dx, BCLIB_FOG_DENSITY[0], BCLIB_FOG_DENSITY[1]);
		float b = Mth.lerp(dx, BCLIB_FOG_DENSITY[2], BCLIB_FOG_DENSITY[3]);
		float c = Mth.lerp(dx, BCLIB_FOG_DENSITY[4], BCLIB_FOG_DENSITY[5]);
		float d = Mth.lerp(dx, BCLIB_FOG_DENSITY[6], BCLIB_FOG_DENSITY[7]);
		
		a = Mth.lerp(dy, a, b);
		b = Mth.lerp(dy, c, d);
		
		return Mth.lerp(dz, a, b);
	}
}
