package ru.bclib.interfaces;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import ru.bclib.BCLib;
import ru.bclib.mixin.common.SurfaceRulesContextAccessor;

import java.util.function.Function;

public interface NumericProvider {
	ResourceKey<Registry<Codec<? extends NumericProvider>>> NUMERIC_PROVIDER_REGISTRY = ResourceKey.createRegistryKey(BCLib.makeID("worldgen/numeric_provider"));
	Registry<Codec<? extends NumericProvider>> NUMERIC_PROVIDER = new MappedRegistry<>(NUMERIC_PROVIDER_REGISTRY, Lifecycle.experimental());
	Codec<NumericProvider> CODEC = NUMERIC_PROVIDER.byNameCodec().dispatch(NumericProvider::pcodec, Function.identity());
	int getNumber(SurfaceRulesContextAccessor context);

	Codec<? extends NumericProvider> pcodec();
}
