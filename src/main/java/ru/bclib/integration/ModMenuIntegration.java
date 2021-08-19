package ru.bclib.integration;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.util.ModMenuApiMarker;
import net.minecraft.client.gui.screens.Screen;
import ru.bclib.gui.screens.ModMenu.MainScreen;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

@FunctionalInterface
interface IModMenuScreenFactory<S extends Screen>  {
	S create(Screen parent);
}

class ModMenuScreenFactory {
	static class ScreenFactoryInvocationHandler implements InvocationHandler {
		private final IModMenuScreenFactory act;
		
		public ScreenFactoryInvocationHandler(IModMenuScreenFactory act) {
			this.act = act;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return act.create((Screen)args[0]);
		}
	}
	
	public static IModMenuScreenFactory create(IModMenuScreenFactory act) {
		Class<?> iConfigScreenFactory = null;
		try {
			iConfigScreenFactory = Class.forName("com.terraformersmc.modmenu.api.ConfigScreenFactory");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		Object o = Proxy.newProxyInstance(
			ModMenuIntegration.class.getClassLoader(),
			new Class[] {iConfigScreenFactory, IModMenuScreenFactory.class},
			new ScreenFactoryInvocationHandler(act));
		
		return (IModMenuScreenFactory)o;
	}
}

public class ModMenuIntegration {
	static class BCLibModMenuInvocationHandler implements InvocationHandler {
		private final ModMenuIntegration target;
		
		public BCLibModMenuInvocationHandler(ModMenuIntegration target) {
			this.target = target;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ("getModConfigScreenFactory".equals(method.getName())){
				return target.getModConfigScreenFactory();
			} else if ("getProvidedConfigScreenFactories".equals(method.getName())){
				return target.getProvidedConfigScreenFactories();
			} else if ("toString".equals(method.getName())){
				return target.toString();
			} else {
				return null;
			}
		}
	}
	
	public static final ModMenuApiMarker entrypointObject = create();
	
	public static ModMenuApiMarker create() {
		Class<?> iModMenuAPI = null;
		//Class<?> iModMenuAPIMarker = null;
		try {
			iModMenuAPI = Class.forName("com.terraformersmc.modmenu.api.ModMenuApi");
			//iModMenuAPIMarker = Class.forName("com.terraformersmc.modmenu.util.ModMenuApiMarker");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			return (ModMenuApiMarker)new Object();
		}
		
		Object o = Proxy.newProxyInstance(
			ModMenuIntegration.class.getClassLoader(),
			new Class[] {iModMenuAPI},
			new BCLibModMenuInvocationHandler(new ModMenuIntegration()));
		
		return (ModMenuApiMarker)o;
	}

	IModMenuScreenFactory<?> getModConfigScreenFactory() {
		return ModMenuScreenFactory.create( MainScreen::new );
	}

	Map<String, IModMenuScreenFactory<?>> getProvidedConfigScreenFactories() {
		return ImmutableMap.of();
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}
