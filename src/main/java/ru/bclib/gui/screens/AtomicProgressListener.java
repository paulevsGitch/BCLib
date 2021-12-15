package ru.bclib.gui.screens;

import net.minecraft.network.chat.Component;

public interface AtomicProgressListener {
	public void incAtomic(int maxProgress);
	public void resetAtomic();
	public void stop();
	public void progressStage(Component component);
}
