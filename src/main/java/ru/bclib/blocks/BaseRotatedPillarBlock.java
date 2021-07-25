package ru.bclib.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;
import ru.bclib.interfaces.BlockModelProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BaseRotatedPillarBlock extends RotatedPillarBlock implements BlockModelProvider {
	public BaseRotatedPillarBlock(Properties settings) {
		super(settings);
	}
	
	public BaseRotatedPillarBlock(Block block) {
		super(FabricBlockSettings.copyOf(block));
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		return Collections.singletonList(new ItemStack(this));
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void registerModels(ResourceLocation blockID, Map<ResourceLocation, UnbakedModel> modelRegistry, Map<ResourceLocation, UnbakedModel> unbakedCache) {
		Optional<String> pattern = createBlockPattern(blockID);
		modelRegistry.put(blockID, ModelsHelper.fromPattern(pattern));
	}
	
	@Environment(EnvType.CLIENT)
	public ResourceLocation getStateModel(ResourceLocation blockID, ResourceLocation stateId, BlockState blockState) {
		return blockID;
	}
	
	protected Optional<String> createBlockPattern(ResourceLocation blockId) {
		return PatternsHelper.createBlockPillar(blockId);
	}
}
