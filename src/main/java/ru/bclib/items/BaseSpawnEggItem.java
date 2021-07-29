package ru.bclib.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.SpawnEggItem;
import ru.bclib.client.models.BasePatterns;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;
import ru.bclib.interfaces.ItemModelProvider;

import java.util.Map;
import java.util.Optional;

public class BaseSpawnEggItem extends SpawnEggItem implements ItemModelProvider {
	public BaseSpawnEggItem(EntityType<? extends Mob> type, int primaryColor, int secondaryColor, Properties settings) {
		super(type, primaryColor, secondaryColor, settings);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public UnbakedModel getItemModel(ResourceLocation itemID, Map<ResourceLocation, UnbakedModel> unbakedCache) {
		Optional<String> pattern = PatternsHelper.createJson(BasePatterns.ITEM_SPAWN_EGG, itemID);
		return ModelsHelper.fromPattern(pattern);
	}
}
