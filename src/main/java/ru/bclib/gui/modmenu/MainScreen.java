package ru.bclib.gui.modmenu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

import org.jetbrains.annotations.Nullable;
import ru.bclib.config.ConfigKeeper.BooleanEntry;
import ru.bclib.config.Configs;
import ru.bclib.config.NamedPathConfig;
import ru.bclib.config.NamedPathConfig.ConfigTokenDescription;
import ru.bclib.config.NamedPathConfig.DependendConfigToken;
import ru.bclib.gui.gridlayout.GridCheckboxCell;
import ru.bclib.gui.gridlayout.GridColumn;
import ru.bclib.gui.gridlayout.GridRow;
import ru.bclib.gui.gridlayout.GridScreen;
import ru.bclib.gui.gridlayout.GridWidgetWithEnabledState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MainScreen extends GridScreen{
	
	public MainScreen(@Nullable Screen parent) {
		super(parent, Component.translatable("title.bclib.modmenu.main"), 10, false);
	}
	
	protected <T> TranslatableComponent getComponent(NamedPathConfig config, ConfigTokenDescription<T> option, String type){
		return Component.translatable(type + ".config." + config.configID + option.getPath() );
	}
	
	Map<GridWidgetWithEnabledState, Supplier<Boolean>> dependentWidgets = new HashMap<>();
	protected void updateEnabledState(){
		dependentWidgets.forEach((cb, supl)->cb.setEnabled(supl.get()));
	}
	
	@SuppressWarnings("unchecked")
	protected <T>void addRow(GridColumn grid, NamedPathConfig config, ConfigTokenDescription<T> option){
		if (BooleanEntry.class.isAssignableFrom(option.token.type)) {
			addCheckbox(grid, config, (ConfigTokenDescription<Boolean>)option);
		}
		
		grid.addSpacerRow(2);
	}
	
	
	protected void addCheckbox(GridColumn grid, NamedPathConfig config, ConfigTokenDescription<Boolean> option){
		if (option.topPadding>0){
			grid.addSpacerRow(option.topPadding);
		}
		GridRow row = grid.addRow();
		if (option.leftPadding>0){
			row.addSpacer(option.leftPadding);
		}
		GridCheckboxCell cb = row.addCheckbox(getComponent(config, option, "title"), config.getRaw(option.token), font, (state)-> {
			config.set(option.token, state);
			updateEnabledState();
		});
		
		if (option.token instanceof DependendConfigToken) {
			dependentWidgets.put(cb, ()->option.token.dependenciesTrue(config));
			cb.setEnabled(option.token.dependenciesTrue(config));
		}
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Override
	protected void initLayout() {
		final int BUTTON_HEIGHT = 20;
		
		Configs.GENERATOR_CONFIG.getAllOptions().stream().filter(o -> !o.hidden).forEach(o -> addRow(grid, Configs.GENERATOR_CONFIG, o));
		grid.addSpacerRow(12);
		Configs.MAIN_CONFIG.getAllOptions().stream().filter(o -> !o.hidden).forEach(o -> addRow(grid, Configs.MAIN_CONFIG, o));
		grid.addSpacerRow(12);
		Configs.CLIENT_CONFIG.getAllOptions().stream().filter(o -> !o.hidden).forEach(o -> addRow(grid, Configs.CLIENT_CONFIG, o));
		
		grid.addSpacerRow(15);
		GridRow row = grid.addRow();
		row.addFiller();
		row.addButton(CommonComponents.GUI_DONE, BUTTON_HEIGHT, font, (button)->{
			Configs.CLIENT_CONFIG.saveChanges();
			Configs.GENERATOR_CONFIG.saveChanges();
			Configs.MAIN_CONFIG.saveChanges();
			onClose();
		});
		grid.addSpacerRow(10);
	}
}
