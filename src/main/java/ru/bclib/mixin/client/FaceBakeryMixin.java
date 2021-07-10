package ru.bclib.mixin.client;

import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.FaceBakery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FaceBakery.class)
public class FaceBakeryMixin {
	@Inject(method = "applyModelRotation", at = @At("HEAD"), cancellable = true)
	public void bclib_applyModelTransforms(Vector3f vector3f, Transformation transformation, CallbackInfo info) {
		if (transformation != Transformation.identity()) {
			Vector3f scale = transformation.getScale();
			this.rotateVertexBy(vector3f, new Vector3f(0.5F, 0.5F, 0.5F), transformation.getMatrix(), new Vector3f(1.0F, 1.0F, 1.0F));
			vector3f.mul(scale.x(), scale.y(), scale.z());
			vector3f.add(transformation.getTranslation());
			info.cancel();
		}
	}
	
	@Shadow
	private void rotateVertexBy(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f, Vector3f vector3f3) {}
}
