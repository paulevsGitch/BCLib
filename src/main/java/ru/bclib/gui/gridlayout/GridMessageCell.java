package ru.bclib.gui.gridlayout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import ru.bclib.gui.gridlayout.GridLayout.Alignment;
import ru.bclib.gui.gridlayout.GridLayout.GridValueType;

import java.util.List;

@Environment(EnvType.CLIENT)
class GridMessageCell extends GridCell {
	private final Font font;
	private final Component text;
	private MultiLineLabel label;
	
	GridMessageCell(double width, GridValueType widthType, Alignment contentAlignment, Font font, Component text) {
		this(width, widthType, contentAlignment, font, text, GridLayout.COLOR_WHITE);
	}
	GridMessageCell(double width, GridValueType widthType, Alignment contentAlignment, Font font, Component text, int color) {
		super(width, -1, widthType, null, (poseStack, transform, context) -> {
			MultiLineLabel label = (MultiLineLabel) context;
			if (contentAlignment == Alignment.CENTER) {
				label.renderCentered(poseStack, transform.width / 2 + transform.left, transform.top, font.lineHeight, color);
			}
			else if (contentAlignment == Alignment.LEFT) {
				label.renderLeftAligned(poseStack, transform.left, transform.top, font.lineHeight, color);
			}
		});
		
		this.font = font;
		this.text = text;
	}
	
	private MultiLineLabel getLabel(GridTransform transform) {
		return label;
	}
	
	protected void create(GridTransform transform) {
		this.label = MultiLineLabel.create(font, text, transform.width);
	}
	
	@Override
	protected GridElement buildElementAt(int left, int top, int width, List<GridElement> collector) {
		create(new GridTransform(left, top, width, 0));
		int promptLines = this.label.getLineCount() + 1;
		int height = promptLines * 9;
		
		return new GridElement(left, top, width, height, this::getLabel, customRender);
	}
}
