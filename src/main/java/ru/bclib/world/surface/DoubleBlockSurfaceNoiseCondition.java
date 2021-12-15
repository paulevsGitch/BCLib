package ru.bclib.world.surface;

import ru.bclib.api.surface.rules.SurfaceNoiseCondition;
import ru.bclib.mixin.common.SurfaceRulesContextAccessor;
import ru.bclib.noise.OpenSimplexNoise;
import ru.bclib.util.MHelper;

public class DoubleBlockSurfaceNoiseCondition extends SurfaceNoiseCondition {
	public static final DoubleBlockSurfaceNoiseCondition CONDITION = new DoubleBlockSurfaceNoiseCondition(0);
	private static final OpenSimplexNoise NOISE = new OpenSimplexNoise(4141);

	private final double threshold;
	public DoubleBlockSurfaceNoiseCondition(double threshold){
		this.threshold = threshold;
	}

	private static int lastX = Integer.MIN_VALUE;
	private static int lastZ = Integer.MIN_VALUE;
	private static double lastValue = 0;

	@Override
	public boolean test(SurfaceRulesContextAccessor context) {
		final int x = context.getBlockX();
		final int z = context.getBlockZ();
		if (lastX==x && lastZ==z) return lastValue > threshold;

		double value = NOISE.eval(x * 0.1, z * 0.1) + MHelper.randRange(-0.4, 0.4, MHelper.RANDOM);

		lastX=x;
		lastZ=z;
		lastValue=value;
		return value > threshold;
	}
}
