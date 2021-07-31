package ru.bclib.gui.screens;


import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import ru.bclib.BCLib;
import ru.bclib.gui.GridLayout;

@Environment(EnvType.CLIENT)
public class ConfirmFixScreen extends Screen {
	static final ResourceLocation BCLIB_LOGO_LOCATION = new ResourceLocation(BCLib.MOD_ID,
		"icon.png");
	@Nullable
	private final Screen lastScreen;
	protected final BackupConfirmScreen.Listener listener;
	private final Component description;
	private MultiLineLabel message;
	protected int id;
	private GridLayout grid = null;
	
	public ConfirmFixScreen(@Nullable Screen screen, BackupConfirmScreen.Listener listener) {
		super(new TranslatableComponent("bclib.datafixer.backupWarning.title"));
		this.message = MultiLineLabel.EMPTY;
		this.lastScreen = screen;
		this.listener = listener;
		
		this.description = new TranslatableComponent("bclib.datafixer.backupWarning.message");
	}
	
	protected void init() {
		super.init();
		this.grid = new GridLayout(30, this.width, this.font, this::addRenderableWidget);
		
		final int BUTTON_WIDTH = 150;
		final int BUTTON_SPACE = 10;
		final int BUTTON_HEIGHT = 20;
		
		grid.addMessageRow(MultiLineLabel.create(this.font, this.description, this.width - 50));
		
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
	
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, grid.width / 2, grid.topStart, 16777215);
		if (grid!=null) grid.render(poseStack);
		super.render(poseStack, i, j, f);
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
		void proceed(boolean bl, boolean bl2);
	}
}
