package ru.bclib.mixin.common;

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Biome.class)
public interface BiomeAccessor {
    @Accessor("biomeCategory")
    @Mutable
    Biome.BiomeCategory bclib_getBiomeCategory();
}
