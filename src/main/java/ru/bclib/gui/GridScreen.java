package ru.bclib.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public abstract class GridScreen extends Screen {
	protected GridLayout grid = null;
	public final int topStart;
	
	public GridScreen(int topStart, Component title) {
		super(title);
		this.topStart = topStart;
	}
	
	final protected void init() {
		super.init();
		this.grid = new GridLayout(topStart, this.width, this.height, this.font, this::addRenderableWidget);
		initLayout();
		grid.finalizeLayout();
	}
	
	protected abstract void initLayout();
	
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, grid.width / 2, grid.getTopStart(), 16777215);
		if (grid!=null) grid.render(poseStack);
		super.render(poseStack, i, j, f);
	}
}
