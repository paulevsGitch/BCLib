package ru.bclib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
import ru.bclib.api.BiomeAPI;
import ru.bclib.api.BonemealAPI;
import ru.bclib.api.TagAPI;
import ru.bclib.util.BlocksHelper;
import ru.bclib.util.MHelper;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
	private static final MutableBlockPos bclib_BLOCK_POS = new MutableBlockPos();

	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void bclib_onUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> info) {
		Level world = context.getLevel();
		BlockPos blockPos = context.getClickedPos();
		if (!world.isClientSide) {
			BlockPos offseted = blockPos.relative(context.getClickedFace());
			boolean endBiome = world.getBiome(offseted).getBiomeCategory() == BiomeCategory.THEEND;
			
			if (world.getBlockState(blockPos).is(TagAPI.END_GROUND)) {
				boolean consume = false;
				if (world.getBlockState(blockPos).is(Blocks.END_STONE)) {
					BlockState nylium = bclib_getNylium(world, blockPos);
					if (nylium != null) {
						BlocksHelper.setWithoutUpdate(world, blockPos, nylium);
						consume = true;
					}
				}
				else {
					if (!world.getFluidState(offseted).isEmpty() && endBiome) {
						if (world.getBlockState(offseted).getBlock().equals(Blocks.WATER)) {
							consume = bclib_growWaterGrass(world, blockPos);
						}
					}
					else {
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
			else if (!world.getFluidState(offseted).isEmpty() && endBiome) {
				if (world.getBlockState(offseted).getBlock().equals(Blocks.WATER)) {
					info.setReturnValue(InteractionResult.FAIL);
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
		block = BonemealAPI.getLandGrass(BiomeAPI.getBiomeID(world.getBiome(pos)), block, world.getRandom());
		return block == null ? null : block.defaultBlockState();
	}

	private BlockState bclib_getNylium(Level world, BlockPos pos) {
		Vec3i[] offsets = MHelper.getOffsets(world.getRandom());
		for (Vec3i dir : offsets) {
			BlockPos p = pos.offset(dir);
			BlockState state = world.getBlockState(p);
			if (BonemealAPI.isSpreadable(state.getBlock())) {
				return state;
			}
		}
		return null;
	}
}