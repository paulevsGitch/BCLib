package ru.bclib.blocks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import ru.bclib.client.models.BasePatterns;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;
import ru.bclib.interfaces.BlockModelProvider;
import ru.bclib.interfaces.CustomItemProvider;
import ru.bclib.items.BaseAnvilItem;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public abstract class BaseAnvilBlock extends AnvilBlock implements BlockModelProvider, CustomItemProvider {
	public static final IntegerProperty DESTRUCTION = BlockProperties.DESTRUCTION;
	public IntegerProperty durability;
	
	public BaseAnvilBlock(MaterialColor color) {
		super(FabricBlockSettings.copyOf(Blocks.ANVIL).mapColor(color));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		if (getMaxDurability() != 3) {
			durability = IntegerProperty.create("durability", 0, getMaxDurability());
		}
		else {
			durability = BlockProperties.DEFAULT_ANVIL_DURABILITY;
		}
		builder.add(DESTRUCTION, durability);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void registerModels(ResourceLocation blockID, Map<ResourceLocation, UnbakedModel> modelRegistry, Map<ResourceLocation, UnbakedModel> unbakedCache) {
		for (int destruction = 0; destruction < 3; destruction++) {
			String name = blockID.getPath();
			Map<String, String> textures = Maps.newHashMap();
			textures.put("%modid%", blockID.getNamespace());
			textures.put("%anvil%", name);
			textures.put("%top%", name + "_top_" + destruction);
			Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_ANVIL, textures);
			ResourceLocation location = getModelLocation(destruction, blockID);
			modelRegistry.put(location, ModelsHelper.fromPattern(pattern));
		}
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public ResourceLocation getStateModel(ResourceLocation stateId, BlockState blockState) {
		int destruction = blockState.getValue(DESTRUCTION);
		return getModelLocation(destruction, stateId);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public UnbakedModel getItemModel(ResourceLocation itemID, Map<ResourceLocation, UnbakedModel> unbakedCache) {
		return unbakedCache.get(getModelLocation(0, itemID));
	}
	
	private ResourceLocation getModelLocation(int destruction, ResourceLocation blockID) {
		String namespace = blockID.getNamespace();
		String path = "block/" + blockID.getPath() + "_top_" + destruction;
		return new ResourceLocation(namespace, path);
	}
	
	@Override
	public BlockItem getCustomItem(ResourceLocation blockID, FabricItemSettings settings) {
		return new BaseAnvilItem(this, settings);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		int destruction = state.getValue(DESTRUCTION);
		int durability = state.getValue(getDurabilityProp());
		int value = destruction * getMaxDurability() + durability;
		ItemStack tool = builder.getParameter(LootContextParams.TOOL);
		if (tool != null && tool.getItem() instanceof PickaxeItem) {
			ItemStack itemStack = new ItemStack(this);
			itemStack.getOrCreateTag().putInt(BaseAnvilItem.DESTRUCTION, value);
			return Lists.newArrayList(itemStack);
		}
		return Collections.emptyList();
	}
	
	public IntegerProperty getDurabilityProp() {
		return durability;
	}
	
	public int getMaxDurability() {
		return 3;
	}
	
	public BlockState damageAnvilUse(BlockState state, Random random) {
		IntegerProperty durability = getDurabilityProp();
		int value = state.getValue(durability);
		if (value < getMaxDurability() && random.nextInt(8) == 0) {
			return state.setValue(durability, value + 1);
		}
		value = state.getValue(DESTRUCTION);
		return value < 2 ? state.setValue(DESTRUCTION, value + 1).setValue(durability, 0) : null;
	}
	
	public BlockState damageAnvilFall(BlockState state) {
		int destruction = state.getValue(DESTRUCTION);
		return destruction < 2 ? state.setValue(DESTRUCTION, destruction + 1) : null;
	}
}
