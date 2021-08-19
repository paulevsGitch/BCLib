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
    public SyncFilesScreen(SyncFilesScreen.Listener listener) {
        super(new TranslatableComponent("title.bclib.syncfiles"));

        this.description = new TranslatableComponent("message.bclib.syncfiles");
        this.listener = listener;
    }

    protected void initLayout() {
        final int BUTTON_HEIGHT = 20;

        grid.addRow().addMessage(this.description, this.font, Alignment.CENTER);

        grid.addSpacerRow(10);
        
        GridRow row = grid.addRow();
        GridCheckboxCell mods = row.addCheckbox(new TranslatableComponent("message.bclib.syncfiles.mods"), true, BUTTON_HEIGHT, this.font);
    
        grid.addSpacerRow();
        row = grid.addRow();
        GridCheckboxCell files = row.addCheckbox(new TranslatableComponent("message.bclib.syncfiles.files"), true, BUTTON_HEIGHT, this.font);
    
        grid.addSpacerRow();
        row = grid.addRow();
        GridCheckboxCell folder = row.addCheckbox(new TranslatableComponent("message.bclib.syncfiles.folder"), true, BUTTON_HEIGHT, this.font);
        
        grid.addSpacerRow(30);
        
        row = grid.addRow();
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
