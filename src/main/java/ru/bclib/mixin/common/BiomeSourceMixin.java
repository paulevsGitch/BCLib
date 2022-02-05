package ru.bclib.mixin.common;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSource.StepFeatureData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import ru.bclib.BCLib;
import ru.bclib.interfaces.BiomeSourceAccessor;

import java.util.List;
import java.util.Set;

@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin implements BiomeSourceAccessor {
	@Shadow protected abstract List<StepFeatureData> buildFeaturesPerStep(List<Biome> list, boolean bl);
	
	@Shadow public abstract Set<Biome> possibleBiomes();
	
	@Mutable @Shadow @Final private List<StepFeatureData> featuresPerStep;
	
	public void bclRebuildFeatures(){
		BCLib.LOGGER.info("Rebuilding features in BiomeSource " + this);
		featuresPerStep = buildFeaturesPerStep(this.possibleBiomes().stream().toList(), true);
	}
}
