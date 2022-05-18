package org.betterx.bclib.gui.screens;

import net.minecraft.network.chat.Component;

public interface AtomicProgressListener {
    void incAtomic(int maxProgress);
    void resetAtomic();
    void stop();
    void progressStage(Component component);
}
