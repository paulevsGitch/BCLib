package ru.bclib.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.impl.biome.InternalBiomeData;
import net.fabricmc.fabric.impl.biome.WeightedBiomePicker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.ClimateParameters;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.Nullable;
import ru.bclib.interfaces.BiomeListProvider;
import ru.bclib.util.MHelper;
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.generator.BiomePicker;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BiomeAPI {
	/**
	 * Empty biome used as default value if requested biome doesn't exist or linked. Shouldn't be registered anywhere to prevent bugs.
	 * Have {@code Biomes.THE_VOID} as the reference biome.
	 */
	public static final BCLBiome EMPTY_BIOME = new BCLBiome(Biomes.THE_VOID.location(), BuiltinRegistries.BIOME.get(Biomes.THE_VOID), 1, 0);
	
	public static final BiomePicker NETHER_BIOME_PICKER = new BiomePicker();
	public static final BiomePicker END_LAND_BIOME_PICKER = new BiomePicker();
	public static final BiomePicker END_VOID_BIOME_PICKER = new BiomePicker();
	
	private static final Map<ResourceLocation, BCLBiome> ID_MAP = Maps.newHashMap();
	private static final Map<Biome, BCLBiome> CLIENT = Maps.newHashMap();
	private static Registry<Biome> biomeRegistry;
	
	public static final BCLBiome NETHER_WASTES_BIOME = registerNetherBiome(getFromRegistry(Biomes.NETHER_WASTES));
	public static final BCLBiome CRIMSON_FOREST_BIOME = registerNetherBiome(getFromRegistry(Biomes.CRIMSON_FOREST));
	public static final BCLBiome WARPED_FOREST_BIOME = registerNetherBiome(getFromRegistry(Biomes.WARPED_FOREST));
	public static final BCLBiome SOUL_SAND_VALLEY_BIOME = registerNetherBiome(getFromRegistry(Biomes.SOUL_SAND_VALLEY));
	public static final BCLBiome BASALT_DELTAS_BIOME = registerNetherBiome(getFromRegistry(Biomes.BASALT_DELTAS));
	
	public static final BCLBiome THE_END = registerEndLandBiome(getFromRegistry(Biomes.THE_END));
	public static final BCLBiome END_MIDLANDS = registerSubBiome(THE_END, getFromRegistry(Biomes.END_MIDLANDS), 0.5F);
	public static final BCLBiome END_HIGHLANDS = registerSubBiome(THE_END, getFromRegistry(Biomes.END_HIGHLANDS), 0.5F);
	
	public static final BCLBiome END_BARRENS = registerEndVoidBiome(getFromRegistry(new ResourceLocation("end_barrens")));
	public static final BCLBiome SMALL_END_ISLANDS = registerEndVoidBiome(getFromRegistry(new ResourceLocation("small_end_islands")));
	
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
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerBiome(BCLBiome biome) {
		if (BuiltinRegistries.BIOME.get(biome.getID()) == null) {
			Registry.register(BuiltinRegistries.BIOME, biome.getID(), biome.getBiome());
		}
		ID_MAP.put(biome.getID(), biome);
		return biome;
	}
	
	public static BCLBiome registerSubBiome(BCLBiome parent, BCLBiome subBiome) {
		registerBiome(subBiome);
		parent.addSubBiome(subBiome);
		return subBiome;
	}
	
	public static BCLBiome registerSubBiome(BCLBiome parent, Biome biome, float chance) {
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome).get();
		BCLBiome subBiome = new BCLBiome(key.location(), biome, 1, chance);
		return registerSubBiome(parent, subBiome);
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * After that biome will be added to BCLib Nether Biome Generator and into Fabric Biome API.
	 * @param biome {@link BCLBiome}
	 * @return {@link BCLBiome}
	 */
	public static void registerNetherBiome(BCLBiome biome) {
		registerBiome(biome);
		NETHER_BIOME_PICKER.addBiome(biome);
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
	 * After that biome will be added to BCLib Nether Biome Generator and into Fabric Biome API.
	 * @param biome {@link BCLBiome}
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerNetherBiome(Biome biome) {
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome).get();
		BCLBiome bclBiome = new BCLBiome(key.location(), biome, 1, 1);
		NETHER_BIOME_PICKER.addBiome(bclBiome);
		registerBiome(bclBiome);
		return bclBiome;
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a land biome (will generate only on islands).
	 * @param biome {@link BCLBiome}
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerEndLandBiome(BCLBiome biome) {
		registerBiome(biome);
		END_LAND_BIOME_PICKER.addBiome(biome);
		float weight = biome.getGenChance();
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).get();
		InternalBiomeData.addEndBiomeReplacement(Biomes.END_HIGHLANDS, key, weight);
		InternalBiomeData.addEndBiomeReplacement(Biomes.END_MIDLANDS, key, weight);
		return biome;
	}
	
	/**
	 * Register {@link BCLBiome} wrapper for {@link Biome}.
	 * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a land biome (will generate only on islands).
	 * @param biome {@link BCLBiome}
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerEndLandBiome(Biome biome) {
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome).get();
		BCLBiome bclBiome = new BCLBiome(key.location(), biome, 1, 1);
		END_LAND_BIOME_PICKER.addBiome(bclBiome);
		registerBiome(bclBiome);
		return bclBiome;
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a void biome (will generate only in the End void - between islands).
	 * @param biome {@link BCLBiome}
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerEndVoidBiome(BCLBiome biome) {
		registerBiome(biome);
		END_VOID_BIOME_PICKER.addBiome(biome);
		float weight = biome.getGenChance();
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).get();
		InternalBiomeData.addEndBiomeReplacement(Biomes.SMALL_END_ISLANDS, key, weight);
		return biome;
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a void biome (will generate only in the End void - between islands).
	 * @param biome {@link BCLBiome}
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerEndVoidBiome(Biome biome) {
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome).get();
		BCLBiome bclBiome = new BCLBiome(key.location(), biome, 1, 1);
		END_VOID_BIOME_PICKER.addBiome(bclBiome);
		registerBiome(bclBiome);
		return bclBiome;
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
			random.nextFloat()
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
	
	/** Use inner biome field instead.
	 * Get actual {@link Biome} from given {@link BCLBiome}. If it is null it will request it from current {@link Registry}.
	 *
	 * @param biome - {@link BCLBiome}.
	 * @return {@link Biome}.
	 */
	@Deprecated(forRemoval = true)
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
	
	/**
	 * Load biomes from Fabric API. For internal usage only.
	 */
	public static void loadFabricAPIBiomes() {
		List<ResourceKey<Biome>> biomes = Lists.newArrayList();
		biomes.addAll(getBiomes(InternalBiomeData.getEndBiomesMap().get(Biomes.SMALL_END_ISLANDS)));
		biomes.addAll(getBiomes(InternalBiomeData.getEndBarrensMap().get(Biomes.END_BARRENS)));
		biomes.forEach((key) -> registerEndVoidBiome(BuiltinRegistries.BIOME.get(key.location())));
		
		biomes.clear();
		biomes.addAll(getBiomes(InternalBiomeData.getEndBiomesMap().get(Biomes.END_MIDLANDS)));
		biomes.addAll(getBiomes(InternalBiomeData.getEndBiomesMap().get(Biomes.END_HIGHLANDS)));
		biomes.forEach((key) -> registerEndLandBiome(BuiltinRegistries.BIOME.get(key.location())));
	}
	
	private static List<ResourceKey<Biome>> getBiomes(WeightedBiomePicker picker) {
		BiomeListProvider biomeList = (BiomeListProvider) (Object) picker;
		return biomeList == null ? Collections.emptyList() : biomeList.getBiomes();
	}
	
	@Nullable
	public static Biome getFromRegistry(ResourceLocation key) {
		return BuiltinRegistries.BIOME.get(key);
	}
	
	@Nullable
	public static Biome getFromRegistry(ResourceKey<Biome> key) {
		return BuiltinRegistries.BIOME.get(key);
	}
	
	public static boolean isDatapackBiome(ResourceLocation biomeID) {
		return getFromRegistry(biomeID) == null;
	}
}
