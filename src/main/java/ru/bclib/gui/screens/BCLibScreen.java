package ru.bclib.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.BCLib;
import ru.bclib.gui.gridlayout.GridLayout.GridValueType;
import ru.bclib.gui.gridlayout.GridLayout.VerticalAlignment;
import ru.bclib.gui.gridlayout.GridRow;
import ru.bclib.gui.gridlayout.GridScreen;

@Environment(EnvType.CLIENT)
abstract class BCLibScreen extends GridScreen {
	static final ResourceLocation BCLIB_LOGO_LOCATION = new ResourceLocation(BCLib.MOD_ID, "icon.png");
	
	public BCLibScreen(Component title) {
		super(title);
	}
	
	public BCLibScreen(Component title, int topPadding, boolean centerVertically) {
		super(title, topPadding, centerVertically);
	}
	
	public BCLibScreen(Component title, int topPadding, int sidePadding, boolean centerVertically) {
		super(title, topPadding, sidePadding, centerVertically);
	}
	
	protected void addTitle(){
		GridRow row = grid.addRow(VerticalAlignment.CENTER);
		row.addFiller();
		row.addImage(BCLIB_LOGO_LOCATION, 24, GridValueType.CONSTANT, 24, 512, 512);
		row.addSpacer(4);
		row.addString(this.title, font.lineHeight,this);
		row.addFiller();
		grid.addSpacerRow(15);
	}
	
	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderDirtBackground(i);
//
//		RenderSystem.setShader(GameRenderer::getPositionTexShader);
//		RenderSystem.setShaderTexture(0, BCLIB_LOGO_LOCATION);
//		RenderSystem.enableBlend();
//		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
//		blit(poseStack, 0, 0, 32, 32, 0, 0, 512, 512, 512, 512);
		
		if (grid!=null) grid.render(poseStack);
		super.renderScreen(poseStack, i, j, f);
	}
}
