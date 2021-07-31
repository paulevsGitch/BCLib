package ru.bclib.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
		final int height;
		final int width;
		final float alpha;
		final Component component;
		final Button.OnPress onPress;
		
		ButtonPos(float alpha, int width, int height, Component component, OnPress onPress) {
			this.height = height;
			this.width = width;
			this.alpha = alpha;
			this.component = component;
			this.onPress = onPress;
		}
	}
	public final int width;
	@NotNull
	private final Font font;
	private final Consumer<Button> addButtonFunction;
	public final int topStart;
	private int top;
	private int currentRowHeight = 0;
	
	final private List<LablePos> labels;
	final private List<ButtonPos> buttons;
	
	public GridLayout(int topStart, int width, Font font, Consumer<Button> addButtonFunction){
		Objects.requireNonNull(font);
		this.topStart = topStart;
		top = topStart + 20;
		this.width = width;
		this.font = font;
		this.addButtonFunction = addButtonFunction;
		labels = new ArrayList<>(4);
		buttons = new ArrayList<>(8);
	}
	
	public void addMessageRow(MultiLineLabel lb){
		final int LABEL_MARGIN_BOTTOM = 12;
		labels.add(new LablePos(lb, top));
		int promptLines = lb.getLineCount() + 1;
		int height = promptLines * 9;
		
		currentRowHeight = height + LABEL_MARGIN_BOTTOM;;
	}
	
	public void startRow(){
		this.endRow();
		top += currentRowHeight;
		currentRowHeight = 0;
	}
	
	public void endRow(){
		final int BUTTON_SPACING = 10;
		int count = buttons.size();
		int rowWidth = buttons.stream().map(b -> b.width).reduce(0, (p, c) -> p+c) + (count-1) * BUTTON_SPACING;
		int left = (width-rowWidth)/2;
		
		for (ButtonPos bp:buttons){
			Button customButton = new Button(left, top, bp.width, bp.height, bp.component, bp.onPress);
			customButton.setAlpha(bp.alpha);
			addButtonFunction.accept(customButton);
			
			left += BUTTON_SPACING + bp.width;
		};
		buttons.clear();
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
		final int BUTTON_MARGIN_BOTTOM = 6;
		
		currentRowHeight = Math.max(currentRowHeight, height + BUTTON_MARGIN_BOTTOM);
		buttons.add(new ButtonPos(alpha, width, height, component, onPress));
	}
	
	public void render(PoseStack poseStack){
		labels.forEach(lp -> {
			lp.label.renderCentered(poseStack, this.width / 2, lp.top);
		});
	}
}
