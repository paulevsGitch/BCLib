package ru.bclib.mixin.common;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.blocks.BaseAnvilBlock;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
	@Shadow
	private int repairItemCountCost;
	
	@Final
	@Shadow
	private DataSlot cost;
	
	public AnvilMenuMixin(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
		super(menuType, i, inventory, containerLevelAccess);
	}
	
	@Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
	protected void bclib_onTakeAnvilOutput(Player player, ItemStack stack, CallbackInfo info) {
		this.access.execute((level, blockPos) -> {
			BlockState blockState = level.getBlockState(blockPos);
			if (blockState.getBlock() instanceof BaseAnvilBlock) {
				info.cancel();
				if (!player.getAbilities().instabuild) {
					player.giveExperienceLevels(-this.cost.get());
				}
				
				this.inputSlots.setItem(0, ItemStack.EMPTY);
				if (this.repairItemCountCost > 0) {
					ItemStack itemStack2 = this.inputSlots.getItem(1);
					if (!itemStack2.isEmpty() && itemStack2.getCount() > this.repairItemCountCost) {
						itemStack2.shrink(this.repairItemCountCost);
						this.inputSlots.setItem(1, itemStack2);
					}
					else {
						this.inputSlots.setItem(1, ItemStack.EMPTY);
					}
				}
				else {
					this.inputSlots.setItem(1, ItemStack.EMPTY);
				}
				
				this.cost.set(0);
				
				if (!player.getAbilities().instabuild && blockState.is(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
					BaseAnvilBlock anvil = (BaseAnvilBlock) blockState.getBlock();
					BlockState damaged = anvil.damageAnvilUse(blockState, player.getRandom());
					if (damaged == null) {
						level.removeBlock(blockPos, false);
						level.levelEvent(1029, blockPos, 0);
					}
					else {
						level.setBlock(blockPos, damaged, 2);
						level.levelEvent(1030, blockPos, 0);
					}
				}
				else {
					level.levelEvent(1030, blockPos, 0);
				}
			}
		});
	}
}
