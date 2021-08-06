package ru.bclib.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import ru.bclib.gui.GridScreen;


@Environment(EnvType.CLIENT)
public class ConfirmRestartScreen extends GridScreen {
    private final Component description;
    private final ConfirmRestartScreen.Listener listener;

    public ConfirmRestartScreen(ConfirmRestartScreen.Listener listener) {
        this(listener, null);
    }

    public ConfirmRestartScreen(ConfirmRestartScreen.Listener listener, Component message) {
        super(30, new TranslatableComponent("bclib.datafixer.confirmrestart.title"));

        this.description = message==null?new TranslatableComponent("bclib.datafixer.confirmrestart.message"):message;
        this.listener = listener;
    }

    protected void initLayout() {
        final int BUTTON_HEIGHT = 20;

        grid.addMessageRow(this.description, 25);

        grid.startRow();
        grid.addButton( BUTTON_HEIGHT, CommonComponents.GUI_PROCEED, (button) -> {
            listener.proceed();
        });

        grid.endRow();
        grid.recenterVertically();
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    public interface Listener {
        void proceed();
    }
}
