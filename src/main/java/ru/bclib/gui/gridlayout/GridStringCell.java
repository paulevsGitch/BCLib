package ru.bclib.gui.gridlayout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import ru.bclib.gui.gridlayout.GridLayout.Alignment;
import ru.bclib.gui.gridlayout.GridLayout.GridValueType;

@Environment(EnvType.CLIENT)
class GridStringCell extends GridCell {
	GridStringCell(double width, GridValueType widthType, int height, Alignment contentAlignment, GridScreen parent, Component text) {
		this(width, widthType, height, contentAlignment, parent, text, GridLayout.COLOR_WHITE);
		
	}
	GridStringCell(double width, GridValueType widthType, int height, Alignment contentAlignment, GridScreen parent, Component text, int color) {
		super(width, height, widthType, null, (poseStack, transform, context) -> {
			if (contentAlignment == Alignment.CENTER) {
				parent.drawCenteredString(poseStack, parent.getFont(), text, transform.width / 2 + transform.left, transform.top, color);
			}
			else if (contentAlignment == Alignment.LEFT) {
				parent.drawString(poseStack, parent.getFont(), text, transform.left, transform.top, color);
			}
		});
	}
}
