package ru.bclib.blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.interfaces.RenderLayerProvider;
import ru.bclib.interfaces.tools.AddMineablePickaxe;

import java.util.Collections;
import java.util.List;

public class BaseGlassBlock extends BaseBlockNotFull implements AddMineablePickaxe, RenderLayerProvider {
    public BaseGlassBlock(Block block) {
        this(block, 0.3f);
    }
    public BaseGlassBlock(Block block, float resistance) {
        super(FabricBlockSettings.copyOf(block)
                .resistance(resistance)
                .nonOpaque()
                .isSuffocating((arg1, arg2, arg3) -> false)
                .isViewBlocking((arg1, arg2, arg3) -> false));
    }

    @Environment(EnvType.CLIENT)
    public float getShadeBrightness(BlockState state, BlockGetter view, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter view, BlockPos pos) {
        return true;
    }

    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    public boolean skipRendering(BlockState state, BlockState neighbor, Direction facing) {
        return neighbor.getBlock() == this ? true : super.skipRendering(state, neighbor, facing);
    }

    @Override
    public List<ItemStack> getLoot(BlockState state, LootContext.Builder builder) {
        ItemStack tool = builder.getParameter(LootContextParams.TOOL);
        if (tool != null && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
            return Collections.singletonList(new ItemStack(this));
        }
        return Collections.emptyList();
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.TRANSLUCENT;
    }
}
