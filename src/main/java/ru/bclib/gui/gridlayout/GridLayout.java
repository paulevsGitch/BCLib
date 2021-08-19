package ru.bclib.gui.gridlayout;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import ru.bclib.gui.gridlayout.GridLayout.GridValueType;
import ru.bclib.util.TriConsumer;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;


@Environment(EnvType.CLIENT)
abstract class GridCellDefinition {
	public final float width;
	public final GridLayout.GridValueType widthType;
	
	public GridCellDefinition(double width, GridLayout.GridValueType widthType) {
		this.width = (float)width;
		this.widthType = widthType;
	}
	
	public final int calculateWidth(final int parentWidth){
		if (widthType == GridLayout.GridValueType.CONSTANT) {
			return (int) this.width;
		} else if (widthType == GridValueType.PERCENTAGE) {
			return (int) (this.width * parentWidth);
		} else {
			return 0;
		}
	}
	
	final GridElement buildElement(final int parentWidth, final int autoWidth, final float autoWidthSum, int left, final int top, final List<GridElement> collector) {
		final int width = widthType == GridValueType.AUTO?(int)((this.width/autoWidthSum)*autoWidth):calculateWidth(parentWidth);
		
		final GridElement el = buildElementAt(left, top, width, collector);
		if (collector!=null) {
			collector.add(el);
		}
		return el;
	}
	
	abstract protected GridElement buildElementAt(int left, int top, int width, final List<GridElement> collector);
}

@Environment(EnvType.CLIENT)
class GridElement extends GridTransform{
	final Function<GridTransform, Object> componentPlacer;
	final TriConsumer<PoseStack, GridTransform, Object> customRender;
	Object renderContext;
	
	GridElement(int left, int top, int width, int height, Function<GridTransform, Object> componentPlacer, TriConsumer<PoseStack, GridTransform, Object> customRender) {
		super(left, top, width, height);
		this.componentPlacer = componentPlacer;
		this.customRender = customRender;
	}
	
	GridElement(int left, int top, int width, int height) {
		this(left, top, width, height, null, null);
	}
	
	GridTransform transformWithPadding(int leftPadding, int topPadding){
		return new GridTransform(left + leftPadding, top +topPadding, width, height);
	}
}

@Environment(EnvType.CLIENT)
abstract class GridContainer extends GridCellDefinition{
	protected List<GridCellDefinition> cells;
	
	public GridContainer(double width) {
		this(width, GridLayout.GridValueType.CONSTANT);
	}
	
	GridContainer(double width, GridLayout.GridValueType widthType) {
		super(width, widthType);
		cells = new LinkedList<>();
	}
}

@Environment(EnvType.CLIENT)
public class GridLayout extends GridColumn {
	public static final int COLOR_WHITE = 0x00FFFFFF;
	public static final int COLOR_RED = 0x00FF0000;
	public static final int COLOR_GREEN = 0x0000FF00;
	public static final int COLOR_BLUE = 0x000000FF;
	
	public final GridScreen screen;
	public final int screenHeight;
	public final int sidePadding;
	public final int initialTopPadding;
	public final  boolean centerVertically;
	private int height;
	private int topPadding;
	
	private List<GridElement> elements;
	
	public GridLayout(GridScreen screen) {
		this(screen, 0, true);
	}
	
	public GridLayout(GridScreen screen, int topPadding, boolean centerVertically) {
		this(screen, topPadding, 20, centerVertically);
	}
	
	public GridLayout(GridScreen screen, int topPadding, int sidePadding, boolean centerVertically) {
		super(screen.width-2*sidePadding, GridValueType.CONSTANT);
		this.screen = screen;
		this.screenHeight = screen.height;
		height = 0;
		this.topPadding = topPadding;
		this.sidePadding = sidePadding;
		this.initialTopPadding = topPadding;
		this.centerVertically = centerVertically;
	}
	
	public int getHeight(){
		return height;
	}
	
	public int getTopPadding() {
		return topPadding;
	}
	
	void buildLayout(){
		elements = new LinkedList<>();
		GridElement el = this.buildElement((int)this.width, 0, 1, 0,0, elements);
		this.height = el.height;
		if (centerVertically) {
			topPadding = (screenHeight - el.height - initialTopPadding) >> 1;
		} else {
			topPadding = initialTopPadding;
		}
		
	}
	
	public void finalizeLayout(){
		buildLayout();
		
		elements
			.stream()
			.filter(element -> element.componentPlacer!=null)
			.forEach(element -> {
				Object context = element.componentPlacer.apply(element.transformWithPadding(sidePadding, topPadding));
				if (element.customRender != null){
					element.renderContext = context;
				} else if (context instanceof AbstractWidget) {
					screen.addRenderableWidget((AbstractWidget) context);
				}
			});
	}
	
	public void render(PoseStack poseStack){
		if (elements == null) return;
		elements
			.stream()
			.filter(element -> element.customRender!=null)
			.forEach(element -> element.customRender.accept(poseStack, element.transformWithPadding(sidePadding, topPadding), element.renderContext));
	}
	
	
	public static enum VerticalAlignment {
		TOP, CENTER, BOTTOM
	}
	
	public static enum Alignment {
		LEFT, CENTER, RIGHT
	}
	
	public static enum GridValueType {
		CONSTANT, PERCENTAGE, AUTO;
	}
}
