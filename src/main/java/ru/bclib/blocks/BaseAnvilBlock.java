package ru.bclib.blocks;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import ru.bclib.client.models.BasePatterns;
import ru.bclib.client.models.BlockModelProvider;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;
import ru.bclib.items.BaseAnvilItem;
import ru.bclib.util.BlocksHelper;

public abstract class BaseAnvilBlock extends AnvilBlock implements BlockModelProvider {
	public static final IntegerProperty DESTRUCTION = BlockProperties.DESTRUCTION;

	public BaseAnvilBlock(MaterialColor color) {
		super(BlocksHelper.copySettingsOf(Blocks.ANVIL).mapColor(color));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(DESTRUCTION);
	}

	@Override
	@SuppressWarnings("deprecation")
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		ItemStack dropStack = new ItemStack(this);
		int destruction = state.getValue(DESTRUCTION);
		dropStack.getOrCreateTag().putInt(BaseAnvilItem.DESTRUCTION, destruction);
		return Lists.newArrayList(dropStack);
	}

	protected String getTop(ResourceLocation blockId, String block) {
		if (block.contains("item")) {
			return blockId.getPath() + "_top_0";
		}
		char last = block.charAt(block.length() - 1);
		return blockId.getPath() + "_top_" + last;
	}

	@Override
	public abstract Item asItem();

	@Override
	@Environment(EnvType.CLIENT)
	public BlockModel getItemModel(ResourceLocation blockId) {
		return getBlockModel(blockId, defaultBlockState());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
		int destruction = blockState.getValue(DESTRUCTION);
		String name = blockId.getPath();
		Map<String, String> textures = Maps.newHashMap();
		textures.put("%modid%", blockId.getNamespace());
		textures.put("%anvil%", name);
		textures.put("%top%", name + "_top_" + destruction);
		Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_ANVIL, textures);
		return ModelsHelper.fromPattern(pattern);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public UnbakedModel getModelVariant(ResourceLocation stateId, BlockState blockState, Map<ResourceLocation, UnbakedModel> modelCache) {
		int destruction = blockState.getValue(DESTRUCTION);
		String modId = stateId.getNamespace();
		String modelId = "block/" + stateId.getPath() + "_top_" + destruction;
		ResourceLocation modelLocation = new ResourceLocation(modId, modelId);
		registerBlockModel(stateId, modelLocation, blockState, modelCache);
		return ModelsHelper.createFacingModel(modelLocation, blockState.getValue(FACING), false, false);
	}
}
