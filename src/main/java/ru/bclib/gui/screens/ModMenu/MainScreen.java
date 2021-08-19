package ru.bclib.gui.screens.ModMenu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;
import ru.bclib.gui.gridlayout.GridRow;
import ru.bclib.gui.gridlayout.GridScreen;

public class MainScreen extends GridScreen{
	
	public MainScreen(@Nullable Screen parent) {
		super(parent, new TranslatableComponent("title.bclib.modmenu.main"));
	}
	
	@Override
	protected void initLayout() {
		final int BUTTON_HEIGHT = 20;
		grid.addSpacerRow(20);
		GridRow row = grid.addRow();
		
		row.addFiller();
		row.addButton(CommonComponents.GUI_BACK, BUTTON_HEIGHT, font, (button)->{
			onClose();
		});
		row.addFiller();
	}
}
