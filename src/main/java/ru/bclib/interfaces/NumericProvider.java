package ru.bclib.interfaces;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.SurfaceRules;
import ru.bclib.mixin.common.SurfaceRulesContextAccessor;

import java.util.function.Function;

public interface NumericProvider {
	int getNumber(SurfaceRulesContextAccessor context);

	Codec<? extends NumericProvider> pcodec();
}
