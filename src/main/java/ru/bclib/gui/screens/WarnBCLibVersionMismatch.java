package ru.bclib.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import ru.bclib.gui.GridScreen;

@Environment(EnvType.CLIENT)
public class WarnBCLibVersionMismatch extends GridScreen {
	private final Component description;
	private final Listener listener;
	public WarnBCLibVersionMismatch(Listener listener) {
		super(30, new TranslatableComponent("bclib.datafixer.bclibmissmatch.title"));
		
		this.description = new TranslatableComponent("bclib.datafixer.bclibmissmatch.message");
		this.listener = listener;
	}
	
	protected void initLayout() {
		final int BUTTON_HEIGHT = 20;
		
		grid.addMessageRow(this.description, 25);
		
		grid.startRow();
		grid.addButton( BUTTON_HEIGHT, CommonComponents.GUI_NO, (button) -> {
			listener.proceed(false);
		});
		grid.addButton( BUTTON_HEIGHT, CommonComponents.GUI_YES, (button) -> {
			listener.proceed(true);
		});
		
		grid.endRow();
		grid.recenterVertically();
	}
	
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Environment(EnvType.CLIENT)
	public interface Listener {
		void proceed(boolean download);
	}
}
