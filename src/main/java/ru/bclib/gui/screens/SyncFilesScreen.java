package ru.bclib.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import ru.bclib.gui.gridlayout.GridCheckboxCell;
import ru.bclib.gui.gridlayout.GridLayout.Alignment;
import ru.bclib.gui.gridlayout.GridRow;

@Environment(EnvType.CLIENT)
public class SyncFilesScreen extends BCLibScreen {
	private final Component description;
	private final SyncFilesScreen.Listener listener;
	private final boolean hasConfigFiles;
	private final boolean hasFiles;
	private final boolean hasMods;
	private final boolean shouldDelete;
	
	public SyncFilesScreen(int modFiles, int configFiles, int singleFiles, int folderFiles, int deleteFiles, Listener listener) {
		super(new TranslatableComponent("title.bclib.syncfiles"));
		
		this.description = new TranslatableComponent("message.bclib.syncfiles");
		this.listener = listener;
		
		this.hasConfigFiles = configFiles>0;
		this.hasFiles = singleFiles+folderFiles>0;
		this.hasMods = modFiles>0;
		this.shouldDelete = deleteFiles>0;
	}
	
	protected void initLayout() {
		final int BUTTON_HEIGHT = 20;
		
		grid.addRow()
			.addMessage(this.description, this.font, Alignment.CENTER);
		
		grid.addSpacerRow(10);
		
		GridRow row;
		
		
		final GridCheckboxCell mods;
		if (hasMods) {
			row = grid.addRow();
			mods = row.addCheckbox(new TranslatableComponent("message.bclib.syncfiles.mods"), true, BUTTON_HEIGHT, this.font);
			grid.addSpacerRow();
		} else mods=null;
		
		final GridCheckboxCell configs;
		if (hasConfigFiles) {
			row = grid.addRow();
			configs = row.addCheckbox(new TranslatableComponent("message.bclib.syncfiles.configs"), true, BUTTON_HEIGHT, this.font);
			grid.addSpacerRow();
		} else configs = null;
		
		row = grid.addRow();
		
		final GridCheckboxCell folder;
		if (hasFiles) {
			folder = row.addCheckbox(new TranslatableComponent("message.bclib.syncfiles.folders"), true, BUTTON_HEIGHT, this.font);
			row.addSpacer();
		} else folder = null;
		
		GridCheckboxCell delete;
		if (shouldDelete) {
			delete = row.addCheckbox(new TranslatableComponent("message.bclib.syncfiles.delete"), true, BUTTON_HEIGHT, this.font);
		} else delete = null;
		
		grid.addSpacerRow(30);
		
		row = grid.addRow();
		row.addFiller();
		row.addButton(CommonComponents.GUI_NO, BUTTON_HEIGHT, this.font, (button) -> {
			listener.proceed(false, false, false, false);
		});
		row.addSpacer();
		row.addButton(CommonComponents.GUI_YES, BUTTON_HEIGHT, this.font, (button) -> {
			listener.proceed(
				mods!=null && mods.isChecked(),
				configs!=null && configs.isChecked(),
				folder != null && folder.isChecked(),
				delete !=null && delete.isChecked()
			);
		});
		row.addFiller();
	}
	
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Environment(EnvType.CLIENT)
	public interface Listener {
		void proceed(boolean downloadMods, boolean downloadConfigs, boolean downloadFiles, boolean removeFiles);
	}
}
