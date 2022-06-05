package org.betterx.bclib.api.features.config;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public abstract class PlaceBlockFeatureConfig implements FeatureConfiguration {

    protected static <T extends PlaceBlockFeatureConfig> RecordCodecBuilder<T, SimpleWeightedRandomList<BlockState>> blockStateCodec() {
        return SimpleWeightedRandomList
                .wrappedCodec(BlockState.CODEC)
                .fieldOf("entries")
                .forGetter((T o) -> o.weightedList);
    }

    protected final SimpleWeightedRandomList<BlockState> weightedList;


    protected static SimpleWeightedRandomList<BlockState> buildWeightedList(List<BlockState> states) {
        var builder = SimpleWeightedRandomList.<BlockState>builder();
        for (BlockState s : states) builder.add(s, 1);
        return builder.build();
    }

    protected static SimpleWeightedRandomList<BlockState> buildWeightedList(BlockState state) {
        return SimpleWeightedRandomList
                .<BlockState>builder()
                .add(state, 1)
                .build();
    }

    public PlaceBlockFeatureConfig(Block block) {
        this(block.defaultBlockState());
    }

    public PlaceBlockFeatureConfig(BlockState state) {
        this(buildWeightedList(state));
    }


    public PlaceBlockFeatureConfig(List<BlockState> states) {
        this(buildWeightedList(states));
    }

    public PlaceBlockFeatureConfig(SimpleWeightedRandomList<BlockState> blocks) {
        this.weightedList = blocks;
    }

    public Optional<BlockState> getRandomBlock(RandomSource random) {
        return this.weightedList.getRandomValue(random);
    }

    public boolean place(FeaturePlaceContext<? extends PlaceBlockFeatureConfig> ctx) {
        Optional<BlockState> state = getRandomBlock(ctx.random());
        if (state.isPresent()) {
            return placeBlock(ctx, ctx.level(), ctx.origin(), state.get());
        }
        return false;
    }


    protected abstract boolean placeBlock(FeaturePlaceContext<? extends PlaceBlockFeatureConfig> ctx,
                                          WorldGenLevel level,
                                          BlockPos pos,
                                          BlockState targetState);
}
