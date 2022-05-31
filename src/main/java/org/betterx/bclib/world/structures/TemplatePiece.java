package org.betterx.bclib.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.util.BlocksHelper;

public class TemplatePiece extends TemplateStructurePiece {
    public static final StructurePieceType INSTANCE = setTemplatePieceId(TemplatePiece::new,
                                                                         "template_piece");


    private static StructurePieceType setFullContextPieceId(StructurePieceType structurePieceType, String id) {
        return Registry.register(Registry.STRUCTURE_PIECE, BCLib.makeID(id), structurePieceType);
    }

    private static StructurePieceType setTemplatePieceId(StructurePieceType.StructureTemplateType structureTemplateType,
                                                         String string) {
        return setFullContextPieceId(structureTemplateType, string);
    }


    public static void ensureStaticInitialization() {
    }


    public TemplatePiece(StructureTemplateManager structureTemplateManager,
                         ResourceLocation resourceLocation,
                         BlockPos centerPos,
                         Rotation rotation,
                         Mirror mirror,
                         BlockPos halfSize) {
        super(INSTANCE,
              0,
              structureTemplateManager,
              resourceLocation,
              resourceLocation.toString(),
              makeSettings(rotation, mirror, halfSize),
              shiftPos(halfSize, centerPos));
    }

    public TemplatePiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
        super(INSTANCE,
              compoundTag,
              structureTemplateManager,
              (ResourceLocation resourceLocation) -> makeSettings(compoundTag));
    }


    @Override
    public void postProcess(WorldGenLevel level,
                            StructureManager structureManager,
                            ChunkGenerator chunkGenerator,
                            RandomSource randomSource,
                            BoundingBox boundingBox,
                            ChunkPos chunkPos,
                            BlockPos blockPos) {
        super.postProcess(level,
                          structureManager,
                          chunkGenerator,
                          randomSource,
                          boundingBox,
                          chunkPos,
                          blockPos);

        BlocksHelper.setWithoutUpdate(level, new BlockPos(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ()),
                                      Blocks.YELLOW_CONCRETE);
        BlocksHelper.setWithoutUpdate(level, new BlockPos(boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ()),
                                      Blocks.LIGHT_BLUE_CONCRETE);
        BlocksHelper.setWithoutUpdate(level, boundingBox.getCenter(),
                                      Blocks.LIME_CONCRETE);
        BlocksHelper.setWithoutUpdate(level, blockPos,
                                      Blocks.ORANGE_CONCRETE);
    }

    private static BlockPos shiftPos(BlockPos halfSize,
                                     BlockPos pos) {
        return pos.offset(-(2 * halfSize.getX()), 0, -(2 * halfSize.getZ()));
        //return pos;
    }

    private static StructurePlaceSettings makeSettings(CompoundTag compoundTag) {
        return makeSettings(
                Rotation.valueOf(compoundTag.getString("R")),
                Mirror.valueOf(compoundTag.getString("M")),
                new BlockPos(compoundTag.getInt("RX"), compoundTag.getInt("RY"), compoundTag.getInt("RZ")));

    }

    private static StructurePlaceSettings makeSettings(Rotation rotation, Mirror mirror, BlockPos halfSize) {
        return new StructurePlaceSettings().setRotation(rotation)
                                           .setMirror(mirror)
                                           .setRotationPivot(halfSize)
                                           .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext,
                                         CompoundTag tag) {
        super.addAdditionalSaveData(structurePieceSerializationContext, tag);
        tag.putString("R", this.placeSettings.getRotation().name());
        tag.putString("M", this.placeSettings.getMirror().name());
        tag.putInt("RX", this.placeSettings.getRotationPivot().getX());
        tag.putInt("RY", this.placeSettings.getRotationPivot().getY());
        tag.putInt("RZ", this.placeSettings.getRotationPivot().getZ());
    }

    @Override
    protected void handleDataMarker(String string,
                                    BlockPos blockPos,
                                    ServerLevelAccessor serverLevelAccessor,
                                    RandomSource randomSource,
                                    BoundingBox boundingBox) {

    }
}

class TemplatePiece2 extends StructurePiece {
    public static final StructurePieceType INSTANCE = register("template_piece_bcl", TemplatePiece2::new);

    private static StructurePieceType register(String id, StructurePieceType pieceType) {
        return Registry.register(Registry.STRUCTURE_PIECE, BCLib.makeID(id), pieceType);
    }

    public static void ensureStaticInitialization() {
    }

    public final StructureWorldNBT structure;
    public final BlockPos pos;
    public final Rotation rot;
    public final Mirror mir;

    protected TemplatePiece2(StructureWorldNBT structure,
                             BlockPos pos,
                             Rotation rot,
                             Mirror mir) {
        super(INSTANCE, 0, structure.boundingBox(rot, pos));
        this.structure = structure;
        this.rot = rot;
        this.mir = mir;
        this.pos = pos;
    }

    public TemplatePiece2(StructurePieceSerializationContext ctx, CompoundTag compoundTag) {
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
        tag.putString("T", structure.type.name());
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
        structure.generate(worldGenLevel, this.pos, this.rot, this.mir);
    }
}
