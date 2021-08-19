package ru.bclib.gui.gridlayout;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import ru.bclib.gui.gridlayout.GridLayout.Alignment;


@Environment(EnvType.CLIENT)
public abstract class GridScreen extends Screen {
	protected GridLayout grid = null;
	public final int topPadding;
	public final int sidePadding;
	public final boolean centerVertically;
	@Nullable
	public final Screen parent;
	
	public GridScreen(Component title) {
		this(null, title);
	}
	
	public GridScreen(@Nullable Screen parent, Component title){
		this(parent, title, 0, true);
	}
	
	public GridScreen(Component title, int topPadding, boolean centerVertically) {
		this(null, title, topPadding, 20, centerVertically);
	}
	
	public GridScreen(@Nullable Screen parent, Component title, int topPadding, boolean centerVertically) {
		this(parent, title, topPadding, 20, centerVertically);
	}
	
	public GridScreen(Component title, int topPadding, int sidePadding, boolean centerVertically) {
		this(null, title, topPadding, sidePadding, centerVertically);
	}
	
	public GridScreen(@Nullable Screen parent, Component title, int topPadding, int sidePadding, boolean centerVertically) {
		super(title);
		
		this.parent = parent;
		this.topPadding = topPadding;
		this.sidePadding = sidePadding;
		this.centerVertically = centerVertically;
	}
	
	@Override
	public void onClose() {
		this.minecraft.setScreen(parent);
	}
	
	public Font getFont(){
		return this.font;
	}
	
	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T guiEventListener) {
		return super.addRenderableWidget(guiEventListener);
	}
	
	
	
	protected void addTitle(){
		grid.addRow().addString(this.title, Alignment.CENTER, this);
		grid.addSpacerRow(15);
	}
	
	final protected void init() {
		super.init();
		this.grid = new GridLayout(this, this.topPadding, this.sidePadding, this.centerVertically);
		
		addTitle();
		
		initLayout();
		grid.finalizeLayout();
	}
	
	protected abstract void initLayout();
	
	protected void renderScreen(PoseStack poseStack, int i, int j, float f) {
		super.render(poseStack, i, j, f);
	}
	
	public void render(PoseStack poseStack, int i, int j, float f) {
		//this.renderBackground(poseStack);
		this.renderDirtBackground(i);
		//drawCenteredString(poseStack, this.font, this.title, grid.width / 2, grid.getTopStart(), 16777215);
		if (grid!=null) grid.render(poseStack);
		
		super.render(poseStack, i, j, f);
	}
}
