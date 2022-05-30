package org.betterx.bclib.world.features;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.betterx.bclib.world.structures.StructurePlacementType;
import org.betterx.bclib.world.structures.StructureWorldNBT;

public class TemplateFeatureConfig implements FeatureConfiguration {
    public static final Codec<TemplateFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(ResourceLocation.CODEC
                            .fieldOf("location")
                            .forGetter((TemplateFeatureConfig cfg) -> cfg.structure.location),

                    Codec
                            .INT
                            .fieldOf("offset_y")
                            .orElse(0)
                            .forGetter((TemplateFeatureConfig cfg) -> cfg.structure.offsetY),

                    StructurePlacementType.CODEC
                            .fieldOf("placement")
                            .orElse(StructurePlacementType.FLOOR)
                            .forGetter((TemplateFeatureConfig cfg) -> cfg.structure.type)
            )
            .apply(instance, TemplateFeatureConfig::new)
    );

    public final StructureWorldNBT structure;

    public TemplateFeatureConfig(ResourceLocation location, int offsetY, StructurePlacementType type) {
        structure = StructureWorldNBT.create(location, offsetY, type);
    }
}
