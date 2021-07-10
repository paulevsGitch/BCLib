package ru.bclib.world.structures;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.fabricmc.fabric.mixin.structure.FlatChunkGeneratorConfigAccessor;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;

public class BCLStructureFeature {
	private static final Random RANDOM = new Random(354);
	private final StructureFeature<NoneFeatureConfiguration> structure;
	private final ConfiguredStructureFeature<?, ?> featureConfigured;
	private final GenerationStep.Decoration featureStep;
	
	public BCLStructureFeature(ResourceLocation id, StructureFeature<NoneFeatureConfiguration> structure, GenerationStep.Decoration step, int spacing, int separation) {
		this.featureStep = step;
		this.structure = FabricStructureBuilder.create(id, structure).step(step).defaultConfig(spacing, separation, RANDOM.nextInt(8192)).register();
		this.featureConfigured = this.structure.configured(NoneFeatureConfiguration.NONE);
		BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, id, this.featureConfigured);
		FlatChunkGeneratorConfigAccessor.getStructureToFeatures().put(this.structure, this.featureConfigured);
	}
	
	public StructureFeature<NoneFeatureConfiguration> getStructure() {
		return structure;
	}
	
	public ConfiguredStructureFeature<?, ?> getFeatureConfigured() {
		return featureConfigured;
	}
	
	public GenerationStep.Decoration getFeatureStep() {
		return featureStep;
	}
}
