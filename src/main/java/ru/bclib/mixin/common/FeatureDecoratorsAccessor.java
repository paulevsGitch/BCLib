package ru.bclib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.data.worldgen.Features$Decorators")
public interface FeatureDecoratorsAccessor {
	/*@Accessor("HEIGHTMAP_SQUARE")
	ConfiguredDecorator<?> bclib_getHeightmapSquare();*/
}
