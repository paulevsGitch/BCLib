package ru.bclib.api.surface.rules;

import ru.bclib.interfaces.NumericProvider;
import ru.bclib.mixin.common.SurfaceRulesContextAccessor;
import ru.bclib.util.MHelper;

public record RandomIntProvider(int range) implements NumericProvider {
	@Override
	public int getNumber(SurfaceRulesContextAccessor context) {
		return MHelper.RANDOM.nextInt(range);
	}
}
