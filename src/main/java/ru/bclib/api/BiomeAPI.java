package ru.bclib.api;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.biome.InternalBiomeData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.ClimateParameters;
import net.minecraft.world.level.biome.Biomes;
import ru.bclib.util.MHelper;
import ru.bclib.world.biomes.BCLBiome;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BiomeAPI {
	/**
	 * Empty biome used as default value if requested biome doesn't exist or linked. Shouldn't be registered anywhere to prevent bugs.
	 * Have {@code Biomes.THE_VOID} as the reference biome.
	 */
	public static final BCLBiome EMPTY_BIOME = new BCLBiome(Biomes.THE_VOID.location(), BuiltinRegistries.BIOME.get(Biomes.THE_VOID), 1, 0);
	
	private static final Set<BCLBiome> NETHER_BIOMES = Sets.newHashSet();
	private static final Set<BCLBiome> END_LAND_BIOMES = Sets.newHashSet();
	private static final Set<BCLBiome> END_VOID_BIOMES = Sets.newHashSet();
	
	private static final Map<ResourceLocation, BCLBiome> ID_MAP = Maps.newHashMap();
	private static final Map<Biome, BCLBiome> CLIENT = Maps.newHashMap();
	private static Registry<Biome> biomeRegistry;
	
	/**
	 * Initialize registry for current server.
	 *
	 * @param server - {@link MinecraftServer}
	 */
	public static void initRegistry(MinecraftServer server) {
		biomeRegistry = server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
		CLIENT.clear();
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * @param biome {@link BCLBiome}
	 */
	public static void registerBiome(BCLBiome biome) {
		if (BuiltinRegistries.BIOME.get(biome.getID()) == null) {
			Registry.register(BuiltinRegistries.BIOME, biome.getID(), biome.getBiome());
		}
		ID_MAP.put(biome.getID(), biome);
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * After that biome will be added to BCLib Nether Biome Generator and into Fabric Biome API.
	 * @param biome {@link BCLBiome}
	 */
	public static void registerNetherBiome(BCLBiome biome) {
		registerBiome(biome);
		Random random = new Random(biome.getID().hashCode());
		ClimateParameters parameters = new ClimateParameters(
			MHelper.randRange(-1.5F, 1.5F, random),
			MHelper.randRange(-1.5F, 1.5F, random),
			MHelper.randRange(-1.5F, 1.5F, random),
			MHelper.randRange(-1.5F, 1.5F, random),
			random.nextFloat()
		);
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).get();
		InternalBiomeData.addNetherBiome(key, parameters);
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a land biome (will generate only on islands).
	 * @param biome {@link BCLBiome}
	 */
	public static void registerEndLandBiome(BCLBiome biome) {
		registerBiome(biome);
		float weight = biome.getGenChance();
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).get();
		InternalBiomeData.addEndBiomeReplacement(Biomes.END_HIGHLANDS, key, weight);
		InternalBiomeData.addEndBiomeReplacement(Biomes.END_MIDLANDS, key, weight);
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a void biome (will generate only in the End void - between islands).
	 * @param biome {@link BCLBiome}
	 */
	public static void registerEndVoidBiome(BCLBiome biome) {
		registerBiome(biome);
		float weight = biome.getGenChance();
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).get();
		InternalBiomeData.addEndBiomeReplacement(Biomes.SMALL_END_ISLANDS, key, weight);
	}
	
	/**
	 * Adds {@link BCLBiome} to FabricAPI biomes as the Nether biome (with random {@link ClimateParameters}).
	 *
	 * @param biome - {@link BCLBiome}.
	 */
	@Deprecated(forRemoval = true)
	public static void addNetherBiomeToFabricApi(BCLBiome biome) {
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).get();
		Random random = new Random(biome.getID().toString().hashCode());
		ClimateParameters parameters = new ClimateParameters(
			MHelper.randRange(-2F, 2F, random),
			MHelper.randRange(-2F, 2F, random),
			MHelper.randRange(-2F, 2F, random),
			MHelper.randRange(-2F, 2F, random),
			MHelper.randRange(-2F, 2F, random)
		);
		InternalBiomeData.addNetherBiome(key, parameters);
	}
	
	/**
	 * Use BiomeAPI.registerEndLandBiome instead of this.
	 * Adds {@link BCLBiome} to FabricAPI biomes as an End land biome (generating on islands).
	 *
	 * @param biome - {@link BCLBiome}.
	 */
	@Deprecated(forRemoval = true)
	public static void addEndLandBiomeToFabricApi(BCLBiome biome) {
		float weight = biome.getGenChance();
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).get();
		InternalBiomeData.addEndBiomeReplacement(Biomes.END_HIGHLANDS, key, weight);
		InternalBiomeData.addEndBiomeReplacement(Biomes.END_MIDLANDS, key, weight);
	}
	
	/**
	 * Use BiomeAPI.registerEndVoidBiome instead of this.
	 * Adds {@link BCLBiome} to FabricAPI biomes as an End void biome (generating between islands in the void).
	 *
	 * @param biome - {@link BCLBiome}.
	 */
	@Deprecated(forRemoval = true)
	public static void addEndVoidBiomeToFabricApi(BCLBiome biome) {
		float weight = biome.getGenChance();
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).get();
		InternalBiomeData.addEndBiomeReplacement(Biomes.SMALL_END_ISLANDS, key, weight);
	}
	
	/**
	 * Get {@link BCLBiome} from {@link Biome} instance on server. Used to convert world biomes to BCLBiomes.
	 *
	 * @param biome - {@link Biome} from world.
	 * @return {@link BCLBiome} or {@code BiomeAPI.EMPTY_BIOME}.
	 */
	public static BCLBiome getFromBiome(Biome biome) {
		if (biomeRegistry == null) {
			return EMPTY_BIOME;
		}
		return ID_MAP.getOrDefault(biomeRegistry.getKey(biome), EMPTY_BIOME);
	}
	
	/**
	 * Get {@link BCLBiome} from biome on client. Used in fog rendering.
	 *
	 * @param biome - {@link Biome} from client world.
	 * @return {@link BCLBiome} or {@code BiomeAPI.EMPTY_BIOME}.
	 */
	@Environment(EnvType.CLIENT)
	public static BCLBiome getRenderBiome(Biome biome) {
		BCLBiome endBiome = CLIENT.get(biome);
		if (endBiome == null) {
			Minecraft minecraft = Minecraft.getInstance();
			ResourceLocation id = minecraft.level.registryAccess()
												 .registryOrThrow(Registry.BIOME_REGISTRY)
												 .getKey(biome);
			endBiome = id == null ? EMPTY_BIOME : ID_MAP.getOrDefault(id, EMPTY_BIOME);
			CLIENT.put(biome, endBiome);
		}
		return endBiome;
	}
	
	/**
	 * Get biome {@link ResourceLocation} from given {@link Biome}.
	 *
	 * @param biome - {@link Biome} from server world.
	 * @return biome {@link ResourceLocation}.
	 */
	public static ResourceLocation getBiomeID(Biome biome) {
		ResourceLocation id = biomeRegistry.getKey(biome);
		return id == null ? EMPTY_BIOME.getID() : id;
	}
	
	/**
	 * Get {@link BCLBiome} from given {@link ResourceLocation}.
	 *
	 * @param biomeID - biome {@link ResourceLocation}.
	 * @return {@link BCLBiome} or {@code BiomeAPI.EMPTY_BIOME}.
	 */
	public static BCLBiome getBiome(ResourceLocation biomeID) {
		return ID_MAP.getOrDefault(biomeID, EMPTY_BIOME);
	}
	
	/**
	 * Get actual {@link Biome} from given {@link BCLBiome}. If it is null it will request it from current {@link Registry}.
	 *
	 * @param biome - {@link BCLBiome}.
	 * @return {@link Biome}.
	 */
	public static Biome getActualBiome(BCLBiome biome) {
		Biome actual = biome.getActualBiome();
		if (actual == null) {
			biome.updateActualBiomes(biomeRegistry);
			actual = biome.getActualBiome();
		}
		return actual;
	}
	
	/**
	 * Check if biome with {@link ResourceLocation} exists in API registry.
	 *
	 * @param biomeID - biome {@link ResourceLocation}.
	 * @return {@code true} if biome exists in API registry and {@code false} if not.
	 */
	public static boolean hasBiome(ResourceLocation biomeID) {
		return ID_MAP.containsKey(biomeID);
	}
}
