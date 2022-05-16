package ru.bclib.mixin.common;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.interfaces.LootProvider;

import java.util.List;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
	@Inject(
		method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/storage/loot/LootContext$Builder;)Ljava/util/List;",
		at = @At("HEAD"), cancellable = true
	)
	private void bclib_getBlockDrops(BlockState state, LootContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> info) {
		if (this instanceof LootProvider) {
			LootProvider provider = LootProvider.class.cast(this);
			info.setReturnValue(provider.getLoot(state, builder));
		}
	}
}
