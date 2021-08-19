package ru.bclib.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import ru.bclib.gui.gridlayout.GridLayout.Alignment;
import ru.bclib.gui.gridlayout.GridRow;


@Environment(EnvType.CLIENT)
public class ConfirmRestartScreen extends BCLibScreen {
    private final Component description;
    private final ConfirmRestartScreen.Listener listener;

    public ConfirmRestartScreen(ConfirmRestartScreen.Listener listener) {
        this(listener, null);
    }

    public ConfirmRestartScreen(ConfirmRestartScreen.Listener listener, Component message) {
        super(new TranslatableComponent("title.bclib.confirmrestart"));

        this.description = message==null?new TranslatableComponent("message.bclib.confirmrestart"):message;
        this.listener = listener;
    }

    protected void initLayout() {
        final int BUTTON_HEIGHT = 20;
    
        grid.addRow().addMessage(this.description, this.font, Alignment.CENTER);
        
        grid.addSpacerRow();
        
        GridRow row = grid.addRow();
        row.addFiller();
        row.addButton(CommonComponents.GUI_PROCEED, BUTTON_HEIGHT, (button) -> {
            listener.proceed();
        });
        row.addFiller();
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    public interface Listener {
        void proceed();
    }
}
