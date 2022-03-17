package ru.bclib.world.structures;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import ru.bclib.api.tag.TagAPI;
import ru.bclib.mixin.common.StructureFeatureAccessor;
import ru.bclib.mixin.common.StructureFeaturesAccessor;

import java.util.List;
import java.util.Random;

public class BCLStructureFeature {
	private static final Random RANDOM = new Random(354);
	private final StructureFeature<NoneFeatureConfiguration> structure;
	private final Holder<ConfiguredStructureFeature<?, ?>> featureConfigured;
	private final GenerationStep.Decoration featureStep;
	private final List<ResourceLocation> biomes = Lists.newArrayList();
	private final ResourceLocation id;
	public final TagKey<Biome> biomeTag;
	public final ResourceKey<ConfiguredStructureFeature<?, ?>> structureKey;
	public final ResourceKey<StructureSet> structureSetKey;

	public BCLStructureFeature(ResourceLocation id, StructureFeature<NoneFeatureConfiguration> structure, GenerationStep.Decoration step, int spacing, int separation) {
		this(id, structure, step, spacing, separation, false);
	}
	public BCLStructureFeature(ResourceLocation id, StructureFeature<NoneFeatureConfiguration> structure, GenerationStep.Decoration step, int spacing, int separation, boolean adaptNoise) {
		this.id = id;
		this.featureStep = step;
		//parts from vanilla for Structure generation
		//public static final ResourceKey<ConfiguredStructureFeature<?, ?>> JUNGLE_TEMPLE =
		//     BuiltinStructures.createKey("jungle_pyramid");
		//public static final Holder<ConfiguredStructureFeature<?, ?>> JUNGLE_TEMPLE =
		//     StructureFeatures.register(BuiltinStructures.JUNGLE_TEMPLE, StructureFeature.JUNGLE_TEMPLE.configured(NoneFeatureConfiguration.INSTANCE, BiomeTags.HAS_JUNGLE_TEMPLE));
		//public static final Holder<StructureSet> JUNGLE_TEMPLES =
		//      StructureSets.register(BuiltinStructureSets.JUNGLE_TEMPLES, StructureFeatures.JUNGLE_TEMPLE, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357619));
		//public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE =
		//      StructureFeature.register("jungle_pyramid", new JunglePyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
		//

		final RandomSpreadStructurePlacement spreadConfig = new RandomSpreadStructurePlacement(spacing, separation, RandomSpreadType.LINEAR, RANDOM.nextInt(8192));
		this.structureKey = ResourceKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, id);
		this.structureSetKey = ResourceKey.create(Registry.STRUCTURE_SET_REGISTRY, id);

		this.biomeTag = TagAPI.makeBiomeTag(id.getNamespace(), "has_structure/"+id.getPath());
		this.structure = StructureFeatureAccessor.callRegister(id.toString(), structure, step);
		this.featureConfigured = StructureFeaturesAccessor.callRegister(structureKey, this.structure.configured(NoneFeatureConfiguration.NONE, biomeTag, adaptNoise));
		StructureSets.register(structureSetKey, featureConfigured, spreadConfig);
		//TODO: 1.18 check if structures are added correctly
		//FlatChunkGeneratorConfigAccessor.getStructureToFeatures().put(this.structure, this.featureConfigured);
	}

	/**
	 * runs the {@code PieceGeneratorSupplier.Context::validBiome} from the given context at
	 * height=5 in the middle of the chunk.
	 *
	 * @param context The context to test with.
	 * @param <C> The FeatureConfiguration of the Context
	 * @return true, if this feature can spawn in the current biome
	 */
	public static <C extends FeatureConfiguration> boolean isValidBiome(PieceGeneratorSupplier.Context<C> context) {
		return isValidBiome(context, 5);
	}
	/**
	 * runs the {@code PieceGeneratorSupplier.Context::validBiome} from the given context at the
	 * given height in the middle of the chunk.
	 *
	 * @param context The context to test with.
	 * @param yPos The Height to test for
	 * @param <C> The FeatureConfiguration of the Context
	 * @return true, if this feature can spawn in the current biome
	 */
	public static <C extends FeatureConfiguration> boolean isValidBiome(PieceGeneratorSupplier.Context<C> context, int yPos) {
		BlockPos blockPos = context.chunkPos().getMiddleBlockPosition(yPos);
		return context.validBiome().test(
			context.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ()))
		);
	}

	public StructureFeature<NoneFeatureConfiguration> getStructure() {
		return structure;
	}
	
	public Holder<ConfiguredStructureFeature<?, ?>> getFeatureConfigured() {
		return featureConfigured;
	}
	
	public GenerationStep.Decoration getFeatureStep() {
		return featureStep;
	}
	
	/**
	 * Get the structure ID;
	 * @return {@link ResourceLocation} id.
	 */
	public ResourceLocation getID() {
		return id;
	}
	
	/**
	 * Adds biome into internal biome list, used in {@link ru.bclib.api.biomes.BCLBiomeBuilder}.
	 * @param biome {@link ResourceLocation} biome ID.
	 */
	public void addInternalBiome(ResourceLocation biome) {
		biomes.add(biome);
	}
	
	/**
	 * Get biome list where this structure feature can generate. Only represents biomes made with {@link ru.bclib.api.biomes.BCLBiomeBuilder} and only
	 * if structure was added during building process. Modification of this list will not affect structure generation.
	 * @return {@link List} of biome {@link ResourceLocation}.
	 */
	public List<ResourceLocation> getBiomes() {
		return biomes;
	}
}
