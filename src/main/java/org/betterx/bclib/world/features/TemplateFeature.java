package org.betterx.bclib.world.features;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import com.mojang.serialization.Codec;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.features.BCLFeatureBuilder;
import org.betterx.bclib.world.structures.StructureNBT;
import org.betterx.bclib.world.structures.StructureWorldNBT;

public class TemplateFeature<FC extends TemplateFeatureConfig> extends Feature<FC> {
    public static final Feature<TemplateFeatureConfig> INSTANCE = BCLFeature.register(BCLib.makeID("template"),
            new TemplateFeature(
                    TemplateFeatureConfig.CODEC));

    public static <T extends TemplateFeatureConfig> BCLFeature createAndRegisterRare(ResourceLocation location,
                                                                                     TemplateFeatureConfig configuration,
                                                                                     int onceEveryChunk) {


        return BCLFeatureBuilder
                .start(location, INSTANCE)
                .decoration(GenerationStep.Decoration.SURFACE_STRUCTURES)
                .oncePerChunks(onceEveryChunk) //discard neighboring chunks
                .count(16) //try 16 placements in chunk
                .squarePlacement() //randomize x/z in chunk
                .randomHeight10FromFloorCeil() //randomize height 10 above and 10 below max vertical
                .findSolidFloor(12) //cast downward ray to find solid surface
                .isEmptyAbove4() //make sure we have 4 free blocks above
                .onlyInBiome() //ensure that we still are in the correct biome

                .buildAndRegister(configuration);
    }

    public static <T extends TemplateFeatureConfig> BCLFeature createAndRegister(ResourceLocation location,
                                                                                 TemplateFeatureConfig configuration,
                                                                                 int count) {
        return BCLFeatureBuilder
                .start(location, INSTANCE)
                .decoration(GenerationStep.Decoration.SURFACE_STRUCTURES)
                .count(count)
                .squarePlacement()
                .randomHeight10FromFloorCeil()
                .findSolidFloor(12) //cast downward ray to find solid surface
                .isEmptyAbove4()
                .onlyInBiome()
                .buildAndRegister(configuration);
    }

    public TemplateFeature(Codec<FC> codec) {
        super(codec);
    }

    protected StructureWorldNBT randomStructure(TemplateFeatureConfig cfg, RandomSource random) {

        if (cfg.structures.size() > 1) {
            final float chanceSum = cfg.structures.parallelStream().map(c -> c.chance).reduce(0.0f, (p, c) -> p + c);
            float rnd = random.nextFloat() * chanceSum;

            for (StructureWorldNBT c : cfg.structures) {
                rnd -= c.chance;
                if (rnd <= 0) return c;
            }
        } else {
            return cfg.structures.get(0);
        }

        return null;
    }

    @Override
    public boolean place(FeaturePlaceContext<FC> ctx) {
        StructureWorldNBT structure = randomStructure(ctx.config(), ctx.random());
        return structure.generateIfPlaceable(ctx.level(),
                ctx.origin(),
                StructureNBT.getRandomRotation(ctx.random()),
                StructureNBT.getRandomMirror(ctx.random())
        );
    }
}
