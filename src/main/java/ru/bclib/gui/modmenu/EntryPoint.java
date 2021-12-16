package ru.bclib.gui.modmenu;

import ru.bclib.integration.modmenu.ModMenuIntegration;

@Deprecated()
public class EntryPoint extends ModMenuIntegration {
	public static final Object entrypointObject = createEntrypoint(new EntryPoint());
	
	public EntryPoint() {
		super(MainScreen::new);
	}
}
