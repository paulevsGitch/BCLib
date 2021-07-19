package ru.bclib.items;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.material.Fluids;
import ru.bclib.interfaces.ItemModelGetter;

public class BaseBucketItem extends MobBucketItem implements ItemModelGetter {
	public BaseBucketItem(EntityType<?> type, FabricItemSettings settings) {
		super(type, Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, settings.stacksTo(1));
	}
}
