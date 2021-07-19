package ru.bclib.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import ru.bclib.interfaces.BlockModelProvider;
import ru.bclib.util.MHelper;

import java.util.Collections;
import java.util.List;

public class BaseOreBlock extends OreBlock implements BlockModelProvider {
	private final Item dropItem;
	private final int minCount;
	private final int maxCount;
	
	public BaseOreBlock(Item drop, int minCount, int maxCount, int experience) {
		super(FabricBlockSettings.of(Material.STONE, MaterialColor.SAND)
								 .hardness(3F)
								 .resistance(9F)
								 .requiresCorrectToolForDrops()
								 .sound(SoundType.STONE), UniformInt.of(1, experience));
		this.dropItem = drop;
		this.minCount = minCount;
		this.maxCount = maxCount;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		ItemStack tool = builder.getParameter(LootContextParams.TOOL);
		if (tool != null && tool.isCorrectToolForDrops(state)) {
			if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
				return Collections.singletonList(new ItemStack(this));
			}
			int count;
			int enchantment = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
			if (enchantment > 0) {
				int min = Mth.clamp(minCount + enchantment, minCount, maxCount);
				int max = maxCount + (enchantment / Enchantments.BLOCK_FORTUNE.getMaxLevel());
				if (min == max) {
					return Collections.singletonList(new ItemStack(dropItem, max));
				}
				count = MHelper.randRange(min, max, MHelper.RANDOM);
			}
			else {
				count = MHelper.randRange(minCount, maxCount, MHelper.RANDOM);
			}
			return Collections.singletonList(new ItemStack(dropItem, count));
		}
		return Collections.emptyList();
	}
	
	@Override
	public BlockModel getItemModel(ResourceLocation resourceLocation) {
		return getBlockModel(resourceLocation, defaultBlockState());
	}
}
