package ru.bclib.api;

import com.google.common.collect.Lists;
import net.minecraft.core.Registry;
import ru.bclib.interfaces.PostInitable;

import java.util.List;
import java.util.function.Consumer;

public class PostInitAPI {
	private static List<Consumer<Void>> postInitFunctions = Lists.newArrayList();
	
	public static void register(Consumer<Void> function) {
		postInitFunctions.add(function);
	}
	
	public static void postInit() {
		if (postInitFunctions == null) {
			return;
		}
		postInitFunctions.forEach(function -> function.accept(null));
		Registry.BLOCK.forEach(block -> {
			if (block instanceof PostInitable) {
				((PostInitable) block).postInit();
			}
		});
		postInitFunctions = null;
	}
}
