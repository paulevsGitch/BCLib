package org.betterx.bclib.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public abstract class TemplateStructure extends Structure {
    public static <T extends TemplateStructure> Codec<T> simpleCodec(Function4 instancer) {
        return RecordCodecBuilder.create((instance) -> instance
                .group(
                        Structure.settingsCodec(instance),
                        ResourceLocation.CODEC
                                .fieldOf("location")
                                .forGetter((T cfg) -> cfg.structure.location),

                        Codec
                                .INT
                                .fieldOf("offset_y")
                                .orElse(0)
                                .forGetter((T cfg) -> cfg.structure.offsetY),

                        StructurePlacementType.CODEC
                                .fieldOf("placement")
                                .orElse(StructurePlacementType.FLOOR)
                                .forGetter((T cfg) -> cfg.structure.type)
                )
                .apply(instance, instancer)
        );
    }

    public final StructureWorldNBT structure;

    protected TemplateStructure(StructureSettings structureSettings,
                                ResourceLocation location,
                                int offsetY,
                                StructurePlacementType type) {
        super(structureSettings);
        structure = StructureWorldNBT.create(location, offsetY, type);
    }


    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        ChunkPos chunkPos = ctx.chunkPos();
        final int x = chunkPos.getMiddleBlockX();
        final int z = chunkPos.getMiddleBlockZ();
        int y = ctx
                .chunkGenerator()
                .getFirstOccupiedHeight(x,
                        z,
                        Heightmap.Types.WORLD_SURFACE_WG,
                        ctx.heightAccessor(),
                        ctx.randomState());

        BlockPos centerPos = new BlockPos(x, y, z);
        Rotation rotation = Rotation.getRandom(ctx.random());
        BoundingBox bb = structure.boundingBox(rotation, centerPos);
        // if (!structure.canGenerate(ctx.chunkGenerator()., centerPos))
        return Optional.of(new GenerationStub(centerPos,
                structurePiecesBuilder -> this.generatePieces(structurePiecesBuilder, centerPos, rotation, ctx)));

    }

    private void generatePieces(StructurePiecesBuilder structurePiecesBuilder,
                                BlockPos centerPos,
                                Rotation rotation,
                                Structure.GenerationContext generationContext) {
        WorldgenRandom worldgenRandom = generationContext.random();

        Mirror mirror = Mirror.values()[worldgenRandom.nextInt(3)];

        structurePiecesBuilder.addPiece(new TemplatePiece(structure, centerPos, rotation, mirror));
    }


}
