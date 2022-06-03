package org.betterx.bclib.world.surface;

import net.minecraft.core.Registry;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.surface.rules.SurfaceNoiseCondition;
import org.betterx.bclib.mixin.common.SurfaceRulesContextAccessor;
import org.betterx.bclib.noise.OpenSimplexNoise;
import org.betterx.bclib.util.MHelper;

public class DoubleBlockSurfaceNoiseCondition extends SurfaceNoiseCondition {
    public static final DoubleBlockSurfaceNoiseCondition CONDITION = new DoubleBlockSurfaceNoiseCondition(0);
    private static final OpenSimplexNoise NOISE = new OpenSimplexNoise(4141);
    public static final Codec<DoubleBlockSurfaceNoiseCondition> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.DOUBLE.fieldOf("threshold").orElse(0.0).forGetter(o -> o.threshold)
            )
            .apply(instance, DoubleBlockSurfaceNoiseCondition::new));

    public static final KeyDispatchDataCodec<DoubleBlockSurfaceNoiseCondition> KEY_CODEC = KeyDispatchDataCodec.of(
            CODEC);
    private final double threshold;

    public DoubleBlockSurfaceNoiseCondition(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
        return KEY_CODEC;
    }

    private static int lastX = Integer.MIN_VALUE;
    private static int lastZ = Integer.MIN_VALUE;
    private static double lastValue = 0;

    @Override
    public boolean test(SurfaceRulesContextAccessor context) {
        final int x = context.getBlockX();
        final int z = context.getBlockZ();
        if (lastX == x && lastZ == z) return lastValue > threshold;

        double value = NOISE.eval(x * 0.1, z * 0.1) + MHelper.randRange(-0.4, 0.4, MHelper.RANDOM_SOURCE);

        lastX = x;
        lastZ = z;
        lastValue = value;
        return value > threshold;
    }

    static {
        Registry.register(Registry.CONDITION,
                BCLib.makeID("doubleblock_surface"),
                DoubleBlockSurfaceNoiseCondition.CODEC);
    }
}
