package ru.bclib.mixin.client;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.bclib.api.tag.CommonBlockTags;

@Mixin(EnchantmentTableBlock.class)
public abstract class EnchantingTableBlockMixin extends Block {
	public EnchantingTableBlockMixin(Properties settings) {
		super(settings);
	}
	
	@Redirect(method = "animateTick", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")//,
	)
	private boolean bclib_isBookshelf(BlockState state, Block block) {
		return block == Blocks.BOOKSHELF ? state.is(CommonBlockTags.BOOKSHELVES) : state.is(block);
	}
}
