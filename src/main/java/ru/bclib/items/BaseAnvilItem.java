package ru.bclib.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Registry;
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
import ru.bclib.blocks.BaseAnvilBlock;
import ru.bclib.client.models.ItemModelProvider;

public class BaseAnvilItem extends BlockItem implements ItemModelProvider {

	public final static String DESTRUCTION = "destruction";

	public BaseAnvilItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = super.getPlacementState(blockPlaceContext);
		ItemStack stack = blockPlaceContext.getItemInHand();
		int destruction = stack.getOrCreateTag().getInt(DESTRUCTION);
		if (blockState != null) {
			blockState = blockState.setValue(BaseAnvilBlock.DESTRUCTION, destruction);
		}
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
	@Environment(EnvType.CLIENT)
	public BlockModel getItemModel(ResourceLocation resourceLocation) {
		Block anvilBlock = getBlock();
		ResourceLocation blockId = Registry.BLOCK.getKey(anvilBlock);
		return ((ItemModelProvider) anvilBlock).getItemModel(blockId);
	}
}
