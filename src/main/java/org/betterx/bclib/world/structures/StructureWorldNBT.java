package org.betterx.bclib.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import com.google.common.collect.Maps;
import org.betterx.bclib.util.BlocksHelper;

import java.util.Map;

public class StructureWorldNBT extends StructureNBT {
    public final StructurePlacementType type;
    public final int offsetY;

    protected StructureWorldNBT(ResourceLocation location, int offsetY, StructurePlacementType type) {
        super(location);
        this.offsetY = offsetY;
        this.type = type;
    }

    private static final Map<String, StructureWorldNBT> READER_CACHE = Maps.newHashMap();

    public static StructureWorldNBT create(ResourceLocation location, int offsetY, StructurePlacementType type) {
        String key = location.toString() + "::" + offsetY + "::" + type.getSerializedName();
        return READER_CACHE.computeIfAbsent(key, r -> new StructureWorldNBT(location, offsetY, type));
    }


    public boolean generateInRandomOrientation(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        randomRM(random);
        return generate(level, pos);
    }

    public boolean generate(ServerLevelAccessor level, BlockPos pos) {
        if (canGenerate(level, pos)) {
            return generateCentered(level, pos.above(offsetY));
        }
        return false;
    }

    protected boolean canGenerate(LevelAccessor level, BlockPos pos) {
        if (type == StructurePlacementType.FLOOR)
            return canGenerateFloor(level, pos);
        else if (type == StructurePlacementType.LAVA)
            return canGenerateLava(level, pos);
        else if (type == StructurePlacementType.UNDER)
            return canGenerateUnder(level, pos);
        else if (type == StructurePlacementType.CEIL)
            return canGenerateCeil(level, pos);
        else
            return false;
    }

    private boolean containsBedrock(LevelAccessor level, BlockPos startPos) {
        for (int i = 0; i < this.structure.getSize().getY(); i += 2) {
            if (level.getBlockState(startPos.above(i)).is(Blocks.BEDROCK)) {
                return true;
            }
        }
        return false;
    }

    protected boolean canGenerateFloor(LevelAccessor world, BlockPos pos) {
        if (containsBedrock(world, pos)) return false;

        return getAirFraction(world, pos) > 0.6 && getAirFractionFoundation(world, pos) < 0.5;
    }

    protected boolean canGenerateLava(LevelAccessor world, BlockPos pos) {
        if (containsBedrock(world, pos)) return false;

        return getLavaFractionFoundation(world, pos) > 0.9 && getAirFraction(world, pos) > 0.9;
    }

    protected boolean canGenerateUnder(LevelAccessor world, BlockPos pos) {
        if (containsBedrock(world, pos)) return false;

        return getAirFraction(world, pos) < 0.2;
    }

    protected boolean canGenerateCeil(LevelAccessor world, BlockPos pos) {
        if (containsBedrock(world, pos)) return false;

        return getAirFractionBottom(world, pos) > 0.8 && getAirFraction(world, pos) < 0.6;
    }

    public BoundingBox boundingBox(Rotation r, BlockPos p) {
        MutableBlockPos size = new MutableBlockPos().set(new BlockPos(structure.getSize()).rotate(rotation));
        size.setX(Math.abs(size.getX()) >> 1);
        size.setY(Math.abs(size.getY()) >> 1);
        size.setZ(Math.abs(size.getZ()) >> 1);
        return new BoundingBox(
                p.getX() - size.getX(),
                p.getY() - size.getY(),
                p.getZ() - size.getZ(),
                p.getX() + size.getX(),
                p.getY() + size.getY(),
                p.getZ() + size.getZ()
        );
    }

    protected float getAirFraction(LevelAccessor world, BlockPos pos) {
        final MutableBlockPos POS = new MutableBlockPos();
        int airCount = 0;

        MutableBlockPos size = new MutableBlockPos().set(new BlockPos(structure.getSize()).rotate(rotation));
        size.setX(Math.abs(size.getX()) >> 1);
        size.setZ(Math.abs(size.getZ()) >> 1);

        BlockPos start = pos.offset(-size.getX(), 0, -size.getZ());
        BlockPos end = pos.offset(size.getX(), size.getY() + offsetY, size.getZ());
        int count = 0;

        for (int x = start.getX(); x <= end.getX(); x++) {
            POS.setX(x);
            for (int y = start.getY(); y <= end.getY(); y++) {
                POS.setY(y);
                for (int z = start.getZ(); z <= end.getZ(); z++) {
                    POS.setZ(z);
                    if (world.isEmptyBlock(POS))
                        airCount++;
                    count++;
                }
            }
        }

        return (float) airCount / count;
    }

    private float getLavaFractionFoundation(LevelAccessor world, BlockPos pos) {
        final MutableBlockPos POS = new MutableBlockPos();
        int lavaCount = 0;

        MutableBlockPos size = new MutableBlockPos().set(new BlockPos(structure.getSize()).rotate(rotation));
        size.setX(Math.abs(size.getX()) >> 1);
        size.setZ(Math.abs(size.getZ()) >> 1);

        BlockPos start = pos.offset(-(size.getX()), 0, -(size.getZ()));
        BlockPos end = pos.offset(size.getX(), 0, size.getZ());
        int count = 0;

        POS.setY(pos.getY() - 1);
        for (int x = start.getX(); x <= end.getX(); x++) {
            POS.setX(x);
            for (int z = start.getZ(); z <= end.getZ(); z++) {
                POS.setZ(z);

                if (BlocksHelper.isLava(world.getBlockState(POS)))
                    lavaCount++;
                count++;
            }
        }

        return (float) lavaCount / count;
    }

    private float getAirFractionFoundation(LevelAccessor world, BlockPos pos) {
        final MutableBlockPos POS = new MutableBlockPos();
        int airCount = 0;

        MutableBlockPos size = new MutableBlockPos().set(new BlockPos(structure.getSize()).rotate(rotation));
        size.setX(Math.abs(size.getX()) >> 1);
        size.setZ(Math.abs(size.getZ()) >> 1);

        BlockPos start = pos.offset(-(size.getX()), -1, -(size.getZ()));
        BlockPos end = pos.offset(size.getX(), 0, size.getZ());
        int count = 0;

        for (int x = start.getX(); x <= end.getX(); x++) {
            POS.setX(x);
            for (int y = start.getY(); y <= end.getY(); y++) {
                POS.setY(y);
                for (int z = start.getZ(); z <= end.getZ(); z++) {
                    POS.setZ(z);
                    if (world.getBlockState(POS).getMaterial().isReplaceable())
                        airCount++;
                    count++;
                }
            }
        }

        return (float) airCount / count;
    }

    private float getAirFractionBottom(LevelAccessor world, BlockPos pos) {
        final MutableBlockPos POS = new MutableBlockPos();
        int airCount = 0;

        MutableBlockPos size = new MutableBlockPos().set(new BlockPos(structure.getSize()).rotate(rotation));
        size.setX(Math.abs(size.getX()));
        size.setZ(Math.abs(size.getZ()));

        float y1 = Math.min(offsetY, 0);
        float y2 = Math.max(offsetY, 0);
        BlockPos start = pos.offset(-(size.getX() >> 1), y1, -(size.getZ() >> 1));
        BlockPos end = pos.offset(size.getX() >> 1, y2, size.getZ() >> 1);
        int count = 0;

        for (int x = start.getX(); x <= end.getX(); x++) {
            POS.setX(x);
            for (int y = start.getY(); y <= end.getY(); y++) {
                POS.setY(y);
                for (int z = start.getZ(); z <= end.getZ(); z++) {
                    POS.setZ(z);
                    if (world.getBlockState(POS).getMaterial().isReplaceable())
                        airCount++;
                    count++;
                }
            }
        }

        return (float) airCount / count;
    }

    public boolean loaded() {
        return structure != null;
    }
}
