package ru.bclib.world.features;

import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import ru.bclib.BCLib;

import java.lang.reflect.Field;

public class BCLDecorators {
	public static final ConfiguredDecorator<?> HEIGHTMAP_SQUARE;
	
	private static final ConfiguredDecorator<?> getDecorator(Field[] fields, int index) {
		try {
			return (ConfiguredDecorator<?>) fields[index].get(null);
		}
		catch (IllegalAccessException e) {
			BCLib.LOGGER.error(e.getLocalizedMessage());
			return null;
		}
	}
	
	static {
		Class<?>[] classes = Features.class.getDeclaredClasses();
		Field[] fields = classes[1].getDeclaredFields(); // Decorators class
		HEIGHTMAP_SQUARE = getDecorator(fields, 17);
	}
}
