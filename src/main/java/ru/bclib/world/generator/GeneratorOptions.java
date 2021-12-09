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
	private static boolean customNetherBiomeSource = true;
	private static boolean customEndBiomeSource = true;
	private static boolean addNetherBiomesByCategory = false;
	private static boolean addEndBiomesByCategory = false;
	private static boolean useOldBiomeGenerator = false;
	private static long farEndBiomesSqr = 1000000;
	
	public static void init() {
		biomeSizeNether = Configs.GENERATOR_CONFIG.getInt("nether.biomeMap", "biomeSize", 256);
		biomeSizeEndLand = Configs.GENERATOR_CONFIG.getInt("end.biomeMap", "biomeSizeLand", 256);
		biomeSizeEndVoid = Configs.GENERATOR_CONFIG.getInt("end.biomeMap", "biomeSizeVoid", 256);
		customNetherBiomeSource = Configs.GENERATOR_CONFIG.getBoolean("options", "customNetherBiomeSource", true);
		customEndBiomeSource = Configs.GENERATOR_CONFIG.getBoolean("options", "customEndBiomeSource", true);
		addNetherBiomesByCategory = Configs.GENERATOR_CONFIG.getBoolean("options", "addNetherBiomesByCategory", false);
		addEndBiomesByCategory = Configs.GENERATOR_CONFIG.getBoolean("options", "addEndBiomesByCategory", false);
		useOldBiomeGenerator = Configs.GENERATOR_CONFIG.getBoolean("options", "useOldBiomeGenerator", false);
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
	
	public static long getFarEndBiomes() {
		return farEndBiomesSqr;
	}
	
	/**
	 * Set distance of far End biomes generation, in blocks
	 * @param distance
	 */
	public static void setFarEndBiomes(int distance) {
		GeneratorOptions.farEndBiomesSqr = (long) distance * (long) distance;
	}
	
	public static boolean customNetherBiomeSource() {
		return customNetherBiomeSource;
	}
	
	public static boolean customEndBiomeSource() {
		return customEndBiomeSource;
	}
	
	public static boolean addNetherBiomesByCategory() {
		return addNetherBiomesByCategory;
	}
	
	public static boolean addEndBiomesByCategory() {
		return addEndBiomesByCategory;
	}
	
	public static boolean useOldBiomeGenerator() {
		return useOldBiomeGenerator;
	}
}
