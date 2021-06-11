package ru.bclib.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import ru.bclib.blocks.BaseAnvilBlock;
import ru.bclib.client.models.ItemModelProvider;

import java.util.List;

public class BaseAnvilItem extends BlockItem implements ItemModelProvider {

	public final static String DESTRUCTION = "destruction";

	public BaseAnvilItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = super.getPlacementState(blockPlaceContext);
		ItemStack stack = blockPlaceContext.getItemInHand();
		int destruction = stack.getOrCreateTag().getInt(DESTRUCTION);
		blockState = blockState.setValue(BaseAnvilBlock.DESTRUCTION, destruction);
		return blockState;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, level, list, tooltipFlag);
		int l = itemStack.getOrCreateTag().getInt(DESTRUCTION);
		if (l > 0) {
			list.add(new TranslatableComponent("message.bclib.anvil_damage").append(": " + l));
		}
	}

	@Override
	public BlockModel getItemModel(ResourceLocation resourceLocation) {
		return ((ItemModelProvider) getBlock()).getItemModel(resourceLocation);
	}
}
