package org.betterx.bclib.api.features;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalInt;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;

import com.mojang.serialization.Codec;
import org.betterx.bclib.api.features.config.ScatterFeatureConfig;
import org.betterx.bclib.api.tag.CommonBlockTags;
import org.betterx.bclib.util.BlocksHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ScatterFeature<FC extends ScatterFeatureConfig>
        extends Feature<FC> {

    public static <T extends ScatterFeatureConfig> BCLFeature createAndRegister(ResourceLocation location,
                                                                                int minPerChunk,
                                                                                int maxPerChunk,
                                                                                T cfg,
                                                                                Feature<T> inlineFeature) {
        List<Holder<PlacedFeature>> set = new ArrayList<>(2);
        if (cfg.floorChance > 0) set.add(PlacementUtils.inlinePlaced(inlineFeature,
                                                                     cfg,
                                                                     EnvironmentScanPlacement.scanningFor(Direction.DOWN,
                                                                                                          BlockPredicate.solid(),
                                                                                                          BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                                                                                          12),
                                                                     RandomOffsetPlacement.vertical(ConstantInt.of(1))));

        if (cfg.floorChance < 1) {
            set.add(PlacementUtils.inlinePlaced(inlineFeature,
                                                cfg,
                                                EnvironmentScanPlacement.scanningFor(Direction.UP,
                                                                                     BlockPredicate.solid(),
                                                                                     BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                                                                     12),
                                                RandomOffsetPlacement.vertical(ConstantInt.of(-1))));
        }
        SimpleRandomFeatureConfiguration configuration = new SimpleRandomFeatureConfiguration(HolderSet.direct(set));

        return BCLFeatureBuilder.start(location, SIMPLE_RANDOM_SELECTOR)
                                .decoration(GenerationStep.Decoration.VEGETAL_DECORATION)
                                .modifier(CountPlacement.of(UniformInt.of(minPerChunk, maxPerChunk)))
                                .modifier(InSquarePlacement.spread())
                                .randomHeight4FromFloorCeil()
                                .modifier(CountPlacement.of(UniformInt.of(2, 5)))
                                .modifier(RandomOffsetPlacement.of(
                                        ClampedNormalInt.of(0.0f, 2.0f, -6, 6),
                                        ClampedNormalInt.of(0.0f, 0.6f, -2, 2)))
                                .modifier(BiomeFilter.biome())
                                .buildAndRegister(configuration);
    }

    public ScatterFeature(Codec<FC> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FC> featurePlaceContext) {
        final WorldGenLevel level = featurePlaceContext.level();
        final BlockPos origin = featurePlaceContext.origin();
        final RandomSource random = featurePlaceContext.random();

        ScatterFeatureConfig config = featurePlaceContext.config();
        Optional<Direction> direction = getTipDirection(level, origin, random, config);
        if (direction.isEmpty()) {
            return false;
        }
        BlockPos basePos = origin.relative(direction.get(), -1);


        int i = (int) (random.nextFloat() * (1 + config.maxHeight - config.minHeight) + config.minHeight);
        growCenterPillar(level, origin, basePos, direction.get(), i, config, random);
        return true;
    }


    protected void growCenterPillar(LevelAccessor level,
                                    BlockPos origin,
                                    BlockPos basePos,
                                    Direction direction,
                                    int centerHeight,
                                    ScatterFeatureConfig config,
                                    RandomSource random) {
        if (config.isValidBase(level.getBlockState(basePos))) {
            final Direction surfaceDirection = direction.getOpposite();
            BlockPos.MutableBlockPos POS = new BlockPos.MutableBlockPos();
            buildPillarWithBase(level, origin, basePos, direction, centerHeight, config, random);
            
            final double distNormalizer = (config.maxSpread * Math.sqrt(2));
            final int tryCount = config.spreadCount.sample(random);
            for (int i = 0; i < tryCount; i++) {
                int x = origin.getX() + (int) (random.nextGaussian() * config.maxSpread);
                int z = origin.getZ() + (int) (random.nextGaussian() * config.maxSpread);
                POS.set(x, origin.getY(), z);

                if (BlocksHelper.findSurroundingSurface(level, POS, surfaceDirection, 4, config::isValidBase)) {
                    int myHeight;
                    if (config.growWhileFree) {
                        myHeight = BlocksHelper.blockCount(level,
                                                           POS,
                                                           direction,
                                                           config.maxHeight,
                                                           state -> state.getMaterial().isReplaceable());
                    } else {
                        myHeight = centerHeight;
                    }

                    POS.move(direction, 1);
                    int dx = x - POS.getX();
                    int dz = z - POS.getZ();
                    float sizeFactor = (1 - (float) (Math.sqrt(dx * dx + dz * dz) / distNormalizer));
                    sizeFactor = (1 - (random.nextFloat() * config.sizeVariation)) * sizeFactor;
                    myHeight = (int) Math.min(Math.max(
                            config.minHeight,
                            config.minHeight + sizeFactor * (myHeight - config.minHeight)
                                                      ), config.maxHeight);

                    buildPillarWithBase(level,
                                        POS,
                                        POS.relative(direction.getOpposite()),
                                        direction,
                                        myHeight,
                                        config,
                                        random);
                }
            }
        }
    }

    private void buildPillarWithBase(LevelAccessor level,
                                     BlockPos origin,
                                     BlockPos basePos,
                                     Direction direction,
                                     int height,
                                     ScatterFeatureConfig config,
                                     RandomSource random) {
        if (BlocksHelper.isFreeSpace(level, origin, direction, height, (state) -> state.is(Blocks.AIR))) {
            createPatchOfBaseBlocks(level, random, basePos, config);
            buildPillar(level, origin, direction, height, config, random);
        }
    }

    private void buildPillar(LevelAccessor level,
                             BlockPos origin,
                             Direction direction,
                             int height,
                             ScatterFeatureConfig config,
                             RandomSource random) {

        final BlockPos.MutableBlockPos POS = origin.mutable();
        buildBaseToTipColumn(height, (blockState) -> {
            BlocksHelper.setWithoutUpdate(level, POS, blockState);
            POS.move(direction);
        }, config, random);
    }

    protected void buildBaseToTipColumn(int totalHeight,
                                        Consumer<BlockState> consumer,
                                        ScatterFeatureConfig config,
                                        RandomSource random) {
        for (int size = 0; size < totalHeight; size++) {
            consumer.accept(config.createBlock(size, totalHeight - 1, random));
//            Block s = config.createBlock(size, totalHeight - 1, random).getBlock();
//            if (size == 0) s = Blocks.YELLOW_CONCRETE;
//            else if (size == 1) s = Blocks.LIME_CONCRETE;
//            else if (size == 2) s = Blocks.CYAN_CONCRETE;
//            else if (size == 3) s = Blocks.LIGHT_BLUE_CONCRETE;
//            else if (size == 4) s = Blocks.BLUE_CONCRETE;
//            else if (size == 5) s = Blocks.PURPLE_CONCRETE;
//            else if (size == 6) s = Blocks.MAGENTA_CONCRETE;
//            else s = Blocks.GRAY_CONCRETE;
//            consumer.accept(s.defaultBlockState());
        }
    }

    private Optional<Direction> getTipDirection(LevelAccessor levelAccessor,
                                                BlockPos blockPos,
                                                RandomSource randomSource,
                                                ScatterFeatureConfig config) {
        boolean onCeil = config.floorChance < 1 && config.isValidBase(levelAccessor.getBlockState(blockPos.above()));
        boolean onFloor = config.floorChance > 0 && config.isValidBase(levelAccessor.getBlockState(blockPos.below()));

        if (onCeil && onFloor) {
            return Optional.of(config.isFloor(randomSource) ? Direction.DOWN : Direction.UP);
        }
        if (onCeil) {
            return Optional.of(Direction.DOWN);
        }
        if (onFloor) {
            return Optional.of(Direction.UP);
        }
        return Optional.empty();
    }

    private void createPatchOfBaseBlocks(LevelAccessor levelAccessor,
                                         RandomSource randomSource,
                                         BlockPos blockPos,
                                         ScatterFeatureConfig config) {
        if (config.baseState.isPresent() && config.baseReplaceChance > 0 && randomSource.nextFloat() < config.baseReplaceChance) {
            final BlockState baseState = config.baseState.get();
            BlockPos pos;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (randomSource.nextFloat() > config.chanceOfDirectionalSpread) continue;
                pos = blockPos.relative(direction);
                placeBaseBlockIfPossible(levelAccessor, pos, baseState);

                if (randomSource.nextFloat() > config.chanceOfSpreadRadius2) continue;
                pos = pos.relative(Direction.getRandom(randomSource));
                placeBaseBlockIfPossible(levelAccessor, pos, baseState);

                if (randomSource.nextFloat() > config.chanceOfSpreadRadius3) continue;
                pos = pos.relative(Direction.getRandom(randomSource));
                placeBaseBlockIfPossible(levelAccessor, pos, baseState);
            }
            placeBaseBlockIfPossible(levelAccessor, blockPos, baseState);
        }
    }

    protected void placeBaseBlockIfPossible(LevelAccessor levelAccessor,
                                            BlockPos blockPos,
                                            BlockState baseState) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        if (blockState.is(CommonBlockTags.TERRAIN)) {
            levelAccessor.setBlock(blockPos, baseState, 2);
        }
    }
}
