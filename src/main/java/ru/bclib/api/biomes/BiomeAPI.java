package ru.bclib.api.biomes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.impl.biome.NetherBiomeData;
import net.fabricmc.fabric.impl.biome.TheEndBiomeData;
import net.fabricmc.fabric.impl.structure.FabricStructureImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.WorldData;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import ru.bclib.BCLib;
import ru.bclib.config.Configs;
import ru.bclib.entity.BCLEntityWrapper;
import ru.bclib.interfaces.BiomeSourceAccessor;
import ru.bclib.interfaces.SurfaceMaterialProvider;
import ru.bclib.interfaces.SurfaceProvider;
import ru.bclib.interfaces.SurfaceRuleProvider;
import ru.bclib.mixin.common.BiomeGenerationSettingsAccessor;
import ru.bclib.mixin.common.BiomeSourceMixin;
import ru.bclib.mixin.common.MobSpawnSettingsAccessor;
import ru.bclib.mixin.common.StructureSettingsAccessor;
import ru.bclib.util.CollectionsUtil;
import ru.bclib.util.MHelper;
import ru.bclib.world.biomes.BCLBiome;
import ru.bclib.world.biomes.BCLBiomeSettings;
import ru.bclib.world.biomes.FabricBiomesData;
import ru.bclib.world.biomes.VanillaBiomeSettings;
import ru.bclib.world.features.BCLFeature;
import ru.bclib.world.generator.BiomePicker;
import ru.bclib.world.structures.BCLStructureFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
	
	private static final Map<PlacedFeature, Integer> FEATURE_ORDER = Maps.newHashMap();
	private static final MutableInt FEATURE_ORDER_ID = new MutableInt(0);
	
	private final static Map<StructureID, BiConsumer<Map<StructureFeature<?>, Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>>, Map<StructureFeature<?>, StructureFeatureConfiguration>>> STRUCTURE_STARTS = new HashMap<>();
	private static final Map<ResourceKey, List<BiConsumer<ResourceLocation, Biome>>> MODIFICATIONS = Maps.newHashMap();
	private static final Map<ResourceLocation, SurfaceRules.RuleSource> SURFACE_RULES = Maps.newHashMap();
	private static final Set<NoiseGeneratorSettings> NOISE_GENERATOR_SETTINGS = new HashSet<>();
	
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
	
	private static void initFeatureOrder() {
		if (!FEATURE_ORDER.isEmpty()) {
			return;
		}
		
		BuiltinRegistries.BIOME
			.entrySet()
			.stream()
			.filter(entry -> entry
				.getKey()
				.location()
				.getNamespace()
				.equals("minecraft"))
			.map(Entry::getValue)
			.map(biome -> (BiomeGenerationSettingsAccessor) biome.getGenerationSettings())
			.map(BiomeGenerationSettingsAccessor::bclib_getFeatures)
			.forEach(stepFeatureSuppliers -> stepFeatureSuppliers.forEach(step -> step.forEach(featureSupplier -> {
				PlacedFeature feature = featureSupplier.get();
				FEATURE_ORDER.computeIfAbsent(feature, f -> FEATURE_ORDER_ID.getAndIncrement());
			})));
	}
	
	/**
	 * Initialize registry for current server.
	 * @param biomeRegistry - {@link Registry} for {@link Biome}.
	 */
	public static void initRegistry(Registry<Biome> biomeRegistry) {
		if (biomeRegistry != BiomeAPI.biomeRegistry) {
			BiomeAPI.biomeRegistry = biomeRegistry;
			CLIENT.clear();
		}
	}
	
	private static final Set<BiomeSource> worldSources = new HashSet<>();
	
	/**
	 * For internal use only.
	 *
	 * Used by {@link BiomeSourceMixin} to add new BiomeSources to the BiomeAPI. We need to know all
	 * created Sources in order to rebuild their feature set whenever biomes get recreated
	 * through a datapack.
	 * @param source The new {@link BiomeSource}
	 */
	public static void registerBiomeSource(BiomeSource source){
		worldSources.add(source);
	}
	
	private static WorldData worldData;
	public static void registerWorldData(WorldData w){
		worldData = w;
		if (worldData!=null){
			worldData.worldGenSettings().dimensions().forEach(dim->{
				StructureSettingsAccessor a = (StructureSettingsAccessor)dim.generator().getSettings();
				STRUCTURE_STARTS.entrySet().forEach(entry -> applyStructureStarts(a, entry.getValue()));
			});
		}
	}
	
	/**
	 * For internal use only.
	 *
	 * This method gets called before a world is loaded/created to flush cashes we build. The Method is
	 * called from  {@link ru.bclib.mixin.client.MinecraftMixin}
	 */
	public static void prepareWorldData(){
		STRUCTURE_STARTS.clear();
		worldSources.clear();
		NOISE_GENERATOR_SETTINGS.clear();
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
		BCLBiome subBiome = new BCLBiome(biome, VanillaBiomeSettings.createVanilla().setGenChance(genChance).build());
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
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).orElseThrow();
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
		BCLBiome bclBiome = new BCLBiome(biome, null);
		
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
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).orElseThrow();
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
		BCLBiome bclBiome = new BCLBiome(biome, null);
		
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
		BCLBiome bclBiome = new BCLBiome(biome, VanillaBiomeSettings.createVanilla().setGenChance(genChance).build());
		
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
		ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome.getBiome()).orElseThrow();
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
		BCLBiome bclBiome = new BCLBiome(biome, null);
		
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
		BCLBiome bclBiome = new BCLBiome(biome, VanillaBiomeSettings.createVanilla().setGenChance(genChance).build());
		
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
	 * Get biome {@link ResourceKey} from given {@link Biome}.
	 * @param biome - {@link Biome} from server world.
	 * @return biome {@link ResourceKey} or {@code null}.
	 */
	@Nullable
	public static ResourceKey getBiomeKey(Biome biome) {
		return BuiltinRegistries.BIOME
			.getResourceKey(biome)
			.orElseGet(() -> biomeRegistry != null ? biomeRegistry.getResourceKey(biome).orElse(null) : null);
	}
	
	/**
	 * Get biome {@link ResourceLocation} from given {@link Biome}.
	 * @param biome - {@link Biome} from server world.
	 * @return biome {@link ResourceLocation}.
	 */
	public static ResourceLocation getBiomeID(Biome biome) {
		ResourceLocation id = BuiltinRegistries.BIOME.getKey(biome);
		if (id == null && biomeRegistry != null) {
			id = biomeRegistry.getKey(biome);
		}
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
	 * Get {@link BCLBiome} from given {@link Biome}.
	 * @param biome - biome {@link Biome}.
	 * @return {@link BCLBiome} or {@code BiomeAPI.EMPTY_BIOME}.
	 */
	public static BCLBiome getBiome(Biome biome) {
		return getBiome(BiomeAPI.getBiomeID(biome));
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
		List<BiConsumer<ResourceLocation, Biome>> modifications = MODIFICATIONS.computeIfAbsent(dimensionID, k -> Lists.newArrayList());
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
		BiomeSource source = level.getChunkSource().getGenerator().getBiomeSource();
		Set<Biome> biomes = source.possibleBiomes();
		
		NoiseGeneratorSettings generator = null;
		if (level.dimension() == Level.NETHER) {
			generator = BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.NETHER);
		}
		else if (level.dimension() == Level.END) {
			generator = BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.END);
		}
		
		if (generator != null) {
			List<SurfaceRules.RuleSource> rules = getRuleSources(biomes);
			SurfaceRuleProvider provider = SurfaceRuleProvider.class.cast(generator);
			changeSurfaceRules(rules, provider);
		}
		
		List<BiConsumer<ResourceLocation, Biome>> modifications = MODIFICATIONS.get(level.dimension());
		if (modifications == null) {
			biomes.forEach(biome -> sortBiomeFeatures(biome));
			((BiomeSourceAccessor) source).bclRebuildFeatures();
			return;
		}
		
		biomes.forEach(biome -> {
			applyModificationsToBiome(modifications, biome);
		});
		
		((BiomeSourceAccessor) source).bclRebuildFeatures();
	}

	private static void applyModificationsToBiome(List<BiConsumer<ResourceLocation, Biome>> modifications, Biome biome) {
		ResourceLocation biomeID = getBiomeID(biome);
		modifications.forEach(consumer -> {
			consumer.accept(biomeID, biome);
		});
		
		final BCLBiome bclBiome = BiomeAPI.getBiome(biome);
		if (bclBiome != null) {
			addStepFeaturesToBiome(biome, bclBiome.getFeatures());
		}
		
		sortBiomeFeatures(biome);
	}

	public static void sortBiomeFeatures(Biome biome) {
		BiomeGenerationSettings settings = biome.getGenerationSettings();
		BiomeGenerationSettingsAccessor accessor = (BiomeGenerationSettingsAccessor) settings;
		List<List<Supplier<PlacedFeature>>> featureList = CollectionsUtil.getMutable(accessor.bclib_getFeatures());
		final int size = featureList.size();
		for (int i = 0; i < size; i++) {
			List<Supplier<PlacedFeature>> features = CollectionsUtil.getMutable(featureList.get(i));
			sortFeatures(features);
			featureList.set(i, features);
		}
		accessor.bclib_setFeatures(featureList);
	}
	
	private static List<SurfaceRules.RuleSource> getRuleSources(Set<Biome> biomes) {
		Set<ResourceLocation> biomeIDs = biomes
			.stream()
			.map(biome -> getBiomeID(biome))
			.collect(Collectors.toSet());
		return getRuleSourcesFromIDs(biomeIDs);
	}

	private static List<SurfaceRules.RuleSource> getAllRuleSources() {
		List<SurfaceRules.RuleSource> rules = Lists.newArrayList();
		SURFACE_RULES.forEach((biomeID, rule) -> {
			rules.add(rule);
		});
		return rules;
	}
	
	private static List<SurfaceRules.RuleSource> getRuleSourcesFromIDs(Set<ResourceLocation> biomeIDs) {
		List<SurfaceRules.RuleSource> rules = Lists.newArrayList();
		SURFACE_RULES.forEach((biomeID, rule) -> {
			if (biomeIDs.contains(biomeID)) {
				rules.add(rule);
			}
		});
		return rules;
	}
	
	/**
	 * Adds new features to existing biome.
	 * @param biome {@link Biome} to add features in.
	 * @param feature {@link ConfiguredFeature} to add.
	 *
	 */
	public static void addBiomeFeature(Biome biome, BCLFeature feature) {
		addBiomeFeature(biome, feature.getDecoration(), feature.getPlacedFeature());
	}
	
	/**
	 * Adds new features to existing biome.
	 * @param biome {@link Biome} to add features in.
	 * @param step a {@link Decoration} step for the feature.
	 * @param featureList {@link ConfiguredFeature} to add.
	 */
	public static void addBiomeFeature(Biome biome, Decoration step, PlacedFeature... featureList) {
		addBiomeFeature(biome, step, List.of(featureList));
	}
	
	/**
	 * Adds new features to existing biome.
	 * @param biome {@link Biome} to add features in.
	 * @param step a {@link Decoration} step for the feature.
	 * @param featureList List of {@link ConfiguredFeature} to add.
	 */
	private static void addBiomeFeature(Biome biome, Decoration step, List<PlacedFeature> featureList) {
		BiomeGenerationSettingsAccessor accessor = (BiomeGenerationSettingsAccessor) biome.getGenerationSettings();
		List<List<Supplier<PlacedFeature>>> allFeatures = CollectionsUtil.getMutable(accessor.bclib_getFeatures());
		Set<PlacedFeature> set = CollectionsUtil.getMutable(accessor.bclib_getFeatureSet());
		List<Supplier<PlacedFeature>> features = getFeaturesList(allFeatures, step);
		for (var feature : featureList) {
			features.add(() -> feature);
			set.add(feature);
		}
		accessor.bclib_setFeatures(allFeatures);
		accessor.bclib_setFeatureSet(set);
	}
	
	/**
	 * For internal use only!
	 *
	 * Adds new features to existing biome. Called from {@link BCLBiome#setFeatures(Map)} when the Biome is first built
	 * @param biome {@link Biome} to add features in.
	 * @param featureMap Map of {@link ConfiguredFeature} to add.
	 */
	public static void addStepFeaturesToBiome(Biome biome, Map<Decoration, List<Supplier<PlacedFeature>>> featureMap) {
		BiomeGenerationSettingsAccessor accessor = (BiomeGenerationSettingsAccessor) biome.getGenerationSettings();
		List<List<Supplier<PlacedFeature>>> allFeatures = CollectionsUtil.getMutable(accessor.bclib_getFeatures());
		Set<PlacedFeature> set = CollectionsUtil.getMutable(accessor.bclib_getFeatureSet());
		
		for (Decoration step: featureMap.keySet()) {
			List<Supplier<PlacedFeature>> features = getFeaturesList(allFeatures, step);
			List<Supplier<PlacedFeature>> featureList = featureMap.get(step);
			
			for (Supplier<PlacedFeature> feature : featureList) {
				features.add(feature);
				set.add(feature.get());
			}
		}
		accessor.bclib_setFeatures(allFeatures);
		accessor.bclib_setFeatureSet(set);
	}
	
	/**
	 * Adds new structure feature to existing biome.
	 * @param biomeKey {@link ResourceKey} for the {@link Biome} to add structure feature in.
	 * @param structure {@link ConfiguredStructureFeature} to add.
	 */
	public static void addBiomeStructure(ResourceKey biomeKey, ConfiguredStructureFeature structure) {
		if (biomeKey == null){
			BCLib.LOGGER.error("null is not a valid biomeKey for " + structure);
			return;
		}
		changeStructureStarts(biomeKey.location(), structure, (structureMap, configMap) -> {
			Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> configuredMap = structureMap.computeIfAbsent(structure.feature, k -> HashMultimap.create());
			configuredMap.put(structure, biomeKey);
			
			StructureFeatureConfiguration config = FabricStructureImpl.STRUCTURE_TO_CONFIG_MAP.get(structure.feature);
			if (config != null){
				configMap.put(structure.feature, config);
			}
		});
	}
	
	public static void addBiomeStructure(Biome biome, ConfiguredStructureFeature structure) {
		changeStructureStarts(BiomeAPI.getBiomeID(biome), structure, (structureMap, configMap) -> {
			Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> configuredMap = structureMap.computeIfAbsent(structure.feature, k -> HashMultimap.create());
			var key = getBiomeKey(biome);
			if (key != null) {
				StructureFeatureConfiguration config = FabricStructureImpl.STRUCTURE_TO_CONFIG_MAP.get(structure.feature);
				if (config != null) {
					configMap.put(structure.feature, config);
				}
				configuredMap.put(structure, key);
			} else {
				BCLib.LOGGER.warning("Unable to find Biome " + getBiomeID(biome));
			}
		});
	}
	
	/**
	 * Adds new structure feature to existing biome.
	 * @param biomeKey {@link ResourceKey} for the {@link Biome} to add structure feature in.
	 * @param structure {@link BCLStructureFeature} to add.
	 */
	public static void addBiomeStructure(ResourceKey biomeKey, BCLStructureFeature structure) {
		addBiomeStructure(biomeKey, structure.getFeatureConfigured());
	}

	/**
	 * Adds new structure features to existing biome.
	 * @param  biomeKey {@link ResourceKey} for the {@link Biome} to add structure features in.
	 * @param structures array of {@link BCLStructureFeature} to add.
	 */
	public static void addBiomeStructures(ResourceKey biomeKey, BCLStructureFeature... structures) {
		for (BCLStructureFeature structure: structures) {
			addBiomeStructure(biomeKey, structure.getFeatureConfigured());
		}
	}
	
	/**
	 * Adds new carver into existing biome.
	 * @param biome {@link Biome} to add carver in.
	 * @param carver {@link ConfiguredWorldCarver} to add.
	 * @param stage {@link Carving} stage.
	 */
	public static void addBiomeCarver(Biome biome, ConfiguredWorldCarver carver, Carving stage) {
		BiomeGenerationSettingsAccessor accessor = (BiomeGenerationSettingsAccessor) biome.getGenerationSettings();
		Map<Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers = CollectionsUtil.getMutable(accessor.bclib_getCarvers());
		List<Supplier<ConfiguredWorldCarver<?>>> carverList = CollectionsUtil.getMutable(carvers.getOrDefault(stage, new ArrayList<>()));
		carvers.put(stage, carverList);
		carverList.add(() -> carver);
		accessor.bclib_setCarvers(carvers);
	}
	
	/**
	 * Adds surface rule to specified biome.
	 * @param biomeID biome {@link ResourceLocation}.
	 * @param source {@link SurfaceRules.RuleSource}.
	 */
	public static void addSurfaceRule(ResourceLocation biomeID, SurfaceRules.RuleSource source) {
		SURFACE_RULES.put(biomeID, source);
		NOISE_GENERATOR_SETTINGS.forEach(BiomeAPI::changeSurfaceRulesForGenerator);
	}
	
	/**
	 * Get surface rule for the biome using its {@link ResourceLocation} ID as a key.
	 * @param biomeID {@link ResourceLocation} biome ID.
	 * @return {@link SurfaceRules.RuleSource}.
	 */
	@Nullable
	public static SurfaceRules.RuleSource getSurfaceRule(ResourceLocation biomeID) {
		return SURFACE_RULES.get(biomeID);
	}
	
	/**
	 * Adds mob spawning to specified biome.
	 * @param biome {@link Biome} to add mob spawning.
	 * @param entityType {@link BCLEntityWrapper} mob type.
	 * @param weight spawn weight.
	 * @param minGroupCount minimum mobs in group.
	 * @param maxGroupCount maximum mobs in group.
	 */
	public static <M extends Mob> void addBiomeMobSpawn(Biome biome, BCLEntityWrapper<M> entityType, int weight, int minGroupCount, int maxGroupCount) {
		if (entityType.canSpawn()){
			addBiomeMobSpawn(biome, entityType.type(), weight, minGroupCount, maxGroupCount);
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
		final MobCategory category = entityType.getCategory();
		MobSpawnSettingsAccessor accessor = (MobSpawnSettingsAccessor) biome.getMobSettings();
		Map<MobCategory, WeightedRandomList<SpawnerData>> spawners = CollectionsUtil.getMutable(accessor.bcl_getSpawners());
		List<SpawnerData> mobs = spawners.containsKey(category) ? CollectionsUtil.getMutable(spawners.get(category).unwrap()) : Lists.newArrayList();
		mobs.add(new SpawnerData(entityType, weight, minGroupCount, maxGroupCount));
		spawners.put(category, WeightedRandomList.create(mobs));
		accessor.bcl_setSpawners(spawners);
	}
	
	/**
	 * Get biome surface block. Can be used to get terrain material for features or other things.
	 * @param pos {@link BlockPos} position to get block.
	 * @param biome {@link Biome} to get block from.
	 * @param level {@link ServerLevel} current server level.
	 * @return {@link BlockState} with the biome surface or AIR if it fails.
	 */
	public static BlockState getBiomeSurfaceBlock(BlockPos pos, Biome biome, ServerLevel level) {
		ChunkGenerator generator = level.getChunkSource().getGenerator();
		if (generator instanceof NoiseBasedChunkGenerator) {
			SurfaceProvider provider = SurfaceProvider.class.cast(generator);
			return provider.getSurface(pos, biome, level);
		}
		return Blocks.AIR.defaultBlockState();
	}

	public static Optional<BlockState> findTopMaterial(WorldGenLevel world, BlockPos pos){
		return findTopMaterial(getBiome(world.getBiome(pos)));
	}

	public static Optional<BlockState> findTopMaterial(Biome biome){
		return findTopMaterial(getBiome(biome));
	}

	public static Optional<BlockState> findTopMaterial(BCLBiome biome){
		if (biome instanceof SurfaceMaterialProvider smp){
			return Optional.of(smp.getTopMaterial());
		}
		return Optional.empty();
	}

	public static Optional<BlockState> findUnderMaterial(WorldGenLevel world, BlockPos pos){
		return findUnderMaterial(getBiome(world.getBiome(pos)));
	}

	public static Optional<BlockState> findUnderMaterial(Biome biome){
		return findUnderMaterial(getBiome(biome));
	}

	public static Optional<BlockState> findUnderMaterial(BCLBiome biome){
		if (biome instanceof SurfaceMaterialProvider smp){
			return Optional.of(smp.getUnderMaterial());
		}
		return Optional.empty();
	}
	
	/**
	 * Set biome in chunk at specified position.
	 * @param chunk {@link ChunkAccess} chunk to set biome in.
	 * @param pos {@link BlockPos} biome position.
	 * @param biome {@link Biome} instance. Should be biome from world.
	 */
	public static void setBiome(ChunkAccess chunk, BlockPos pos, Biome biome) {
		int sectionY = (pos.getY() - chunk.getMinBuildHeight()) >> 4;
		PalettedContainer<Biome> biomes = chunk.getSection(sectionY).getBiomes();
		biomes.set((pos.getX() & 15) >> 2, (pos.getY() & 15) >> 2, (pos.getZ() & 15) >> 2, biome);
	}
	
	/**
	 * Set biome in world at specified position.
	 * @param level {@link LevelAccessor} world to set biome in.
	 * @param pos {@link BlockPos} biome position.
	 * @param biome {@link Biome} instance. Should be biome from world.
	 */
	public static void setBiome(LevelAccessor level, BlockPos pos, Biome biome) {
		ChunkAccess chunk = level.getChunk(pos);
		setBiome(chunk, pos, biome);
	}
	
	static class StructureID {
		public final ResourceLocation id;
		public final ConfiguredStructureFeature structure;
		
		StructureID(ResourceLocation id, ConfiguredStructureFeature structure){
			this.id = id;
			this.structure = structure;
		}

		@Override
		public String toString() {
			return "StructureID{" + "id=" + id + ", structure=" + structure + '}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			StructureID that = (StructureID) o;
			return id.equals(that.id) && structure.equals(that.structure);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(id, structure);
		}
	}
	
	private static void registerNoiseGeneratorAndChangeSurfaceRules(NoiseGeneratorSettings settings){
		NOISE_GENERATOR_SETTINGS.add(settings);
		changeSurfaceRulesForGenerator(settings);
	}
	
	public static void registerStructureEvents(){
		DynamicRegistrySetupCallback.EVENT.register(registryManager -> {
			Optional<? extends Registry<NoiseGeneratorSettings>> oGeneratorRegistry = registryManager.registry(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
//			Optional<? extends Registry<Codec<? extends BiomeSource>>> oBiomeSourceRegistry = registryManager.registry(Registry.BIOME_SOURCE_REGISTRY);
//
//			if (oBiomeSourceRegistry.isPresent()) {
//				RegistryEntryAddedCallback
//						.event(oBiomeSourceRegistry.get())
//						.register((rawId, id, source) -> {
//							BCLib.LOGGER.info(" #### " + rawId + ", " + source + ", " + id);
//						});
//			}
			
			if (oGeneratorRegistry.isPresent()) {
				oGeneratorRegistry.get().forEach(BiomeAPI::registerNoiseGeneratorAndChangeSurfaceRules);
				RegistryEntryAddedCallback
					.event(oGeneratorRegistry.get())
					.register((rawId, id, settings) -> {
						//BCLib.LOGGER.info(" #### " + rawId + ", " + object + ", " + id);
						
						//add back modded structures
						StructureSettingsAccessor a = (StructureSettingsAccessor)settings.structureSettings();
						STRUCTURE_STARTS.entrySet().forEach(entry -> applyStructureStarts(a, entry.getValue()));
						
						//add surface rules
						registerNoiseGeneratorAndChangeSurfaceRules(settings);
					});
			}
			
			
		});
	}
	
	private static void changeSurfaceRulesForGenerator(NoiseGeneratorSettings settings){
		List<SurfaceRules.RuleSource> rules;
		if (biomeRegistry!=null) {
			rules = getRuleSourcesFromIDs(biomeRegistry.keySet());
		} else {
			rules = getAllRuleSources();
		}
		SurfaceRuleProvider provider = SurfaceRuleProvider.class.cast(settings);
		changeSurfaceRules(rules, provider);
	}
	
	private static void changeSurfaceRules(List<RuleSource> rules, SurfaceRuleProvider provider) {
		if (rules.size() > 0) {
			provider.addCustomRules(rules);
		}
		else {
			provider.clearCustomRules();
		}
	}
	
	private static void changeStructureStarts(ResourceLocation id, ConfiguredStructureFeature structure, BiConsumer<Map<StructureFeature<?>, Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>>, Map<StructureFeature<?>, StructureFeatureConfiguration>> modifier) {
		STRUCTURE_STARTS.put(new StructureID(id, structure), modifier);
		Registry<NoiseGeneratorSettings> chunkGenSettingsRegistry = BuiltinRegistries.NOISE_GENERATOR_SETTINGS;
		
		for (Map.Entry<ResourceKey<NoiseGeneratorSettings>, NoiseGeneratorSettings> entry : chunkGenSettingsRegistry.entrySet()) {
			final StructureSettingsAccessor access = (StructureSettingsAccessor)  entry.getValue().structureSettings();
			applyStructureStarts(access, modifier);
		}
		
		if (worldData!=null){
			worldData.worldGenSettings().dimensions().forEach(dim->{
				StructureSettingsAccessor access = (StructureSettingsAccessor)dim.generator().getSettings();
				applyStructureStarts(access, modifier);
			});
		}
	}
	
	
	private static void applyStructureStarts(StructureSettingsAccessor access, BiConsumer<Map<StructureFeature<?>, Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>>, Map<StructureFeature<?>, StructureFeatureConfiguration>> modifier) {
			Map<StructureFeature<?>, Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> structureMap;
			Map<StructureFeature<?>, StructureFeatureConfiguration> configMap;
			
			structureMap = getMutableConfiguredStructures(access);
			configMap = getMutableStructureConfig(access);
			
			modifier.accept(structureMap, configMap);
			
			setMutableConfiguredStructures(access, structureMap);
			setMutableStructureConfig(access, configMap);
	}
	
	private static void sortFeatures(List<Supplier<PlacedFeature>> features) {
		initFeatureOrder();
		
		Set<PlacedFeature> featuresWithoutDuplicates = Sets.newHashSet();
		features.forEach(provider -> featuresWithoutDuplicates.add(provider.get()));
		
		if (featuresWithoutDuplicates.size() != features.size()) {
			features.clear();
			featuresWithoutDuplicates.forEach(feature -> features.add(() -> feature));
		}
		
		features.forEach(provider -> {
			PlacedFeature feature = provider.get();
			FEATURE_ORDER.computeIfAbsent(feature, f -> FEATURE_ORDER_ID.getAndIncrement());
		});
		
		features.sort((f1, f2) -> {
			int v1 = FEATURE_ORDER.getOrDefault(f1.get(), 70000);
			int v2 = FEATURE_ORDER.getOrDefault(f2.get(), 70000);
			return Integer.compare(v1, v2);
		});
	}
	
	/**
	 * Getter for correct feature list from all biome feature list of lists.
	 * @param step feature {@link Decoration} step.
	 * @param lists biome accessor lists.
	 * @return mutable {@link ConfiguredFeature} list.
	 */
	private static List<Supplier<PlacedFeature>> getList(Decoration step, List<List<Supplier<PlacedFeature>>> lists) {
		int index = step.ordinal();
		if (lists.size() <= index) {
			for (int i = lists.size(); i <= index; i++) {
				lists.add(Lists.newArrayList());
			}
		}
		List<Supplier<PlacedFeature>> list = CollectionsUtil.getMutable(lists.get(index));
		lists.set(index, list);
		return list;
	}
	
	private static List<Supplier<PlacedFeature>> getFeaturesList(List<List<Supplier<PlacedFeature>>> features, Decoration step) {
		int index = step.ordinal();
		while (features.size() <= index) {
			features.add(Lists.newArrayList());
		}
		List<Supplier<PlacedFeature>> mutable = CollectionsUtil.getMutable(features.get(index));
		features.set(index, mutable);
		return mutable;
	}
	
	//inspired by net.fabricmc.fabric.impl.biome.modification.BiomeStructureStartsImpl
	private static Map<StructureFeature<?>, Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> getMutableConfiguredStructures(StructureSettingsAccessor access) {
		ImmutableMap<StructureFeature<?>, ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> configuredStructures = access.bcl_getConfiguredStructures();
		Map<StructureFeature<?>, Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> result = new HashMap<>(configuredStructures.size());
		
		for (Map.Entry<StructureFeature<?>, ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> entry : configuredStructures.entrySet()) {
			result.put(entry.getKey(), HashMultimap.create(entry.getValue()));
		}
		
		return result;
	}
	
	//inspired by net.fabricmc.fabric.impl.biome.modification.BiomeStructureStartsImpl
	private static void setMutableConfiguredStructures(StructureSettingsAccessor access, Map<StructureFeature<?>, Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> structureStarts) {
		access.bcl_setConfiguredStructures(
			structureStarts
				.entrySet()
				.stream()
				.collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> ImmutableMultimap.copyOf(e.getValue())))
		);
	}
	
	private static Map<StructureFeature<?>, StructureFeatureConfiguration> getMutableStructureConfig(StructureSettingsAccessor access) {
		return CollectionsUtil.getMutable(access.bcl_getStructureConfig());
	}
	
	private static void setMutableStructureConfig(StructureSettingsAccessor access, Map<StructureFeature<?>, StructureFeatureConfiguration> structureConfig) {
		access.bcl_setStructureConfig(structureConfig);
	}
}
