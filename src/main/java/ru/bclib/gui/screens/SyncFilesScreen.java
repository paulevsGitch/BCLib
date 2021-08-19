package ru.bclib.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import ru.bclib.gui.gridlayout.GridLayout.Alignment;
import ru.bclib.gui.gridlayout.GridRow;
import ru.bclib.gui.gridlayout.GridScreen;

@Environment(EnvType.CLIENT)
public class SyncFilesScreen extends GridScreen {
    private final Component description;
    private final SyncFilesScreen.Listener listener;
    public SyncFilesScreen(SyncFilesScreen.Listener listener) {
        super(new TranslatableComponent("bclib.datafixer.syncfiles.title"));

        this.description = new TranslatableComponent("bclib.datafixer.syncfiles.message");
        this.listener = listener;
    }

    protected void initLayout() {
        final int BUTTON_HEIGHT = 20;

        grid.addRow().addMessage(this.description, this.font, Alignment.CENTER);
        //grid.addMessageRow(this.description, 25);

        grid.addSpacerRow();
        
        GridRow row = grid.addRow();
        row.addFiller();
        row.addButton(CommonComponents.GUI_NO, BUTTON_HEIGHT, this.font, (button) -> {
            listener.proceed(false);
        });
        row.addSpacer();
        row.addButton(CommonComponents.GUI_YES, BUTTON_HEIGHT, this.font, (button) -> {
            listener.proceed(true);
        });
        row.addFiller();
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    public interface Listener {
        void proceed(boolean download);
    }
}
