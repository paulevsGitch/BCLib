package org.betterx.bclib.api.features.config;

import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.List;
import java.util.Optional;

public class BlockPlaceFeatureConfig implements FeatureConfiguration {
    public static final Codec<BlockPlaceFeatureConfig> CODEC = SimpleWeightedRandomList
            .wrappedCodec(BlockState.CODEC)
            .comapFlatMap(BlockPlaceFeatureConfig::create, cfg -> cfg.weightedList)
            .fieldOf("entries").codec();

    private final SimpleWeightedRandomList<BlockState> weightedList;

    private static DataResult<BlockPlaceFeatureConfig> create(SimpleWeightedRandomList<BlockState> simpleWeightedRandomList) {
        if (simpleWeightedRandomList.isEmpty()) {
            return DataResult.error("BlockPlaceFeatureConfig with no states");
        }
        return DataResult.success(new BlockPlaceFeatureConfig(simpleWeightedRandomList));
    }


    private static SimpleWeightedRandomList<BlockState> convert(List<BlockState> states) {
        var builder = SimpleWeightedRandomList.<BlockState>builder();
        for (BlockState s : states) builder.add(s, 1);
        return builder.build();
    }

    public BlockPlaceFeatureConfig(Block block) {
        this(block.defaultBlockState());
    }

    public BlockPlaceFeatureConfig(BlockState state) {
        this(SimpleWeightedRandomList
                .<BlockState>builder()
                .add(state, 1)
                .build());
    }

    public BlockPlaceFeatureConfig(List<BlockState> states) {
        this(convert(states));
    }

    public BlockPlaceFeatureConfig(SimpleWeightedRandomList<BlockState> blocks) {
        this.weightedList = blocks;
    }

    public Optional<BlockState> getRandomBlock(RandomSource random) {
        return this.weightedList.getRandomValue(random);
    }
}
