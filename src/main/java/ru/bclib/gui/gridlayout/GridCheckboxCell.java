package ru.bclib.gui.gridlayout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import ru.bclib.gui.gridlayout.GridLayout.GridValueType;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
class SignalingCheckBox extends Checkbox{
	private Consumer<Boolean> onChange;
	public SignalingCheckBox(int left, int top, int width, int height, Component component, boolean checked, Consumer<Boolean> onChange) {
		super(left, top, width, height, component, checked);
		this.onChange = onChange;
		if (onChange!=null)
			onChange.accept(checked);
	}
	
	@Override
	public void onPress() {
		super.onPress();
		if (onChange!=null)
			onChange.accept(this.selected());
	}
}

@Environment(EnvType.CLIENT)
public class GridCheckboxCell extends GridCell{
	private boolean checked;
	
	GridCheckboxCell(Component text, boolean checked, float alpha, double width, GridValueType widthType, double height) {
		this(text, checked, alpha, width, widthType, height, null);
	}
	
	GridCheckboxCell(Component text, boolean checked, float alpha, double width, GridValueType widthType, double height, Consumer<Boolean> onChange) {
		super(width, height, widthType, null, null);
		
		this.componentPlacer = (transform) -> {
			Checkbox cb = new SignalingCheckBox(transform.left, transform.top, transform.width, transform.height,
				text,
				checked,
				(state)-> {
					this.checked = state;
					if (onChange!=null) onChange.accept(state);
				}
			);
			cb.setAlpha(alpha);
			return cb;
		};
	}
	
	public boolean isChecked(){
		return checked;
	}
}
