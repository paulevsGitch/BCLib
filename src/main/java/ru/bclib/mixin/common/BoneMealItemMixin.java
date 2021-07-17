package ru.bclib.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.api.BiomeAPI;
import ru.bclib.api.BonemealAPI;
import ru.bclib.api.TagAPI;
import ru.bclib.util.BlocksHelper;
import ru.bclib.util.MHelper;

import java.util.Collection;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
	private static final MutableBlockPos bclib_BLOCK_POS = new MutableBlockPos();
	
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void bclib_onUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> info) {
		Level world = context.getLevel();
		BlockPos blockPos = context.getClickedPos();
		if (!world.isClientSide) {
			BlockPos offseted = blockPos.relative(context.getClickedFace());
			if (BonemealAPI.isTerrain(world.getBlockState(blockPos).getBlock())) {
				boolean consume = false;
				if (BonemealAPI.isSpreadableTerrain(world.getBlockState(blockPos).getBlock())) {
					BlockState terrain = bclib_getSpreadable(world, blockPos);
					if (terrain != null) {
						BlocksHelper.setWithoutUpdate(world, blockPos, terrain);
						consume = true;
					}
				}
				else {
					BlockState stateAbove = world.getBlockState(blockPos.above());
					if (!stateAbove.getFluidState().isEmpty()) {
						if (stateAbove.is(Blocks.WATER)) {
							consume = bclib_growWaterGrass(world, blockPos);
						}
					}
					else if (stateAbove.isAir()) {
						consume = bclib_growLandGrass(world, blockPos);
					}
				}
				if (consume) {
					if (!context.getPlayer().isCreative()) {
						context.getItemInHand().shrink(1);
					}
					world.levelEvent(2005, blockPos, 0);
					info.setReturnValue(InteractionResult.SUCCESS);
					info.cancel();
				}
			}
		}
	}
	
	private boolean bclib_growLandGrass(Level world, BlockPos pos) {
		int y1 = pos.getY() + 3;
		int y2 = pos.getY() - 3;
		boolean result = false;
		for (int i = 0; i < 64; i++) {
			int x = (int) (pos.getX() + world.random.nextGaussian() * 2);
			int z = (int) (pos.getZ() + world.random.nextGaussian() * 2);
			bclib_BLOCK_POS.setX(x);
			bclib_BLOCK_POS.setZ(z);
			for (int y = y1; y >= y2; y--) {
				bclib_BLOCK_POS.setY(y);
				BlockPos down = bclib_BLOCK_POS.below();
				if (world.isEmptyBlock(bclib_BLOCK_POS) && !world.isEmptyBlock(down)) {
					BlockState grass = bclib_getLandGrassState(world, down);
					if (grass != null) {
						BlocksHelper.setWithoutUpdate(world, bclib_BLOCK_POS, grass);
						result = true;
					}
					break;
				}
			}
		}
		return result;
	}
	
	private boolean bclib_growWaterGrass(Level world, BlockPos pos) {
		int y1 = pos.getY() + 3;
		int y2 = pos.getY() - 3;
		boolean result = false;
		for (int i = 0; i < 64; i++) {
			int x = (int) (pos.getX() + world.random.nextGaussian() * 2);
			int z = (int) (pos.getZ() + world.random.nextGaussian() * 2);
			bclib_BLOCK_POS.setX(x);
			bclib_BLOCK_POS.setZ(z);
			for (int y = y1; y >= y2; y--) {
				bclib_BLOCK_POS.setY(y);
				BlockPos down = bclib_BLOCK_POS.below();
				if (BlocksHelper.isFluid(world.getBlockState(bclib_BLOCK_POS)) && !BlocksHelper.isFluid(world.getBlockState(down))) {
					BlockState grass = bclib_getWaterGrassState(world, down);
					if (grass != null) {
						BlocksHelper.setWithoutUpdate(world, bclib_BLOCK_POS, grass);
						result = true;
					}
					break;
				}
			}
		}
		return result;
	}
	
	private BlockState bclib_getLandGrassState(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		block = BonemealAPI.getLandGrass(BiomeAPI.getBiomeID(world.getBiome(pos)), block, world.getRandom());
		return block == null ? null : block.defaultBlockState();
	}
	
	private BlockState bclib_getWaterGrassState(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		block = BonemealAPI.getWaterGrass(BiomeAPI.getBiomeID(world.getBiome(pos)), block, world.getRandom());
		return block == null ? null : block.defaultBlockState();
	}
	
	private BlockState bclib_getSpreadable(Level world, BlockPos pos) {
		Vec3i[] offsets = MHelper.getOffsets(world.getRandom());
		BlockState center = world.getBlockState(pos);
		for (Vec3i dir : offsets) {
			BlockPos p = pos.offset(dir);
			BlockState state = world.getBlockState(p);
			Block terrain = BonemealAPI.getSpreadable(state.getBlock());
			if (center.is(terrain)) {
				if (haveSameProperties(state, center)) {
					for (Property property: center.getProperties()) {
						state = state.setValue(property, center.getValue(property));
					}
				}
				return state;
			}
		}
		return null;
	}
	
	private boolean haveSameProperties(BlockState state1, BlockState state2) {
		Property<?>[] properties1 = state1.getProperties().toArray(new Property[0]);
		Property<?>[] properties2 = state2.getProperties().toArray(new Property[0]);
		if (properties1.length != properties2.length) {
			return false;
		}
		for (int i = 0; i < properties1.length; i++) {
			String name1 = properties1[i].getName();
			String name2 = properties2[i].getName();
			if (!name1.equals(name2)) {
				return false;
			}
		}
		return true;
	}
}