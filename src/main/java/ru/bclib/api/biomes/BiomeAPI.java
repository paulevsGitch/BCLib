package ru.bclib.api.biomes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.impl.biome.NetherBiomeData;
import net.fabricmc.fabric.impl.biome.TheEndBiomeData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.Nullable;
import ru.bclib.config.Configs;
import ru.bclib.util.MHelper;
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.biomes.FabricBiomesData;
import ru.bclib.world.features.BCLFeature;
import ru.bclib.world.generator.BiomePicker;
import ru.bclib.world.structures.BCLStructureFeature;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;

public class BiomeAPI {
	/**
	 * Empty biome used as default value if requested biome doesn't exist or linked. Shouldn't be registered anywhere to prevent bugs.
	 * Have {@code Biomes.THE_VOID} as the reference biome.
	 */
	public static final BCLBiome EMPTY_BIOME = new BCLBiome(Biomes.THE_VOID.location());
	
	public static final BiomePicker NETHER_BIOME_PICKER = new BiomePicker();
	public static final BiomePicker END_LAND_BIOME_PICKER = new BiomePicker();
	public static final BiomePicker END_VOID_BIOME_PICKER = new BiomePicker();
	
	private static final Map<ResourceLocation, BCLBiome> ID_MAP = Maps.newHashMap();
	private static final Map<Biome, BCLBiome> CLIENT = Maps.newHashMap();
	private static Registry<Biome> biomeRegistry;
	
	private static final Map<ResourceKey, List<BiConsumer<ResourceLocation, Biome>>> MODIFICATIONS = Maps.newHashMap();
	private static final Set<ResourceLocation> MODIFIED_BIOMES = Sets.newHashSet();
	
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
	
	public static BCLBiome registerSubBiome(BCLBiome parent, Biome biome, float genChance) {
		BCLBiome subBiome = new BCLBiome(biome).setGenChance(genChance);
		return registerSubBiome(parent, subBiome);
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * After that biome will be added to BCLib Nether Biome Generator and into Fabric Biome API.
	 * @param biome {@link BCLBiome}
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerNetherBiome(BCLBiome biome) {
		registerBiome(biome);
		NETHER_BIOME_PICKER.addBiome(biome);
		Random random = new Random(biome.getID().hashCode());
		
		//TODO: 1.18 Check parameters, depth was previously called altitude
		//temperature, humidity, continentalness, erosion, depth, weirdness, offset
        Climate.ParameterPoint parameters = Climate.parameters(
			MHelper.randRange(-1.5F, 1.5F, random),
			MHelper.randRange(-1.5F, 1.5F, random),
			MHelper.randRange(-1.5F, 1.5F, random), //new in 1.18
			MHelper.randRange(-1.5F, 1.5F, random), //new in 1.18
			MHelper.randRange(-1.5F, 1.5F, random),
			MHelper.randRange(-1.5F, 1.5F, random),
			random.nextFloat()
		);
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).get();
		NetherBiomeData.addNetherBiome(key, parameters);
		return biome;
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * After that biome will be added to BCLib Nether Biome Generator and into Fabric Biome API.
	 * @param biome {@link BCLBiome}
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerNetherBiome(Biome biome) {
		BCLBiome bclBiome = new BCLBiome(biome);
		configureBiome(bclBiome, 1.0F, 1.0F);
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
		configureBiome(biome, 1.0F, 1.0F);
		END_LAND_BIOME_PICKER.addBiome(biome);
		float weight = biome.getGenChance();
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).get();
		TheEndBiomeData.addEndBiomeReplacement(Biomes.END_HIGHLANDS, key, weight);
		TheEndBiomeData.addEndBiomeReplacement(Biomes.END_MIDLANDS, key, weight);
		return biome;
	}
	
	/**
	 * Register {@link BCLBiome} wrapper for {@link Biome}.
	 * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a land biome (will generate only on islands).
	 * @param biome {@link BCLBiome}
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerEndLandBiome(Biome biome) {
		BCLBiome bclBiome = new BCLBiome(biome);
		configureBiome(bclBiome, 1.0F, 1.0F);
		END_LAND_BIOME_PICKER.addBiome(bclBiome);
		registerBiome(bclBiome);
		return bclBiome;
	}
	
	/**
	 * Register {@link BCLBiome} wrapper for {@link Biome}.
	 * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a land biome (will generate only on islands).
	 * @param biome {@link BCLBiome};
	 * @param genChance float generation chance.
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerEndLandBiome(Biome biome, float genChance) {
		BCLBiome bclBiome = new BCLBiome(biome);
		configureBiome(bclBiome, genChance, 1.0F);
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
		configureBiome(biome, 1.0F, 1.0F);
		END_VOID_BIOME_PICKER.addBiome(biome);
		float weight = biome.getGenChance();
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).get();
		TheEndBiomeData.addEndBiomeReplacement(Biomes.SMALL_END_ISLANDS, key, weight);
		return biome;
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a void biome (will generate only in the End void - between islands).
	 * @param biome {@link BCLBiome}
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerEndVoidBiome(Biome biome) {
		BCLBiome bclBiome = new BCLBiome(biome);
		configureBiome(bclBiome, 1.0F, 1.0F);
		END_VOID_BIOME_PICKER.addBiome(bclBiome);
		registerBiome(bclBiome);
		return bclBiome;
	}
	
	/**
	 * Register {@link BCLBiome} instance and its {@link Biome} if necessary.
	 * After that biome will be added to BCLib End Biome Generator and into Fabric Biome API as a void biome (will generate only in the End void - between islands).
	 * @param biome {@link BCLBiome}.
	 * @param genChance float generation chance.
	 * @return {@link BCLBiome}
	 */
	public static BCLBiome registerEndVoidBiome(Biome biome, float genChance) {
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome).get();
		BCLBiome bclBiome = new BCLBiome(biome);
		configureBiome(bclBiome, genChance, 1.0F);
		END_VOID_BIOME_PICKER.addBiome(bclBiome);
		registerBiome(bclBiome);
		return bclBiome;
	}
	
	/**
	 * Get {@link BCLBiome} from {@link Biome} instance on server. Used to convert world biomes to BCLBiomes.
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
	 * @param biome - {@link Biome} from client world.
	 * @return {@link BCLBiome} or {@code BiomeAPI.EMPTY_BIOME}.
	 */
	@Environment(EnvType.CLIENT)
	public static BCLBiome getRenderBiome(Biome biome) {
		BCLBiome endBiome = CLIENT.get(biome);
		if (endBiome == null) {
			Minecraft minecraft = Minecraft.getInstance();
			ResourceLocation id = minecraft.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome);
			endBiome = id == null ? EMPTY_BIOME : ID_MAP.getOrDefault(id, EMPTY_BIOME);
			CLIENT.put(biome, endBiome);
		}
		return endBiome;
	}
	
	/**
	 * Get biome {@link ResourceLocation} from given {@link Biome}.
	 * @param biome - {@link Biome} from server world.
	 * @return biome {@link ResourceLocation}.
	 */
	public static ResourceLocation getBiomeID(Biome biome) {
		ResourceLocation id = biomeRegistry.getKey(biome);
		return id == null ? EMPTY_BIOME.getID() : id;
	}
	
	/**
	 * Get {@link BCLBiome} from given {@link ResourceLocation}.
	 * @param biomeID - biome {@link ResourceLocation}.
	 * @return {@link BCLBiome} or {@code BiomeAPI.EMPTY_BIOME}.
	 */
	public static BCLBiome getBiome(ResourceLocation biomeID) {
		return ID_MAP.getOrDefault(biomeID, EMPTY_BIOME);
	}
	
	/**
	 * Check if biome with {@link ResourceLocation} exists in API registry.
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
		FabricBiomesData.NETHER_BIOMES.forEach((key) -> {
			if (!hasBiome(key.location())) {
				registerNetherBiome(BuiltinRegistries.BIOME.get(key.location()));
			}
		});
		
		FabricBiomesData.END_LAND_BIOMES.forEach((key, weight) -> {
			if (!hasBiome(key.location())) {
				registerEndLandBiome(BuiltinRegistries.BIOME.get(key.location()), weight);
			}
		});
		
		FabricBiomesData.END_VOID_BIOMES.forEach((key, weight) -> {
			if (!hasBiome(key.location())) {
				registerEndVoidBiome(BuiltinRegistries.BIOME.get(key.location()), weight);
			}
		});
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
	
	public static boolean isNetherBiome(ResourceLocation biomeID) {
		return pickerHasBiome(NETHER_BIOME_PICKER, biomeID);
	}
	
	public static boolean isEndBiome(ResourceLocation biomeID) {
		return pickerHasBiome(END_LAND_BIOME_PICKER, biomeID) || pickerHasBiome(END_VOID_BIOME_PICKER, biomeID);
	}
	
	private static boolean pickerHasBiome(BiomePicker picker, ResourceLocation key) {
		return picker.getBiomes().stream().filter(biome -> biome.getID().equals(key)).findFirst().isPresent();
	}
	
	/**
	 * Registers new biome modification for specified dimension. Will work both for mod and datapack biomes.
	 * @param dimensionID {@link ResourceLocation} dimension ID, example: Level.OVERWORLD or "minecraft:overworld".
	 * @param modification {@link BiConsumer} with {@link ResourceKey} biome ID and {@link Biome} parameters.
	 */
	public static void registerBiomeModification(ResourceKey dimensionID, BiConsumer<ResourceLocation, Biome> modification) {
		List<BiConsumer<ResourceLocation, Biome>> modifications = MODIFICATIONS.get(dimensionID);
		if (modifications == null) {
			modifications = Lists.newArrayList();
			MODIFICATIONS.put(dimensionID, modifications);
		}
		modifications.add(modification);
	}
	
	/**
	 * Registers new biome modification for the Overworld. Will work both for mod and datapack biomes.
	 * @param modification {@link BiConsumer} with {@link ResourceLocation} biome ID and {@link Biome} parameters.
	 */
	public static void registerOverworldBiomeModification(BiConsumer<ResourceLocation, Biome> modification) {
		registerBiomeModification(Level.OVERWORLD, modification);
	}
	
	/**
	 * Registers new biome modification for the Nether. Will work both for mod and datapack biomes.
	 * @param modification {@link BiConsumer} with {@link ResourceLocation} biome ID and {@link Biome} parameters.
	 */
	public static void registerNetherBiomeModification(BiConsumer<ResourceLocation, Biome> modification) {
		registerBiomeModification(Level.NETHER, modification);
	}
	
	/**
	 * Registers new biome modification for the End. Will work both for mod and datapack biomes.
	 * @param modification {@link BiConsumer} with {@link ResourceLocation} biome ID and {@link Biome} parameters.
	 */
	public static void registerEndBiomeModification(BiConsumer<ResourceLocation, Biome> modification) {
		registerBiomeModification(Level.END, modification);
	}
	
	/**
	 * Will apply biome modifications to world, internal usage only.
	 * @param level
	 */
	public static void applyModifications(ServerLevel level) {
		List<BiConsumer<ResourceLocation, Biome>> modifications = MODIFICATIONS.get(level.dimension());
		if (modifications == null) {
			return;
		}
		BiomeSource source = level.getChunkSource().getGenerator().getBiomeSource();
		Set<Biome> biomes = source.possibleBiomes();
		
		biomes.forEach(biome -> {
			ResourceLocation biomeID =  getBiomeID(biome);
			boolean modify = isDatapackBiome(biomeID);
			if (biome != BuiltinRegistries.BIOME.get(biomeID)) {
				modify = true;
			}
			else if (!modify && !MODIFIED_BIOMES.contains(biomeID)) {
				MODIFIED_BIOMES.add(biomeID);
				modify = true;
			}
			if (modify) {
				modifications.forEach(consumer -> {
					consumer.accept(biomeID, biome);
				});
			}
		});
	}
	
	/**
	 * Adds new features to existing biome.
	 * @param biome {@link Biome} to add features in.
	 * @param feature {@link ConfiguredFeature} to add.
	 *
	 */
	public static void addBiomeFeature(Biome biome, BCLFeature feature) {
		addBiomeFeature(biome, feature.getPlacedFeature(), feature.getDecoration());
	}
	
	/**
	 * Adds new features to existing biome.
	 * @param biomeID {@link ResourceLocation} for the {@link Biome} to add features in.
	 * @param feature {@link ConfiguredFeature} to add.
	 *
	 */
	public static void addBiomeFeature(ResourceLocation biomeID, BCLFeature feature) {
		addBiomeFeature(biomeID, feature.getPlacedFeature(), feature.getDecoration());
	}
	
	/**
	 * Adds new features to existing biome.
	 * @param biome {@link Biome} to add features in.
	 * @param feature {@link ConfiguredFeature} to add.
	 * @param step a {@link Decoration} step for the feature.
	 */
	public static void addBiomeFeature(Biome biome, PlacedFeature feature, Decoration step) {
		addBiomeFeature(getBiomeID(biome), feature, step);
	}
	
	/**
	 * Adds new features to existing biome.
	 * @param biomeID {@link ResourceLocation} of the {@link Biome} to add features in.
	 * @param feature {@link ConfiguredFeature} to add.
	 * @param step a {@link Decoration} step for the feature.
	 */
	private static void addBiomeFeature(ResourceLocation biomeID, PlacedFeature feature, Decoration step) {
		BuiltinRegistries.PLACED_FEATURE
			.getResourceKey(feature)
			.ifPresent(key -> BiomeModifications.addFeature(ctx -> ctx.getBiomeKey().location().equals(biomeID), step, key));
	}
	
	/**
	 * Adds new features to existing biome.
	 * @param biome {@link Biome} to add features in.
	 * @param features array of {@link BCLFeature} to add.
	 */
	public static void addBiomeFeatures(Biome biome, BCLFeature... features) {
		for (BCLFeature feature: features) {
			addBiomeFeature(biome, feature.getPlacedFeature(), feature.getDecoration());
		}
	}
	
	/**
	 * Adds new structure feature to existing biome.
	 * @param biome {@link Biome} to add structure feature in.
	 * @param structure {@link ConfiguredStructureFeature} to add.
	 */
	public static void addBiomeStructure(Biome biome, ConfiguredStructureFeature structure) {
		addBiomeStructure(getBiomeID(biome), structure);
	}
	
	/**
	 * Adds new structure feature to existing biome.
	 * @param biomeID {@link ResourceLocation} of the {@link Biome} to add structure feature in.
	 * @param structure {@link ConfiguredStructureFeature} to add.
	 */
	public static void addBiomeStructure(ResourceLocation biomeID, ConfiguredStructureFeature structure) {
		BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE
			.getResourceKey(structure)
			.ifPresent(key -> BiomeModifications.addStructure(ctx -> ctx.getBiomeKey().location().equals(biomeID), key));
	}
	
	/**
	 * Adds new structure feature to existing biome.
	 * @param biome {@link Biome} to add structure feature in.
	 * @param structure {@link BCLStructureFeature} to add.
	 */
	public static void addBiomeStructure(Biome biome, BCLStructureFeature structure) {
		addBiomeStructure(biome, structure.getFeatureConfigured());
	}
	
	/**
	 * Adds new structure feature to existing biome.
	 * @param biomeID {@link ResourceLocation} of the {@link Biome} to add structure feature in.
	 * @param structure {@link BCLStructureFeature} to add.
	 */
	public static void addBiomeStructure(ResourceLocation biomeID, BCLStructureFeature structure) {
		addBiomeStructure(biomeID, structure.getFeatureConfigured());
	}

	/**
	 * Adds new structure features to existing biome.
	 * @param biome {@link Biome} to add structure features in.
	 * @param structures array of {@link BCLStructureFeature} to add.
	 */
	public static void addBiomeStructures(Biome biome, BCLStructureFeature... structures) {
		for (BCLStructureFeature structure: structures) {
			addBiomeStructure(biome, structure.getFeatureConfigured());
		}
	}
	
	/**
	 * Adds mob spawning to specified biome.
	 * @param biome {@link Biome} to add mob spawning.
	 * @param entityType {@link EntityType} mob type.
	 * @param weight spawn weight.
	 * @param minGroupCount minimum mobs in group.
	 * @param maxGroupCount maximum mobs in group.
	 */
	public static <M extends Mob> void addBiomeMobSpawn(Biome biome, EntityType<M> entityType, int weight, int minGroupCount, int maxGroupCount) {
		addBiomeMobSpawn(getBiomeID(biome), entityType, weight, minGroupCount, maxGroupCount);
	}
	
	/**
	 * Adds mob spawning to specified biome.
	 * @param biomeID {@link ResourceLocation} of the {@link Biome }to add mob spawning.
	 * @param entityType {@link EntityType} mob type.
	 * @param weight spawn weight.
	 * @param minGroupCount minimum mobs in group.
	 * @param maxGroupCount maximum mobs in group.
	 */
	public static <M extends Mob> void addBiomeMobSpawn(ResourceLocation biomeID, EntityType<M> entityType, int weight, int minGroupCount, int maxGroupCount) {
		BiomeModifications.addSpawn(
			ctx -> ctx.getBiomeKey().location().equals(biomeID),
			entityType.getCategory(), entityType, weight, minGroupCount, maxGroupCount
		);
	}
	
	private static void configureBiome(BCLBiome biome, float chance, float fog) {
		String group = biome.getID().getNamespace() + "." + biome.getID().getPath();
		chance = Configs.BIOMES_CONFIG.getFloat(group, "generation_chance", chance);
		fog = Configs.BIOMES_CONFIG.getFloat(group, "fog_density", fog);
		biome.setGenChance(chance).setFogDensity(fog);
	}
}
