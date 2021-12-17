package ru.bclib.mixin.common;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSource.StepFeatureData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.BCLib;
import ru.bclib.api.biomes.BiomeAPI;
import ru.bclib.interfaces.BiomeSourceAccessor;

import java.util.List;
import java.util.Set;

@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin implements BiomeSourceAccessor {
	@Shadow protected abstract List<StepFeatureData> buildFeaturesPerStep(List<Biome> list, boolean bl);
	
	@Shadow public abstract Set<Biome> possibleBiomes();
	
	@Mutable @Shadow @Final private List<StepFeatureData> featuresPerStep;
	
	public void bclRebuildFeatures(){
		BCLib.LOGGER.info("Rebuilding features in BiomeSource " + this.getClass());
		featuresPerStep = buildFeaturesPerStep(this.possibleBiomes().stream().toList(), true);
	}
	
	@Inject(method="<init>(Ljava/util/List;)V", at=@At(value="INVOKE", shift = Shift.AFTER, target="Lnet/minecraft/world/level/biome/BiomeSource;buildFeaturesPerStep(Ljava/util/List;Z)Ljava/util/List;"))
	private void bcl_Init(List list, CallbackInfo ci){
		BiomeAPI.registerBiomeSource((BiomeSource)(Object)this);
	}
}
