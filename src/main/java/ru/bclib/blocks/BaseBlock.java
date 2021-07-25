package ru.bclib.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;
import ru.bclib.interfaces.BlockModelProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base class for a default Block.
 * <p>
 * This Block-Type will:
 * <ul>
 *     <li>Drop itself</li>
 *     <li>Automatically create an Item-Model from the Block-Model</li>
 * </ul>
 */
public class BaseBlock extends Block implements BlockModelProvider {
	/**
	 * Creates a new Block with the passed properties
	 *
	 * @param settings The properties of the Block.
	 */
	public BaseBlock(Properties settings) {
		super(settings);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation will drop the Block itself
	 */
	@Override
	@SuppressWarnings("deprecation")
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		return Collections.singletonList(new ItemStack(this));
	}
	
	@Environment(EnvType.CLIENT)
	public void registerModels(ResourceLocation blockID, Map<ResourceLocation, UnbakedModel> modelRegistry, Map<ResourceLocation, UnbakedModel> unbakedCache) {
		modelRegistry.put(blockID, ModelsHelper.createBlockSimple(blockID));
	}
	
	/**
	 * This method is used internally.
	 * <p>
	 * It is called from Block-Contructors, to allow the augmentation of the blocks
	 * preset properties.
	 * <p>
	 * For example in {@link BaseLeavesBlock#BaseLeavesBlock(Block, MaterialColor, Consumer)}
	 *
	 * @param customizeProperties A {@link Consumer} to call with the preset properties
	 * @param settings            The properties as created by the Block
	 * @return The reconfigured {@code settings}
	 */
	static FabricBlockSettings acceptAndReturn(Consumer<FabricBlockSettings> customizeProperties, FabricBlockSettings settings) {
		customizeProperties.accept(settings);
		return settings;
	}
}