package ru.bclib.items;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.FishBucketItem;
import net.minecraft.world.level.material.Fluids;
import ru.bclib.client.models.ItemModelProvider;

public class BaseBucketItem extends FishBucketItem implements ItemModelProvider {
	public BaseBucketItem(EntityType<?> type, FabricItemSettings settings) {
		super(type, Fluids.WATER, settings.stacksTo(1));
	}
}
