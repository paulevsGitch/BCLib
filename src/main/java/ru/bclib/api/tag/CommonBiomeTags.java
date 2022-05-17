package ru.bclib.api.tag;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class CommonBiomeTags {
    public static final TagKey<Biome> IN_NETHER = TagAPI.makeCommonBiomeTag("in_nether");
}
