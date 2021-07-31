package ru.bclib.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GridLayout {
	class LablePos {
		final MultiLineLabel label;
		final int top;
		
		LablePos(MultiLineLabel label, int top){
			this.label = label;
			this.top = top;
		}
	}
	
	class ButtonPos {
		final int top;
		final int height;
		final int width;
		final float alpha;
		final Component component;
		final Button.OnPress onPress;
		
		ButtonPos(float alpha, int top, int width, int height, Component component, OnPress onPress) {
			this.height = height;
			this.width = width;
			this.top = top;
			this.alpha = alpha;
			this.component = component;
			this.onPress = onPress;
		}
	}
	public final int width;
	public final int height;
	@NotNull
	private final Font font;
	private final Consumer<Button> addButtonFunction;
	private final int topStart;
	private int topOffset;
	private int top;
	private int currentRowHeight = 0;
	private int currentRowMargin = 6;
	private int lastRowMargin = 0;
	
	final private List<LablePos> labels;
	final private List<List<ButtonPos>> buttons;
	private List<ButtonPos> currentButtonRow;
	
	public GridLayout(int topStart, int width, int height, Font font, Consumer<Button> addButtonFunction){
		Objects.requireNonNull(font);
		this.topStart = topStart;
		top = topStart + 20;
		this.topOffset = 0;
		this.width = width;
		this.height = height;
		this.font = font;
		this.addButtonFunction = addButtonFunction;
		labels = new ArrayList<>(4);
		buttons = new ArrayList<>(4);
	}
	
	public int getTopStart(){
		return topStart + topOffset;
	}
	
	public void addMessageRow(Component text, int padding){
		addMessageRow(MultiLineLabel.create(this.font, text, this.width - 2*padding));
	}
	
	public void addMessageRow(MultiLineLabel lb){
		labels.add(new LablePos(lb, top));
		int promptLines = lb.getLineCount() + 1;
		int height = promptLines * 9;
		
		currentRowMargin = 12;
		currentRowHeight = height;
	}
	
	public void startRow(){
		this.endRow();
		this.currentButtonRow = new ArrayList<>(8);
		this.buttons.add(this.currentButtonRow);
		
	}
	
	public void endRow(){
		lastRowMargin = currentRowMargin;
		top += currentRowHeight + currentRowMargin;
		currentRowHeight = 0;
		currentRowMargin = 0;
	}
	
	public void recenterVertically(){
		int hg = (top - lastRowMargin) - topStart;
		int targetTop = (height - hg)/2;
		topOffset = targetTop - topStart;
	}
	
	void finalizeLayout(){
		final int BUTTON_SPACING = 10;
		for (List<ButtonPos> row : this.buttons) {
			int count = row.size();
			int rowWidth = row.stream()
										   .map(b -> b.width)
										   .reduce(0, (p, c) -> p + c) + (count - 1) * BUTTON_SPACING;
			int left = (width - rowWidth) / 2;
			
			for (ButtonPos bp : row) {
				Button customButton = new Button(left, bp.top+topOffset, bp.width, bp.height, bp.component, bp.onPress);
				customButton.setAlpha(bp.alpha);
				addButtonFunction.accept(customButton);
				
				left += BUTTON_SPACING + bp.width;
			}
		}
	}
	
	public void addButton(int width, int height, Component component, Button.OnPress onPress){
		addButton(1.0f, width, height, component, onPress);
	}
	
	public void addButton(int height, Component component, Button.OnPress onPress){
		addButton(1.0f, height, component, onPress);
	}
	
	public void addButton(float alpha, int height, Component component, Button.OnPress onPress){
		final int BUTTON_PADDING = 12;
		int width = font.width(component.getVisualOrderText()) + 2*BUTTON_PADDING;
		addButton(alpha, width, height, component, onPress);
	}
	
	public void addButton(float alpha, int width, int height, Component component, Button.OnPress onPress){
		currentRowHeight = Math.max(currentRowHeight, height);
		currentRowMargin = 6;
		currentButtonRow.add(new ButtonPos(alpha, top, width, height, component, onPress));
	}
	
	public void render(PoseStack poseStack){
		labels.forEach(lp -> {
			lp.label.renderCentered(poseStack, this.width / 2, lp.top + topOffset);
		});
	}
}
