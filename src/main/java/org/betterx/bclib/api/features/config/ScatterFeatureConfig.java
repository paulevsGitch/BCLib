package org.betterx.bclib.api.features.config;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import com.mojang.datafixers.util.Function15;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.util.BlocksHelper;

import java.util.Optional;

public abstract class ScatterFeatureConfig implements FeatureConfiguration {
    public interface Instancer<T extends ScatterFeatureConfig> extends Function15<BlockState, Optional<BlockState>, Optional<BlockState>, Optional<BlockState>, Float, Float, Float, Float, Integer, Integer, Float, Float, Float, Boolean, IntProvider, T> {
    }

    public final BlockState clusterBlock;
    public final BlockState tipBlock;
    public final BlockState bottomBlock;
    public final Optional<BlockState> baseState;
    public final float baseReplaceChance;
    public final float chanceOfDirectionalSpread;
    public final float chanceOfSpreadRadius2;
    public final float chanceOfSpreadRadius3;
    public final int minHeight;
    public final int maxHeight;
    public final float maxSpread;
    public final float sizeVariation;
    public final float floorChance;

    public final IntProvider spreadCount;

    public final boolean growWhileFree;

    public ScatterFeatureConfig(BlockState clusterBlock,
                                Optional<BlockState> tipBlock,
                                Optional<BlockState> bottomBlock,
                                Optional<BlockState> baseState,
                                float baseReplaceChance,
                                float chanceOfDirectionalSpread,
                                float chanceOfSpreadRadius2,
                                float chanceOfSpreadRadius3,
                                int minHeight,
                                int maxHeight,
                                float maxSpread,
                                float sizeVariation,
                                float floorChance,
                                boolean growWhileFree,
                                IntProvider spreadCount) {
        this.clusterBlock = clusterBlock;
        this.tipBlock = tipBlock.orElse(clusterBlock);
        this.bottomBlock = bottomBlock.orElse(clusterBlock);
        this.baseState = baseState;
        this.baseReplaceChance = baseReplaceChance;
        this.chanceOfDirectionalSpread = chanceOfDirectionalSpread;
        this.chanceOfSpreadRadius2 = chanceOfSpreadRadius2;
        this.chanceOfSpreadRadius3 = chanceOfSpreadRadius3;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.maxSpread = maxSpread;
        this.sizeVariation = sizeVariation;
        this.floorChance = floorChance;
        this.growWhileFree = growWhileFree;
        this.spreadCount = spreadCount;
    }


    public boolean isFloor(RandomSource random) {
        return random.nextFloat() < floorChance;
    }

    public abstract boolean isValidBase(BlockState state);

    public abstract BlockState createBlock(int height, int maxHeight, RandomSource random);

    public static <T extends ScatterFeatureConfig> Codec<T> buildCodec(Instancer<T> instancer) {
        return RecordCodecBuilder.create((instance) -> instance
                .group(BlockState.CODEC
                                .fieldOf("cluster_block")
                                .forGetter((T cfg) -> cfg.clusterBlock),
                        BlockState.CODEC
                                .optionalFieldOf("tip_block")
                                .orElse(Optional.empty())
                                .forGetter((T cfg) -> cfg.tipBlock == cfg.clusterBlock
                                        ? Optional.empty()
                                        : Optional.of(cfg.tipBlock)),
                        BlockState.CODEC
                                .optionalFieldOf("bottom_block")
                                .orElse(Optional.empty())
                                .forGetter((T cfg) -> cfg.bottomBlock == cfg.clusterBlock
                                        ? Optional.empty()
                                        : Optional.of(cfg.bottomBlock)),
                        BlockState.CODEC
                                .optionalFieldOf("base_state")
                                .forGetter((T cfg) -> cfg.baseState),
                        Codec
                                .floatRange(0.0F, 1.0F)
                                .fieldOf("baseReplaceChance")
                                .orElse(1.0F)
                                .forGetter((T cfg) -> cfg.baseReplaceChance),
                        Codec
                                .floatRange(0.0F, 1.0F)
                                .fieldOf("chance_of_directional_spread")
                                .orElse(0.7F)
                                .forGetter((T cfg) -> cfg.chanceOfDirectionalSpread),
                        Codec
                                .floatRange(0.0F, 1.0F)
                                .fieldOf("chance_of_spread_radius2")
                                .orElse(0.5F)
                                .forGetter((T cfg) -> cfg.chanceOfSpreadRadius2),
                        Codec
                                .floatRange(0.0F, 1.0F)
                                .fieldOf("chance_of_spread_radius3")
                                .orElse(0.5F)
                                .forGetter((T cfg) -> cfg.chanceOfSpreadRadius3),
                        Codec
                                .intRange(1, 20)
                                .fieldOf("min_height")
                                .orElse(2)
                                .forGetter((T cfg) -> cfg.minHeight),
                        Codec
                                .intRange(1, 20)
                                .fieldOf("max_height")
                                .orElse(7)
                                .forGetter((T cfg) -> cfg.maxHeight),
                        Codec
                                .floatRange(0, 10)
                                .fieldOf("max_spread")
                                .orElse(2f)
                                .forGetter((T cfg) -> cfg.maxSpread),
                        Codec
                                .floatRange(0, 1)
                                .fieldOf("size_variation")
                                .orElse(0.7f)
                                .forGetter((T cfg) -> cfg.sizeVariation),
                        Codec
                                .floatRange(0, 1)
                                .fieldOf("floor_chance")
                                .orElse(0.5f)
                                .forGetter((T cfg) -> cfg.floorChance),
                        Codec
                                .BOOL
                                .fieldOf("grow_while_empty")
                                .orElse(false)
                                .forGetter((T cfg) -> cfg.growWhileFree),
                        IntProvider.codec(0, 64)
                                   .fieldOf("length")
                                   .orElse(UniformInt.of(0, 3))
                                   .forGetter(cfg -> cfg.spreadCount)
                )
                .apply(instance, instancer)
        );
    }

    public static class Builder<T extends ScatterFeatureConfig> {
        private BlockState clusterBlock;
        private BlockState tipBlock;
        private BlockState bottomBlock;
        private Optional<BlockState> baseState = Optional.empty();
        private float baseReplaceChance = 0;
        private float chanceOfDirectionalSpread = 0;
        private float chanceOfSpreadRadius2 = 0;
        private float chanceOfSpreadRadius3 = 0;
        private int minHeight = 2;
        private int maxHeight = 12;
        private float maxSpread = 0;
        private float sizeVariation = 0;
        private float floorChance = 0.5f;
        private boolean growWhileFree = false;
        public IntProvider spreadCount = ConstantInt.of(0);
        private final Instancer<T> instancer;

        public Builder(Instancer<T> instancer) {
            this.instancer = instancer;
        }

        public static <T extends ScatterFeatureConfig> Builder<T> start(Instancer<T> instancer) {
            return new Builder<>(instancer);
        }

        public Builder<T> block(Block b) {
            return block(b.defaultBlockState());
        }

        public Builder<T> singleBlock(Block b) {
            return block(b.defaultBlockState()).heightRange(1, 1).spread(0, 0);
        }

        public Builder<T> block(BlockState s) {
            this.clusterBlock = s;
            if (tipBlock == null) tipBlock = s;
            if (bottomBlock == null) bottomBlock = s;
            return this;
        }

        public Builder<T> tipBlock(BlockState s) {
            tipBlock = s;
            return this;
        }

        public Builder<T> bottomBlock(BlockState s) {
            bottomBlock = s;
            return this;
        }

        public Builder<T> heightRange(int min, int max) {
            minHeight = min;
            maxHeight = max;
            return this;
        }

        public Builder<T> growWhileFree() {
            growWhileFree = true;
            return this;
        }

        public Builder<T> minHeight(int h) {
            minHeight = h;
            return this;
        }

        public Builder<T> maxHeight(int h) {
            maxHeight = h;
            return this;
        }

        public Builder<T> generateBaseBlock(BlockState baseState) {
            return generateBaseBlock(baseState, 1, 0, 0, 0);
        }

        public Builder<T> generateBaseBlock(BlockState baseState, float baseReplaceChance) {
            return generateBaseBlock(baseState, baseReplaceChance, 0, 0, 0);
        }


        public Builder<T> generateBaseBlock(BlockState baseState,
                                            float chanceOfDirectionalSpread,
                                            float chanceOfSpreadRadius2,
                                            float chanceOfSpreadRadius3) {
            return generateBaseBlock(baseState,
                    1,
                    chanceOfDirectionalSpread,
                    chanceOfSpreadRadius2,
                    chanceOfSpreadRadius3);
        }

        public Builder<T> generateBaseBlock(BlockState baseState,
                                            float baseReplaceChance,
                                            float chanceOfDirectionalSpread,
                                            float chanceOfSpreadRadius2,
                                            float chanceOfSpreadRadius3) {
            if (this.baseState.isPresent() && this.baseReplaceChance == 0) {
                BCLib.LOGGER.error("Base generation was already selected.");
            }
            this.baseState = Optional.of(baseState);
            this.baseReplaceChance = baseReplaceChance;
            this.chanceOfDirectionalSpread = chanceOfDirectionalSpread;
            this.chanceOfSpreadRadius2 = chanceOfSpreadRadius2;
            this.chanceOfSpreadRadius3 = chanceOfSpreadRadius3;
            return this;
        }

        public Builder<T> spread(float maxSpread, float sizeVariation) {
            return spread(maxSpread, sizeVariation, ConstantInt.of((int) Math.min(16, 4 * maxSpread * maxSpread)));
        }

        public Builder<T> spread(float maxSpread, float sizeVariation, IntProvider spreadCount) {
            this.spreadCount = spreadCount; //
            this.maxSpread = maxSpread;
            this.sizeVariation = sizeVariation;
            return this;
        }

        public Builder<T> floorChance(float chance) {
            this.floorChance = chance;
            return this;
        }

        public Builder<T> onFloor() {
            this.floorChance = 1;
            return this;
        }

        public Builder<T> onCeil() {
            this.floorChance = 0;
            return this;
        }

        public T build() {
            return instancer.apply(
                    this.clusterBlock,
                    Optional.of(this.tipBlock),
                    Optional.of(this.bottomBlock),
                    this.baseState,
                    this.baseReplaceChance,
                    this.chanceOfDirectionalSpread,
                    this.chanceOfSpreadRadius2,
                    this.chanceOfSpreadRadius3,
                    this.minHeight,
                    this.maxHeight,
                    this.maxSpread,
                    this.sizeVariation,
                    this.floorChance,
                    this.growWhileFree,
                    this.spreadCount
            );
        }
    }

    public static class OnSolid extends ScatterFeatureConfig {
        public static final Codec<OnSolid> CODEC = buildCodec(OnSolid::new);

        public OnSolid(BlockState clusterBlock,
                       Optional<BlockState> tipBlock,
                       Optional<BlockState> bottomBlock,
                       Optional<BlockState> baseState,
                       float baseReplaceChance,
                       float chanceOfDirectionalSpread,
                       float chanceOfSpreadRadius2,
                       float chanceOfSpreadRadius3,
                       int minHeight,
                       int maxHeight,
                       float maxSpread,
                       float sizeVariation,
                       float floorChance,
                       boolean growWhileFree,
                       IntProvider spreadCount) {
            super(clusterBlock,
                    tipBlock,
                    bottomBlock,
                    baseState,
                    baseReplaceChance,
                    chanceOfDirectionalSpread,
                    chanceOfSpreadRadius2,
                    chanceOfSpreadRadius3,
                    minHeight,
                    maxHeight,
                    maxSpread,
                    sizeVariation,
                    floorChance,
                    growWhileFree,
                    spreadCount);
        }


        public static Builder<OnSolid> startOnSolid() {
            return Builder.start(OnSolid::new);
        }


        @Override
        public boolean isValidBase(BlockState state) {
            return BlocksHelper.isTerrain(state);
        }

        @Override
        public BlockState createBlock(int height, int maxHeight, RandomSource random) {
            if (height == 0) return this.bottomBlock;
            return height == maxHeight
                    ? this.tipBlock
                    : this.clusterBlock;
        }
    }

}
