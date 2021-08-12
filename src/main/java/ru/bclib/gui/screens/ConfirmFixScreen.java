package ru.bclib.gui.screens;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import ru.bclib.BCLib;
import ru.bclib.gui.GridScreen;

@Environment(EnvType.CLIENT)
public class ConfirmFixScreen extends GridScreen {
	static final ResourceLocation BCLIB_LOGO_LOCATION = new ResourceLocation(BCLib.MOD_ID,
		"icon.png");
	@Nullable
	private final Screen lastScreen;
	protected final ConfirmFixScreen.Listener listener;
	private final Component description;
	protected int id;
	
	public ConfirmFixScreen(@Nullable Screen screen, ConfirmFixScreen.Listener listener) {
		super(30, new TranslatableComponent("bclib.datafixer.backupWarning.title"));
		this.lastScreen = screen;
		this.listener = listener;
		
		this.description = new TranslatableComponent("bclib.datafixer.backupWarning.message");
	}
	
	protected void initLayout() {
		final int BUTTON_HEIGHT = 20;
		
		grid.addMessageRow(this.description, 25);
		
		grid.startRow();
		grid.addButton( BUTTON_HEIGHT, new TranslatableComponent("bclib.datafixer.backupWarning.backup"), (button) -> {
			this.listener.proceed(true, true);
		});
		
		grid.startRow();
		grid.addButton( BUTTON_HEIGHT, CommonComponents.GUI_CANCEL, (button) -> {
			this.minecraft.setScreen(this.lastScreen);
		});
		grid.addButton(0.5f, BUTTON_HEIGHT, new TranslatableComponent("bclib.datafixer.backupWarning.continue"), (button) -> {
			this.listener.proceed(false, true);
		});
		
		grid.startRow();
		grid.addButton(0.5f, BUTTON_HEIGHT, new TranslatableComponent("bclib.datafixer.backupWarning.nofixes"), (button) -> {
			this.listener.proceed(false, false);
		});
		
		grid.endRow();
	}
	
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.minecraft.setScreen(this.lastScreen);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}
	
	@Environment(EnvType.CLIENT)
	public interface Listener {
		void proceed(boolean createBackup, boolean applyPatches);
	}
}
