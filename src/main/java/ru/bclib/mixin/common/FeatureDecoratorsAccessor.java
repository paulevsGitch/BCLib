package ru.bclib.mixin.common;

import net.minecraft.data.worldgen.Features;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(targets = "net.minecraft.data.worldgen.Features$Decorators")
public interface FeatureDecoratorsAccessor {
	@Accessor("HEIGHTMAP_SQUARE")
	ConfiguredDecorator<?> bcl_getHeightmapSquare();
}
