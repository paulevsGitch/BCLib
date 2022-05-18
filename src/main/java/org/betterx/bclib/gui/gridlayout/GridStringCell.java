package org.betterx.bclib.gui.gridlayout;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.betterx.bclib.gui.gridlayout.GridLayout.Alignment;
import org.betterx.bclib.gui.gridlayout.GridLayout.GridValueType;

@Environment(EnvType.CLIENT)
public class GridStringCell extends GridCell {
    private Component text;

    GridStringCell(double width,
                   GridValueType widthType,
                   int height,
                   Alignment contentAlignment,
                   GridScreen parent,
                   Component text) {
        this(width, widthType, height, contentAlignment, parent, text, GridLayout.COLOR_WHITE);

    }

    GridStringCell(double width,
                   GridValueType widthType,
                   int height,
                   Alignment contentAlignment,
                   GridScreen parent,
                   Component text,
                   int color) {
        super(width, height, widthType, null, null);
        this.text = text;
        this.customRender = (poseStack, transform, context) -> {
            if (contentAlignment == Alignment.CENTER) {
                GuiComponent.drawCenteredString(poseStack,
                                                parent.getFont(),
                                                this.text,
                                                transform.width / 2 + transform.left,
                                                transform.top,
                                                color);
            } else if (contentAlignment == Alignment.LEFT) {
                GuiComponent.drawString(poseStack, parent.getFont(), this.text, transform.left, transform.top, color);
            }
        };
    }

    public void setText(Component newText) {
        this.text = newText;
    }
}
