package ru.bclib.mixin.common.shears;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PumpkinBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.bclib.items.tool.BaseShearsItem;

@Mixin(PumpkinBlock.class)
public abstract class PumpkinBlockMixin {
	@Redirect(method="use", at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	public boolean bn_useProxy(ItemStack itemStack, Item item){
		return BaseShearsItem.isShear(itemStack, item);
	}
}
