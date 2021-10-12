package ru.bclib.interfaces;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IStructureFeatures {
    public void bclib_registerStructure(Consumer<BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> callback);
}
