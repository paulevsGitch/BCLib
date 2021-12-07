package ru.bclib.util;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import ru.bclib.api.TagAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class StructureHelper {
	private static final Direction[] DIR = BlocksHelper.makeHorizontal();
	
	public static StructureTemplate readStructure(ResourceLocation resource) {
		String ns = resource.getNamespace();
		String nm = resource.getPath();
		return readStructure("/data/" + ns + "/structures/" + nm + ".nbt");
	}
	
	public static StructureTemplate readStructure(File datapack, String path) {
		if (datapack.isDirectory()) {
			return readStructure(datapack.toString() + "/" + path);
		}
		else if (datapack.isFile() && datapack.getName().endsWith(".zip")) {
			try {
				ZipFile zipFile = new ZipFile(datapack);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					String name = entry.getName();
					long compressedSize = entry.getCompressedSize();
					long normalSize = entry.getSize();
					String type = entry.isDirectory() ? "DIR" : "FILE";
					
					//System.out.println(name);
					System.out.format("\t %s - %d - %d\n", type, compressedSize, normalSize);
				}
				zipFile.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static StructureTemplate readStructure(String path) {
		try {
			InputStream inputstream = StructureHelper.class.getResourceAsStream(path);
			return readStructureFromStream(inputstream);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static StructureTemplate readStructureFromStream(InputStream stream) throws IOException {
		CompoundTag nbttagcompound = NbtIo.readCompressed(stream);
		
		StructureTemplate template = new StructureTemplate();
		template.load(nbttagcompound);
		
		return template;
	}
	
	public static BlockPos offsetPos(BlockPos pos, StructureTemplate structure, Rotation rotation, Mirror mirror) {
		Vec3 offset = StructureTemplate.transform(
			Vec3.atCenterOf(structure.getSize()),
			mirror,
			rotation,
			BlockPos.ZERO
		);
		return pos.offset(-offset.x * 0.5, 0, -offset.z * 0.5);
	}
	
	public static void placeCenteredBottom(WorldGenLevel world, BlockPos pos, StructureTemplate structure, Rotation rotation, Mirror mirror, Random random) {
		placeCenteredBottom(world, pos, structure, rotation, mirror, makeBox(pos), random);
	}
	
	public static void placeCenteredBottom(WorldGenLevel world, BlockPos pos, StructureTemplate structure, Rotation rotation, Mirror mirror, BoundingBox bounds, Random random) {
		BlockPos offset = offsetPos(pos, structure, rotation, mirror);
		StructurePlaceSettings placementData = new StructurePlaceSettings().setRotation(rotation)
																		   .setMirror(mirror)
																		   .setBoundingBox(bounds);
		structure.placeInWorld(world, offset, offset, placementData, random, 4);
	}
	
	private static BoundingBox makeBox(BlockPos pos) {
		int sx = ((pos.getX() >> 4) << 4) - 16;
		int sz = ((pos.getZ() >> 4) << 4) - 16;
		int ex = sx + 47;
		int ez = sz + 47;
		return BoundingBox.fromCorners(new Vec3i(sx, 0, sz), new Vec3i(ex, 255, ez));
	}
	
	public static BoundingBox getStructureBounds(BlockPos pos, StructureTemplate structure, Rotation rotation, Mirror mirror) {
		Vec3i max = structure.getSize();
		Vec3 min = StructureTemplate.transform(Vec3.atCenterOf(structure.getSize()), mirror, rotation, BlockPos.ZERO);
		max = max.offset(-min.x, -min.y, -min.z);
		return BoundingBox.fromCorners(pos.offset(min.x, min.y, min.z), max.offset(pos));
	}
	
	public static BoundingBox intersectBoxes(BoundingBox box1, BoundingBox box2) {
		int x1 = MHelper.max(box1.minX(), box2.minX());
		int y1 = MHelper.max(box1.minY(), box2.minY());
		int z1 = MHelper.max(box1.minZ(), box2.minZ());
		
		int x2 = MHelper.min(box1.maxX(), box2.maxX());
		int y2 = MHelper.min(box1.maxY(), box2.maxY());
		int z2 = MHelper.min(box1.maxZ(), box2.maxZ());
		
		return BoundingBox.fromCorners(new Vec3i(x1, y1, z1), new Vec3i(x2, y2, z2));
	}
	
	public static void erode(WorldGenLevel world, BoundingBox bounds, int iterations, Random random) {
		MutableBlockPos mut = new MutableBlockPos();
		boolean canDestruct = true;
		for (int i = 0; i < iterations; i++) {
			for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
				mut.setX(x);
				for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
					mut.setZ(z);
					for (int y = bounds.maxY(); y >= bounds.minY(); y--) {
						mut.setY(y);
						BlockState state = world.getBlockState(mut);
						boolean ignore = ignore(state, world, mut);
						if (canDestruct && BlocksHelper.isInvulnerable(
							state,
							world,
							mut
						) && random.nextInt(8) == 0 && world.isEmptyBlock(mut.below(2))) {
							int r = MHelper.randRange(1, 4, random);
							int cx = mut.getX();
							int cy = mut.getY();
							int cz = mut.getZ();
							int x1 = cx - r;
							int y1 = cy - r;
							int z1 = cz - r;
							int x2 = cx + r;
							int y2 = cy + r;
							int z2 = cz + r;
							for (int px = x1; px <= x2; px++) {
								int dx = px - cx;
								dx *= dx;
								mut.setX(px);
								for (int py = y1; py <= y2; py++) {
									int dy = py - cy;
									dy *= dy;
									mut.setY(py);
									for (int pz = z1; pz <= z2; pz++) {
										int dz = pz - cz;
										dz *= dz;
										mut.setZ(pz);
										if (dx + dy + dz <= r && BlocksHelper.isInvulnerable(
											world.getBlockState(mut),
											world,
											mut
										)) {
											BlocksHelper.setWithoutUpdate(world, mut, Blocks.AIR);
										}
									}
								}
							}
							mut.setX(cx);
							mut.setY(cy);
							mut.setZ(cz);
							canDestruct = false;
							continue;
						}
						else if (ignore) {
							continue;
						}
						if (!state.isAir() && random.nextBoolean()) {
							MHelper.shuffle(DIR, random);
							for (Direction dir : DIR) {
								if (world.isEmptyBlock(mut.relative(dir)) && world.isEmptyBlock(mut.below()
																								   .relative(dir))) {
									BlocksHelper.setWithoutUpdate(world, mut, Blocks.AIR);
									mut.move(dir).move(Direction.DOWN);
									for (int py = mut.getY(); y >= bounds.minY() - 10; y--) {
										mut.setY(py - 1);
										if (!world.isEmptyBlock(mut)) {
											mut.setY(py);
											BlocksHelper.setWithoutUpdate(world, mut, state);
											break;
										}
									}
								}
							}
							break;
						}
						else if (random.nextInt(8) == 0 && !BlocksHelper.isInvulnerable(
							world.getBlockState(mut.above()),
							world,
							mut
						)) {
							BlocksHelper.setWithoutUpdate(world, mut, Blocks.AIR);
						}
					}
				}
			}
		}
		for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
			mut.setX(x);
			for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
				mut.setZ(z);
				for (int y = bounds.maxY(); y >= bounds.minY(); y--) {
					mut.setY(y);
					BlockState state = world.getBlockState(mut);
					if (!ignore(state, world, mut) && world.isEmptyBlock(mut.below())) {
						BlocksHelper.setWithoutUpdate(world, mut, Blocks.AIR);
						for (int py = mut.getY(); py >= bounds.minY() - 10; py--) {
							mut.setY(py - 1);
							if (!world.isEmptyBlock(mut)) {
								mut.setY(py);
								BlocksHelper.setWithoutUpdate(world, mut, state);
								break;
							}
						}
					}
				}
			}
		}
	}
	
	public static void erodeIntense(WorldGenLevel world, BoundingBox bounds, Random random) {
		MutableBlockPos mut = new MutableBlockPos();
		MutableBlockPos mut2 = new MutableBlockPos();
		int minY = bounds.minY() - 10;
		for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
			mut.setX(x);
			for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
				mut.setZ(z);
				for (int y = bounds.maxY(); y >= bounds.minY(); y--) {
					mut.setY(y);
					BlockState state = world.getBlockState(mut);
					if (!ignore(state, world, mut)) {
						if (random.nextInt(6) == 0) {
							BlocksHelper.setWithoutUpdate(world, mut, Blocks.AIR);
							if (random.nextBoolean()) {
								int px = MHelper.floor(random.nextGaussian() * 2 + x + 0.5);
								int pz = MHelper.floor(random.nextGaussian() * 2 + z + 0.5);
								mut2.set(px, y, pz);
								while (world.getBlockState(mut2).getMaterial().isReplaceable() && mut2.getY() > minY) {
									mut2.setY(mut2.getY() - 1);
								}
								if (!world.getBlockState(mut2).isAir() && state.canSurvive(world, mut2)) {
									mut2.setY(mut2.getY() + 1);
									BlocksHelper.setWithoutUpdate(world, mut2, state);
								}
							}
						}
						else if (random.nextInt(8) == 0) {
							BlocksHelper.setWithoutUpdate(world, mut, Blocks.AIR);
						}
					}
				}
			}
		}
		
		drop(world, bounds);
	}
	
	private static boolean isTerrainNear(WorldGenLevel world, BlockPos pos) {
		for (Direction dir : BlocksHelper.DIRECTIONS) {
			if (world.getBlockState(pos.relative(dir)).is(TagAPI.BLOCK_GEN_TERRAIN)) {
				return true;
			}
		}
		return false;
	}
	
	private static void drop(WorldGenLevel world, BoundingBox bounds) {
		MutableBlockPos mut = new MutableBlockPos();
		
		Set<BlockPos> blocks = Sets.newHashSet();
		Set<BlockPos> edge = Sets.newHashSet();
		Set<BlockPos> add = Sets.newHashSet();
		
		for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
			mut.setX(x);
			for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
				mut.setZ(z);
				for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
					mut.setY(y);
					BlockState state = world.getBlockState(mut);
					if (!ignore(state, world, mut) && isTerrainNear(world, mut)) {
						edge.add(mut.immutable());
					}
				}
			}
		}
		
		if (edge.isEmpty()) {
			return;
		}
		
		while (!edge.isEmpty()) {
			for (BlockPos center : edge) {
				for (Direction dir : BlocksHelper.DIRECTIONS) {
					BlockState state = world.getBlockState(center);
					if (state.isCollisionShapeFullBlock(world, center)) {
						mut.set(center).move(dir);
						if (bounds.isInside(mut)) {
							state = world.getBlockState(mut);
							if (!ignore(state, world, mut) && !blocks.contains(mut)) {
								add.add(mut.immutable());
							}
						}
					}
				}
			}
			
			blocks.addAll(edge);
			edge.clear();
			edge.addAll(add);
			add.clear();
		}
		
		int minY = bounds.minY() - 10;
		for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
			mut.setX(x);
			for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
				mut.setZ(z);
				for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
					mut.setY(y);
					BlockState state = world.getBlockState(mut);
					if (!ignore(state, world, mut) && !blocks.contains(mut)) {
						BlocksHelper.setWithoutUpdate(world, mut, Blocks.AIR);
						while (world.getBlockState(mut).getMaterial().isReplaceable() && mut.getY() > minY) {
							mut.setY(mut.getY() - 1);
						}
						if (mut.getY() > minY) {
							mut.setY(mut.getY() + 1);
							BlocksHelper.setWithoutUpdate(world, mut, state);
						}
					}
				}
			}
		}
	}
	
	private static boolean ignore(BlockState state, WorldGenLevel world, BlockPos pos) {
		return state.getMaterial().isReplaceable() || !state.getFluidState()
															.isEmpty() || state.is(TagAPI.BLOCK_END_GROUND) || state.is(
			BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.getMaterial()
																  .equals(Material.PLANT) || state.getMaterial()
																								  .equals(Material.LEAVES) || BlocksHelper
			.isInvulnerable(state, world, pos);
	}
	
	public static void cover(WorldGenLevel world, BoundingBox bounds, Random random) {
		MutableBlockPos mut = new MutableBlockPos();
		for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
			mut.setX(x);
			for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
				mut.setZ(z);
				BlockState top = world.getBiome(mut).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial();
				for (int y = bounds.maxY(); y >= bounds.minY(); y--) {
					mut.setY(y);
					BlockState state = world.getBlockState(mut);
					if (state.is(TagAPI.BLOCK_END_GROUND) && !world.getBlockState(mut.above())
															 .getMaterial()
															 .isSolidBlocking()) {
						BlocksHelper.setWithoutUpdate(world, mut, top);
					}
				}
			}
		}
	}
}
