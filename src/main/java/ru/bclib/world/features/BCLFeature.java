package ru.bclib.world.features;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Map.Entry;
import java.util.Optional;

public class BCLFeature {
	private Holder<PlacedFeature> placedFeature;
	private Decoration featureStep;
	private Feature<?> feature;
	
	public BCLFeature(ResourceLocation id, Feature<?> feature, Decoration featureStep, Holder<PlacedFeature> placedFeature) {
		this.placedFeature = placedFeature;
		this.featureStep = featureStep;
		this.feature = feature;
		
		if (!BuiltinRegistries.PLACED_FEATURE.containsKey(id)) {
			Registry.register(BuiltinRegistries.PLACED_FEATURE, id, placedFeature.value());
		}
		if (!Registry.FEATURE.containsKey(id) && !containsObj(Registry.FEATURE, feature)) {
			Registry.register(Registry.FEATURE, id, feature);
		}
	}
	
	private static <E> boolean containsObj(Registry<E> registry, E obj) {
		Optional<Entry<ResourceKey<E>, E>> optional = registry
			.entrySet()
			.stream()
			.filter(entry -> entry.getValue() == obj)
			.findAny();
		return optional.isPresent();
	}
	
	/**
	 * Get raw feature.
	 * @return {@link Feature}.
	 */
	public Feature<?> getFeature() {
		return feature;
	}
	
	/**
	 * Get configured feature.
	 * @return {@link PlacedFeature}.
	 */
	public Holder<PlacedFeature> getPlacedFeature() {
		return placedFeature;
	}
	
	/**
	 * Get feature decoration step.
	 * @return {@link Decoration}.
	 */
	public Decoration getDecoration() {
		return featureStep;
	}
}
