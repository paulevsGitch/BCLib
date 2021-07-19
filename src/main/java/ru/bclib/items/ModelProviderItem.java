package ru.bclib.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.interfaces.ItemModelGetter;

public class ModelProviderItem extends Item implements ItemModelGetter {
	public ModelProviderItem(Properties settings) {
		super(settings);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public BlockModel getItemModel(ResourceLocation resourceLocation) {
		return ModelsHelper.createItemModel(resourceLocation);
	}
}
