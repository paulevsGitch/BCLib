package ru.bclib.mixin.common;

import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.Supplier;

@Mixin(BiomeGenerationSettings.class)
public interface BiomeGenerationSettingsAccessor {
	@Accessor("features")
	List<List<Supplier<PlacedFeature>>> bcl_getFeatures();
	
	@Accessor("features")
	@Mutable
	void bcl_setFeatures(List<List<Supplier<PlacedFeature>>> value);
}
