package org.betterx.bclib.presets;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.tag.TagAPI;
import org.betterx.bclib.api.tag.TagType;

public class WorldPresets {
    public static TagType.Simple<WorldPreset> WORLD_PRESETS =
            TagAPI.registerType(BuiltinRegistries.WORLD_PRESET, "tag/worldgen/world_preset");

    public static ResourceKey<WorldPreset> BCL_WORLD = register(BCLib.makeID("normal"));

    public static ResourceKey<WorldPreset> register(ResourceLocation loc) {
        ResourceKey<WorldPreset> key = ResourceKey.create(Registry.WORLD_PRESET_REGISTRY,
                                                          loc);
        WORLD_PRESETS.addUntyped(WorldPresetTags.NORMAL, key.location());
        return key;
    }

    public static void register() {
    }
}
