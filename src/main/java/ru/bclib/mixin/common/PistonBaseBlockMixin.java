package ru.bclib.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.api.TagAPI;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {
	@Inject(method="isPushable", at=@At("HEAD"), cancellable = true)
	private static void bclib_isPushable(BlockState blockState, Level level, BlockPos blockPos, Direction direction, boolean bl, Direction direction2, CallbackInfoReturnable<Boolean> cir){
		if (blockState.is(TagAPI.BLOCK_IMMOBILE)){
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
}
