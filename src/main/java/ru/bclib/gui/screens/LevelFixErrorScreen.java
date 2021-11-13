package ru.bclib.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import ru.bclib.gui.gridlayout.GridColumn;
import ru.bclib.gui.gridlayout.GridLayout;
import ru.bclib.gui.gridlayout.GridRow;

@Environment(EnvType.CLIENT)
public class LevelFixErrorScreen  extends BCLibScreen {
    private final String[] errors;
    final Listener onContinue;
    
    public LevelFixErrorScreen(Screen parent, String[] errors, Listener onContinue) {
        super(parent, new TranslatableComponent("title.bclib.datafixer.error"), 10, true);
        this.errors = errors;
        this.onContinue = onContinue;
    }
    
    @Override
    protected void initLayout() {
        grid.addSpacerRow();
        grid.addRow().addMessage(new TranslatableComponent("message.bclib.datafixer.error"), font, GridLayout.Alignment.CENTER);
        grid.addSpacerRow(8);
    
        GridRow row = grid.addRow();
        row.addSpacer(10);
        GridColumn col = row.addColumn(300, GridLayout.GridValueType.CONSTANT);
        for (String error : errors){
            TextComponent dash = new TextComponent("-");
            row = col.addRow();
            row.addString(dash, this);
    
            row.addSpacer(4);
            row.addString(new TextComponent(error), this);
        }
    
        grid.addSpacerRow(8);
        row = grid.addRow();
        row.addFiller();
        row.addButton(new TranslatableComponent("title.bclib.datafixer.error.continue"), 0.5f, 20, font, (n)-> {
            onClose();
            onContinue.doContinue(true);
        });
        row.addSpacer();
        row.addButton(CommonComponents.GUI_CANCEL, 20, font, (n)-> {
            this.minecraft.setScreen(null);
        });
        row.addFiller();
    }
    
    @Environment(EnvType.CLIENT)
    public interface Listener {
        void doContinue(boolean markFixed);
    }
}
