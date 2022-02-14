package ru.bclib.blocks;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import ru.bclib.api.tag.NamedBlockTags;
import ru.bclib.api.tag.NamedItemTags;
import ru.bclib.api.tag.NamedMineableTags;
import ru.bclib.api.tag.TagAPI.TagLocation;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.interfaces.BlockModelProvider;
import ru.bclib.interfaces.RenderLayerProvider;
import ru.bclib.interfaces.TagProvider;
import ru.bclib.items.tool.BaseShearsItem;
import ru.bclib.util.MHelper;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BaseLeavesBlock extends LeavesBlock implements BlockModelProvider, RenderLayerProvider, TagProvider {
	protected final Block sapling;

	private static FabricBlockSettings makeLeaves(MaterialColor color) {
		return FabricBlockSettings
			.copyOf(Blocks.OAK_LEAVES)
			.mapColor(color)
			//.requiresTool()
			.allowsSpawning((state, world, pos, type) -> false)
			.suffocates((state, world, pos) -> false)
			.blockVision((state, world, pos) -> false);
	}

	public BaseLeavesBlock(Block sapling, MaterialColor color, Consumer<FabricBlockSettings> customizeProperties) {
		super(BaseBlock.acceptAndReturn(customizeProperties, makeLeaves(color)));
		this.sapling = sapling;
	}
	
	public BaseLeavesBlock(Block sapling, MaterialColor color, int light, Consumer<FabricBlockSettings> customizeProperties) {
		super(BaseBlock.acceptAndReturn(customizeProperties, makeLeaves(color).luminance(light)));
		this.sapling = sapling;
	}
	
	public BaseLeavesBlock(Block sapling, MaterialColor color) {
		super(makeLeaves(color));
		this.sapling = sapling;
	}
	
	public BaseLeavesBlock(Block sapling, MaterialColor color, int light) {
		super(makeLeaves(color).lightLevel(light));
		this.sapling = sapling;
	}
	
	@Override
	public BCLRenderLayer getRenderLayer() {
		return BCLRenderLayer.CUTOUT;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		return BaseLeavesBlock.getLeaveDrops(this, this.sapling, builder, 16, 16);
	}
	
	public static List<ItemStack> getLeaveDrops(ItemLike leaveBlock, Block sapling, LootContext.Builder builder, int fortuneRate, int dropRate) {
		ItemStack tool = builder.getParameter(LootContextParams.TOOL);
		if (tool != null) {
			if (BaseShearsItem.isShear(tool) || EnchantmentHelper.getItemEnchantmentLevel(
				Enchantments.SILK_TOUCH,
				tool
			) > 0) {
				return Collections.singletonList(new ItemStack(leaveBlock));
			}
			int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
			if (MHelper.RANDOM.nextInt(fortuneRate) <= fortune) {
				return Lists.newArrayList(new ItemStack(sapling));
			}
			return Lists.newArrayList();
		}
		return MHelper.RANDOM.nextInt(dropRate) == 0 ? Lists.newArrayList(new ItemStack(sapling)) : Lists.newArrayList();
	}
	
	@Override
	public BlockModel getItemModel(ResourceLocation resourceLocation) {
		return getBlockModel(resourceLocation, defaultBlockState());
	}
	
	@Override
	public void addTags(List<TagLocation<Block>> blockTags, List<TagLocation<Item>> itemTags) {
		blockTags.add(NamedMineableTags.SHEARS);
		blockTags.add(NamedMineableTags.HOE);
		blockTags.add(NamedBlockTags.LEAVES);
		itemTags.add(NamedItemTags.LEAVES);
	}
}
