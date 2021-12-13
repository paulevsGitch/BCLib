package ru.bclib.gui.modmenu;

import com.terraformersmc.modmenu.api.ModMenuApi;
import ru.bclib.integration.ModMenuIntegration;

public class EntryPoint extends ModMenuIntegration {
	public static final ModMenuApi entrypointObject = (ModMenuApi) createEntrypoint(new EntryPoint());
	
	public EntryPoint() {
		super(MainScreen::new);
	}
}
