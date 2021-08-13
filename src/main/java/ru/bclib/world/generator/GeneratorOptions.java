package ru.bclib.world.generator;

import net.minecraft.util.Mth;
import ru.bclib.config.Configs;

import java.awt.Point;
import java.util.function.Function;

public class GeneratorOptions {
	private static int biomeSizeNether;
	private static int biomeSizeEndLand;
	private static int biomeSizeEndVoid;
	private static Function<Point, Boolean> endLandFunction;
	private static boolean farEndBiomes;
	
	public static void init() {
		biomeSizeNether = Configs.GENERATOR_CONFIG.getInt("nether.biomeMap", "biomeSize", 256);
		biomeSizeEndLand = Configs.GENERATOR_CONFIG.getInt("end.biomeMap", "biomeSizeLand", 256);
		biomeSizeEndVoid = Configs.GENERATOR_CONFIG.getInt("end.biomeMap", "biomeSizeVoid", 256);
	}
	
	public static int getBiomeSizeNether() {
		return Mth.clamp(biomeSizeNether, 1, 8192);
	}
	
	public static int getBiomeSizeEndLand() {
		return Mth.clamp(biomeSizeEndLand, 1, 8192);
	}
	
	public static int getBiomeSizeEndVoid() {
		return Mth.clamp(biomeSizeEndVoid, 1, 8192);
	}
	
	public static void setEndLandFunction(Function<Point, Boolean> endLandFunction) {
		GeneratorOptions.endLandFunction = endLandFunction;
	}
	
	public static Function<Point, Boolean> getEndLandFunction() {
		return endLandFunction;
	}
	
	public static boolean isFarEndBiomes() {
		return farEndBiomes;
	}
	
	public static void setFarEndBiomes(boolean farEndBiomes) {
		GeneratorOptions.farEndBiomes = farEndBiomes;
	}
}
