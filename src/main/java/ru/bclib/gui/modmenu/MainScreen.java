package ru.bclib.gui.modmenu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;
import ru.bclib.config.Configs;
import ru.bclib.config.NamedPathConfig;
import ru.bclib.config.NamedPathConfig.ConfigToken;
import ru.bclib.config.NamedPathConfig.ConfigToken.Bool;
import ru.bclib.gui.gridlayout.GridColumn;
import ru.bclib.gui.gridlayout.GridRow;
import ru.bclib.gui.gridlayout.GridScreen;

public class MainScreen extends GridScreen{
	
	public MainScreen(@Nullable Screen parent) {
		super(parent, new TranslatableComponent("title.bclib.modmenu.main"));
	}
	
	protected TranslatableComponent getComponent(NamedPathConfig config, ConfigToken.Bool token, String type){
		StringBuilder path = new StringBuilder();
		for (String p : token.getPath()){
			path.append(".")
				.append(p);
			
		}
		path.append(".").append(token.getEntry());
		return new TranslatableComponent(type + ".config." + config.configID + path );
	}
	
	protected void addRow(GridColumn grid, NamedPathConfig config, ConfigToken token){
		if (token instanceof Bool){
			addRow(grid, config, (ConfigToken.Bool)token);
		}
		
		grid.addSpacerRow(2);
	}
	
	protected void addRow(GridColumn grid, NamedPathConfig config, ConfigToken.Bool token){
		GridRow row = grid.addRow();
		row.addCheckbox(getComponent(config, token, "title"), config.get(token), font, (state)-> config.set(token, state));
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Override
	protected void initLayout() {
		final int BUTTON_HEIGHT = 20;
		
		Configs.CLIENT_CONFIG.getAllOptions().forEach(o -> addRow(grid, Configs.CLIENT_CONFIG, o));
		
		grid.addSpacerRow(15);
		GridRow row = grid.addRow();
		row.addFiller();
		row.addButton(CommonComponents.GUI_DONE, BUTTON_HEIGHT, font, (button)->{
			Configs.CLIENT_CONFIG.saveChanges();
			onClose();
		});
	}
}
