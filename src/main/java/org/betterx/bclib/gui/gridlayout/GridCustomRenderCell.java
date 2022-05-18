package org.betterx.bclib.gui.gridlayout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.mojang.blaze3d.vertex.PoseStack;
import org.betterx.bclib.gui.gridlayout.GridLayout.GridValueType;


@Environment(EnvType.CLIENT)
public abstract class GridCustomRenderCell extends GridCell {
    protected GridCustomRenderCell(double width, GridValueType widthType, double height) {
        super(width, height, widthType, null, null);
        this.customRender = this::onRender;
    }

    public abstract void onRender(PoseStack poseStack, GridTransform transform, Object context);
}
