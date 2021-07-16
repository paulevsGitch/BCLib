package ru.bclib.mixin.common;

import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.data.worldgen.Features$Decorators")
public interface FeatureDecoratorsAccessor {
	@Accessor("HEIGHTMAP_SQUARE")
	ConfiguredDecorator<?> bclib_getHeightmapSquare();
}
