package ru.bclib.gui.gridlayout;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.gui.gridlayout.GridLayout.GridValueType;

@Environment(EnvType.CLIENT)
public class GridImageCell extends GridCell{
	GridImageCell(ResourceLocation location, double width, GridValueType widthType, double height, float alpha, int uvLeft, int uvTop, int uvWidth, int uvHeight, int resourceWidth, int resourceHeight) {
		super(width, height, widthType, null, (poseStack, transform, context) -> {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, location);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
			GuiComponent.blit(poseStack, transform.left, transform.top, transform.width, transform.height, uvLeft, uvTop, uvWidth, uvHeight, resourceWidth, resourceHeight);
		});
	}
}
