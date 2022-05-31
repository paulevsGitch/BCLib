package org.betterx.bclib.world.features;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;

import com.mojang.serialization.Codec;
import org.betterx.bclib.api.features.BCLFeatureBuilder;
import org.betterx.bclib.world.structures.StructureNBT;

public class TemplateFeature<FC extends TemplateFeatureConfig> extends Feature<FC> {
    public static final Feature<TemplateFeatureConfig> INSTANCE = BCLFeature.register("template",
                                                                                      new TemplateFeature(
                                                                                              TemplateFeatureConfig.CODEC));

    public static <T extends TemplateFeatureConfig> BCLFeature createAndRegister(TemplateFeatureConfig configuration,
                                                                                 int onveEveryChunk) {
        return BCLFeatureBuilder
                .start(new ResourceLocation(configuration.structure.location.getNamespace(),
                                            "feature_" + configuration.structure.location.getPath()), INSTANCE)
                .decoration(GenerationStep.Decoration.SURFACE_STRUCTURES)
                .oncePerChunks(onveEveryChunk)
                .squarePlacement()
                .distanceToTopAndBottom10()
                .modifier(EnvironmentScanPlacement.scanningFor(Direction.DOWN,
                                                               BlockPredicate.solid(),
                                                               BlockPredicate.matchesBlocks(Blocks.AIR,
                                                                                            Blocks.WATER,
                                                                                            Blocks.LAVA),
                                                               12))
                .modifier(BiomeFilter.biome())
                .buildAndRegister(configuration);
    }

    public TemplateFeature(Codec<FC> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FC> ctx) {
        return ctx.config().structure.generateIfPlaceable(ctx.level(),
                                                          ctx.origin(),
                                                          StructureNBT.getRandomRotation(ctx.random()),
                                                          StructureNBT.getRandomMirror(ctx.random())
                                                         );
    }
}
