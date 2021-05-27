package ru.bclib.items;

import java.util.Optional;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import ru.bclib.client.models.BasePatterns;
import ru.bclib.client.models.ItemModelProvider;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;

public class BaseSpawnEggItem extends SpawnEggItem implements ItemModelProvider {
	public BaseSpawnEggItem(EntityType<?> type, int primaryColor, int secondaryColor, Properties settings) {
		super(type, primaryColor, secondaryColor, settings);
	}
	
	@Override
	public BlockModel getItemModel(ResourceLocation resourceLocation) {
		Optional<String> pattern = PatternsHelper.createJson(BasePatterns.ITEM_SPAWN_EGG, resourceLocation);
		return ModelsHelper.fromPattern(pattern);
	}
}
