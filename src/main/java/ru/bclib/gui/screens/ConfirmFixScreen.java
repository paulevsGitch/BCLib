package ru.bclib.gui.screens;


	import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import ru.bclib.BCLib;

import java.util.Objects;
class ColoredButton extends Button {
	private final float r;
	private final float g;
	private final float b;
	public ColoredButton(int x, int y, int width, int height, Component component, OnPress onPress, float r, float g, float b) {
		super(x, y, width, height, component, onPress);
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	@Override
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		RenderSystem.setShaderColor(r, g, b, this.alpha);
		int k = this.getYImage(this.isHovered());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		this.blit(poseStack, this.x, this.y, 0, 46 + k * 20, this.width / 2, this.height);
		this.blit(poseStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
		this.renderBg(poseStack, minecraft, i, j);
		int l = this.active ? 16777215 : 10526880;
		drawCenteredString(poseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, l | Mth.ceil(this.alpha * 255.0F) << 24);
	}
}

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
	
	public ConfirmFixScreen(@Nullable Screen screen, BackupConfirmScreen.Listener listener) {
		super(new TranslatableComponent("bclib.datafixer.backupWarning.title"));
		this.message = MultiLineLabel.EMPTY;
		this.lastScreen = screen;
		this.listener = listener;
		this.description = new TranslatableComponent("bclib.datafixer.backupWarning.message");
	}
	
	protected void init() {
		super.init();
		this.message = MultiLineLabel.create(this.font, this.description, this.width - 50);
		int promptLines = this.message.getLineCount() + 1;
		Objects.requireNonNull(this.font);
		int height = promptLines * 9;
		final int BUTTON_WIDTH = 150;
		final int BUTTON_SPACE = 10;
		final int BUTTON_HEIGHT = 20;
		
		Button customButton = new Button((this.width -BUTTON_WIDTH)/2, 100 + height, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslatableComponent("bclib.datafixer.backupWarning.backup"), (button) -> {
			this.listener.proceed(true, true);
		});
		this.addRenderableWidget(customButton);
		
		
		customButton = new Button((this.width - 2*BUTTON_WIDTH - BUTTON_SPACE)/ 2  , 124 + height, BUTTON_WIDTH, BUTTON_HEIGHT, CommonComponents.GUI_CANCEL, (button) -> {
			this.minecraft.setScreen(this.lastScreen);
		});
		this.addRenderableWidget(customButton);
		
		customButton = new Button((this.width - 2*BUTTON_WIDTH - BUTTON_SPACE)/ 2 + BUTTON_WIDTH+BUTTON_SPACE, 124 + height, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslatableComponent("bclib.datafixer.backupWarning.continue"), (button) -> {
			this.listener.proceed(false, true);
		});
		customButton.setAlpha(0.5f);
		this.addRenderableWidget(customButton);
		
		
		customButton = new Button((this.width - BUTTON_WIDTH)/ 2, 148 + height, BUTTON_WIDTH, BUTTON_HEIGHT, new TranslatableComponent("bclib.datafixer.backupWarning.nofixes"), (button) -> {
			this.listener.proceed(false, false);
		});
		customButton.setAlpha(0.5f);
		this.addRenderableWidget(customButton);
	}
	
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
		this.message.renderCentered(poseStack, this.width / 2, 70);
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
