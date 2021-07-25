package ru.bclib.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;
import ru.bclib.client.models.BasePatterns;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BaseBookshelfBlock extends BaseBlock {
	public BaseBookshelfBlock(Block source) {
		super(FabricBlockSettings.copyOf(source));
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		ItemStack tool = builder.getParameter(LootContextParams.TOOL);
		if (tool != null && tool.isCorrectToolForDrops(state)) {
			int silk = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool);
			if (silk > 0) {
				return Collections.singletonList(new ItemStack(this));
			}
		}
		return Collections.singletonList(new ItemStack(Items.BOOK, 3));
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void registerModels(ResourceLocation blockID, Map<ResourceLocation, UnbakedModel> modelRegistry, Map<ResourceLocation, UnbakedModel> unbakedCache) {
		Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_BOOKSHELF, replacePath(blockID));
		modelRegistry.put(blockID, ModelsHelper.fromPattern(pattern));
	}
	
	private ResourceLocation replacePath(ResourceLocation blockId) {
		String newPath = blockId.getPath().replace("_bookshelf", "");
		return new ResourceLocation(blockId.getNamespace(), newPath);
	}
}
