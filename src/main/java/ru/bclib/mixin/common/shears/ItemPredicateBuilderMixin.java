package ru.bclib.mixin.common.shears;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bclib.api.tag.CommonItemTags;

import java.util.Set;

@Mixin(ItemPredicate.class)
public abstract class ItemPredicateBuilderMixin {
	
	@Shadow @Final private @Nullable Set<Item> items;
	
	@Inject(method = "matches", at = @At("HEAD"), cancellable = true)
	void bclib_of(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
		if (this.items != null && this.items.size() == 1 && this.items.contains(Items.SHEARS)) {
			if (itemStack.is(CommonItemTags.SHEARS) ){
				cir.setReturnValue(true);
			}
		}
	}
}
