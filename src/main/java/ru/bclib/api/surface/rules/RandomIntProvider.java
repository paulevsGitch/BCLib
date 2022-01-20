package ru.bclib.api.surface.rules;

import com.mojang.serialization.Codec;
import ru.bclib.interfaces.NumericProvider;
import ru.bclib.mixin.common.SurfaceRulesContextAccessor;
import ru.bclib.util.MHelper;

public record RandomIntProvider(int range) implements NumericProvider {
	public static final Codec<RandomIntProvider> CODEC = Codec.INT.fieldOf("nethrangeer_noise").xmap(RandomIntProvider::new, obj -> obj.range).codec();
	@Override
	public int getNumber(SurfaceRulesContextAccessor context) {
		return MHelper.RANDOM.nextInt(range);
	}

	@Override
	public Codec<? extends NumericProvider> pcodec() {
		return CODEC;
	}
}
