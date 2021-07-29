package ru.bclib.items.tool;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tier;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.interfaces.ItemModelProvider;

import java.util.Map;

public class BaseHoeItem extends HoeItem implements ItemModelProvider {
	public BaseHoeItem(Tier material, int attackDamage, float attackSpeed, Properties settings) {
		super(material, attackDamage, attackSpeed, settings);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public UnbakedModel getItemModel(ResourceLocation itemID, Map<ResourceLocation, UnbakedModel> unbakedCache) {
		return ModelsHelper.createHandheldItem(itemID);
	}
}
