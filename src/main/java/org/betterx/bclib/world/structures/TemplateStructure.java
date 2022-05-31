package org.betterx.bclib.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class TemplateStructure extends Structure {
    protected final List<Config> configs;

    public static <T extends TemplateStructure> Codec<T> simpleCodec(BiFunction instancer) {
        return RecordCodecBuilder.create((instance) -> instance
                                                 .group(
                                                         Structure.settingsCodec(instance),
                                                         ExtraCodecs.nonEmptyList(Config.CODEC.listOf())
                                                                    .fieldOf("configs")
                                                                    .forGetter((T ruinedPortalStructure) -> ruinedPortalStructure.configs)
                                                       )
                                                 .apply(instance, instancer)
                                        );
    }

    protected TemplateStructure(StructureSettings structureSettings,
                                ResourceLocation location,
                                int offsetY,
                                StructurePlacementType type,
                                float chance) {
        this(structureSettings, List.of(new Config(location, offsetY, type, chance)));
    }

    protected TemplateStructure(StructureSettings structureSettings,
                                List<Config> configs) {
        super(structureSettings);
        this.configs = configs;
    }

    protected Config randomConfig(RandomSource random) {
        Config config = null;
        if (this.configs.size() > 1) {
            final float chanceSum = configs.parallelStream().map(c -> c.chance()).reduce(0.0f, (p, c) -> p + c);
            float rnd = random.nextFloat() * chanceSum;

            for (Config c : configs) {
                rnd -= c.chance();
                if (rnd <= 0) return c;
            }
        } else {
            return this.configs.get(0);
        }

        return null;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(ctx.chunkGenerator(),
                                                                                   ctx.heightAccessor());
        final Config config = randomConfig(ctx.random());
        if (config == null) return Optional.empty();

        ChunkPos chunkPos = ctx.chunkPos();
        final int x = chunkPos.getMiddleBlockX();
        final int z = chunkPos.getMiddleBlockZ();
        NoiseColumn column = ctx.chunkGenerator().getBaseColumn(x, z, ctx.heightAccessor(), ctx.randomState());
        final int seaLevel = ctx.chunkGenerator().getSeaLevel();
        StructureTemplate structureTemplate = ctx.structureTemplateManager().getOrCreate(config.location);
        final int maxHeight = worldGenerationContext.getGenDepth() - 4 - (structureTemplate.getSize(Rotation.NONE)
                                                                                           .getY() + config.offsetY);
        int y = seaLevel;
        BlockState state = column.getBlock(y - 1);

        for (; y < maxHeight; y++) {
            BlockState below = state;
            state = column.getBlock(y);
            if (state.is(Blocks.AIR) && below.is(Blocks.LAVA)) break;
        }
        if (y >= maxHeight) return Optional.empty();


        BlockPos halfSize = new BlockPos(structureTemplate.getSize().getX() / 2,
                                         0,
                                         structureTemplate.getSize().getZ() / 2);
        Rotation rotation = StructureNBT.getRandomRotation(ctx.random());
        Mirror mirror = StructureNBT.getRandomMirror(ctx.random());
        BlockPos centerPos = new BlockPos(x, y, z);
        BoundingBox boundingBox = structureTemplate.getBoundingBox(centerPos, rotation, halfSize, mirror);


        // if (!structure.canGenerate(ctx.chunkGenerator()., centerPos))
        return Optional.of(new GenerationStub(centerPos,
                                              structurePiecesBuilder ->
                                                      structurePiecesBuilder.addPiece(
                                                              new TemplatePiece(ctx.structureTemplateManager(),
                                                                                config.location,
                                                                                centerPos.offset(
                                                                                        0,
                                                                                        config.offsetY,
                                                                                        0),
                                                                                rotation,
                                                                                mirror,
                                                                                halfSize))
        ));

    }

    public record Config(ResourceLocation location, int offsetY, StructurePlacementType type, float chance) {
        public static final Codec<Config> CODEC =
                RecordCodecBuilder.create((instance) ->
                                                  instance.group(
                                                                  ResourceLocation.CODEC
                                                                          .fieldOf("location")
                                                                          .forGetter((cfg) -> cfg.location),

                                                                  Codec
                                                                          .INT
                                                                          .fieldOf("offset_y")
                                                                          .orElse(0)
                                                                          .forGetter((cfg) -> cfg.offsetY),

                                                                  StructurePlacementType.CODEC
                                                                          .fieldOf("placement")
                                                                          .orElse(StructurePlacementType.FLOOR)
                                                                          .forGetter((cfg) -> cfg.type),
                                                                  Codec
                                                                          .FLOAT
                                                                          .fieldOf("chance")
                                                                          .orElse(1.0f)
                                                                          .forGetter((cfg) -> cfg.chance)
                                                                )
                                                          .apply(instance, Config::new)
                                         );
    }
}
