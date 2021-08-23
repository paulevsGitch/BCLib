package ru.bclib.gui.gridlayout;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.bclib.gui.gridlayout.GridLayout.GridValueType;


@Environment(EnvType.CLIENT)
public abstract class GridCustomRenderCell extends GridCell{
	protected GridCustomRenderCell(double width, GridValueType widthType, double height) {
		super(width, height, widthType, null, null);
		this.customRender = this::onRender;
	}
	
	public abstract void onRender(PoseStack poseStack, GridTransform transform, Object context);
}
