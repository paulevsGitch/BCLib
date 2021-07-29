package ru.bclib.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.interfaces.ItemModelProvider;

import java.util.Map;

public class ModelProviderItem extends Item implements ItemModelProvider {
	public ModelProviderItem(Properties settings) {
		super(settings);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public UnbakedModel getItemModel(ResourceLocation itemID, Map<ResourceLocation, UnbakedModel> unbakedCache) {
		return ModelsHelper.createItemModel(itemID);
	}
}
