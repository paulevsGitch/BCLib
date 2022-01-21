package ru.bclib.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour.StatePredicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.bclib.api.TagAPI;

@Mixin(PortalShape.class)
public class PortalShapeMixin {
	@Redirect(method="getDistanceUntilEdgeAboveFrame", at=@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;test(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean be_getDistanceUntilEdgeAboveFrame(StatePredicate statePredicate, BlockState blockState, BlockGetter blockGetter, BlockPos blockPos){
		return be_FRAME(statePredicate, blockState, blockGetter, blockPos);
	}
	
	@Redirect(method="hasTopFrame", at=@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;test(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean be_hasTopFrame(StatePredicate statePredicate, BlockState blockState, BlockGetter blockGetter, BlockPos blockPos){
		return be_FRAME(statePredicate, blockState, blockGetter, blockPos);
	}
	
	@Redirect(method="getDistanceUntilTop", at=@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;test(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean be_getDistanceUntilTop(StatePredicate statePredicate, BlockState blockState, BlockGetter blockGetter, BlockPos blockPos){
		return be_FRAME(statePredicate, blockState, blockGetter, blockPos);
	}
	
	private static boolean be_FRAME(StatePredicate FRAME, BlockState state, BlockGetter getter, BlockPos pos){
		return state.is(TagAPI.COMMON_BLOCK_NETHER_PORTAL_FRAME) || FRAME.test(state, getter, pos);
	}
}
