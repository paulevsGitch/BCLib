package org.betterx.bclib.world.features.placement;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import org.betterx.bclib.BCLib;

public class PlacementModifiers {
    public static final PlacementModifierType<IsEmptyAboveSampledFilter> IS_EMPTY_ABOVE_SAMPLED_FILTER = register(
            "is_empty_above_sampled_filter",
            IsEmptyAboveSampledFilter.CODEC);

    public static final PlacementModifierType<MinEmptyFilter> MIN_EMPTY_FILTER = register(
            "min_empty_filter",
            MinEmptyFilter.CODEC);

    public static final PlacementModifierType<FindSolidInDirection> SOLID_IN_DIR = register(
            "solid_in_dir",
            FindSolidInDirection.CODEC);

    public static final PlacementModifierType<Stencil> STENCIL = register(
            "stencil",
            Stencil.CODEC);

    public static final PlacementModifierType<IsBasin> IS_BASIN = register(
            "is_basin",
            IsBasin.CODEC);

    public static final PlacementModifierType<Offset> OFFSET = register(
            "offset",
            Offset.CODEC);

    public static final PlacementModifierType<Extend> EXTEND = register(
            "extend",
            Extend.CODEC);


    private static <P extends PlacementModifier> PlacementModifierType<P> register(String path, Codec<P> codec) {
        return register(BCLib.makeID(path), codec);
    }

    public static <P extends PlacementModifier> PlacementModifierType<P> register(ResourceLocation location,
                                                                                  Codec<P> codec) {
        return Registry.register(Registry.PLACEMENT_MODIFIERS, location, () -> codec);
    }

    public static void ensureStaticInitialization() {

    }
}

