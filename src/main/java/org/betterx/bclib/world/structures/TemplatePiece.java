package org.betterx.bclib.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import org.betterx.bclib.BCLib;

public class TemplatePiece extends StructurePiece {
    public static final StructurePieceType INSTANCE = register("template_piece", TemplatePiece::new);

    private static StructurePieceType register(String id, StructurePieceType pieceType) {
        return Registry.register(Registry.STRUCTURE_PIECE, BCLib.makeID(id), pieceType);
    }

    public final StructureWorldNBT structure;
    public final BlockPos pos;
    public final Rotation rot;
    public final Mirror mir;

    protected TemplatePiece(StructureWorldNBT structure,
                            BlockPos pos,
                            Rotation rot,
                            Mirror mir) {
        super(INSTANCE, 0, structure.boundingBox(rot, pos));
        this.structure = structure;
        this.rot = rot;
        this.mir = mir;
        this.pos = pos;
    }

    public TemplatePiece(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
        super(INSTANCE, compoundTag);

        ResourceLocation location = new ResourceLocation(compoundTag.getString("L"));
        int offsetY = compoundTag.getInt("OY");
        StructurePlacementType type = StructurePlacementType.valueOf(compoundTag.getString("T"));
        this.structure = new StructureWorldNBT(location, offsetY, type);

        this.rot = Rotation.valueOf(compoundTag.getString("Rot"));
        this.mir = Mirror.valueOf(compoundTag.getString("Mir"));
        this.pos = new BlockPos(
                compoundTag.getInt("PX"),
                compoundTag.getInt("PY"),
                compoundTag.getInt("PZ")
        );
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext,
                                         CompoundTag tag) {

        tag.putString("L", structure.location.toString());
        tag.putInt("OY", structure.offsetY);
        tag.putString("T", structure.type.getSerializedName());
        tag.putString("Rot", this.rot.name());
        tag.putString("Mir", this.mir.name());
        tag.putInt("PX", this.pos.getX());
        tag.putInt("PY", this.pos.getY());
        tag.putInt("PZ", this.pos.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel worldGenLevel,
                            StructureManager structureManager,
                            ChunkGenerator chunkGenerator,
                            RandomSource randomSource,
                            BoundingBox boundingBox,
                            ChunkPos chunkPos,
                            BlockPos blockPos) {
        structure.generateInRandomOrientation(worldGenLevel, blockPos, randomSource);
    }
}
