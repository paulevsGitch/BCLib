package ru.bclib.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ru.bclib.gui.modmenu.MainScreen;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


/**
 * Internal class to hook into ModMenu, you should not need to use this class. If you want to register a
 * ModMenu Screen for a Mod using BCLib, use {@link ModMenu#addModMenuScreen(String, Function)}
 */
public class ModMenuEntryPoint implements ModMenuApi {
	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		Map<String, ConfigScreenFactory<?>> copy = new HashMap<>();
		for (var entry : ModMenu.screen.entrySet() ){
			copy.put(entry.getKey(), (parent)->entry.getValue().apply(parent));
		}
		return copy;
	}
	
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (parent)->new MainScreen(parent);
	}
	
	
}
