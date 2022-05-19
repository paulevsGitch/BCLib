package org.betterx.bclib.presets;

import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.tag.TagAPI;
import org.betterx.bclib.api.tag.TagType;
import org.betterx.bclib.gui.modmenu.MainScreen;

import java.util.Optional;

public class WorldPresets {
    public static TagType.Simple<WorldPreset> WORLD_PRESETS =
            TagAPI.registerType(BuiltinRegistries.WORLD_PRESET, "tags/worldgen/world_preset");

    public static ResourceKey<WorldPreset> BCL_WORLD = register(BCLib.makeID("normal"));

    public static ResourceKey<WorldPreset> register(ResourceLocation loc) {
        ResourceKey<WorldPreset> key = ResourceKey.create(Registry.WORLD_PRESET_REGISTRY,
                                                          loc);
        WORLD_PRESETS.addUntyped(WorldPresetTags.NORMAL, key.location());

        PresetEditor.EDITORS.put(Optional.of(key),
                                 (createWorldScreen, worldCreationContext) -> new MainScreen(createWorldScreen));

        return key;
    }

    public static void register() {
    }
}
