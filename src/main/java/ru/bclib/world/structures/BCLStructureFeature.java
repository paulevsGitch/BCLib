package ru.bclib.world.structures;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class BCLStructureFeature {
	private static final Random RANDOM = new Random(354);
	private final StructureFeature<NoneFeatureConfiguration> structure;
	private final ConfiguredStructureFeature<?, ?> featureConfigured;
	private final GenerationStep.Decoration featureStep;
	private final List<ResourceLocation> biomes = Lists.newArrayList();
	private final ResourceLocation id;
	
	public BCLStructureFeature(ResourceLocation id, StructureFeature<NoneFeatureConfiguration> structure, GenerationStep.Decoration step, int spacing, int separation) {
		this.id = id;
		this.featureStep = step;
		this.structure = FabricStructureBuilder
			.create(id, structure)
			.step(step)
			.defaultConfig(spacing, separation, RANDOM.nextInt(8192))
			.register();
		this.featureConfigured = this.structure.configured(NoneFeatureConfiguration.NONE);
		BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, id, this.featureConfigured);
		//TODO: 1.18 check if structures are added correctly
		//FlatChunkGeneratorConfigAccessor.getStructureToFeatures().put(this.structure, this.featureConfigured);
	}

	/**
	 * runs the {@link PieceGeneratorSupplier.Context::validBiome} from the given context at
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
	 * runs the {@link PieceGeneratorSupplier.Context::validBiome} from the given context at the
	 * given height in the middle of the chunk.
	 *
	 * @param context The context to test with.
	 * @param yPos The Height to test for
	 * @param <C> The FeatureConfiguration of the Context
	 * @return true, if this feature can spawn in the current biome
	 */
    public static <C extends FeatureConfiguration> boolean isValidBiome(PieceGeneratorSupplier.Context<C> context, int yPos) {
        BlockPos blockPos = context.chunkPos().getMiddleBlockPosition(yPos);

        return
                context.validBiome().test(
                        context.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(blockPos.getY()), QuartPos.fromBlock(blockPos.getZ()))
                );
    }

    public StructureFeature<NoneFeatureConfiguration> getStructure() {
		return structure;
	}
	
	public ConfiguredStructureFeature<?, ?> getFeatureConfigured() {
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
