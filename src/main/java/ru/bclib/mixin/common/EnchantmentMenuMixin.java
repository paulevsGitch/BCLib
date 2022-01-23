package ru.bclib.mixin.common;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.bclib.api.tag.CommonBlockTags;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin extends AbstractContainerMenu {
	protected EnchantmentMenuMixin(MenuType<?> type, int syncId) {
		super(type, syncId);
	}
	
	@Redirect(method = "lambda$slotsChanged$0(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")//,
	)
	private boolean bclib_isBookshelf(BlockState state, Block block) {
		return block == Blocks.BOOKSHELF ? state.is(CommonBlockTags.BOOKSHELVES) : state.is(block);
	}
}
