package org.betterx.bclib.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;
import java.util.Optional;

public class LootUtil {
    public static Optional<List<ItemStack>> getDrops(BlockBehaviour block,
                                                     BlockState state,
                                                     LootContext.Builder builder) {
        ResourceLocation tableID = block.getLootTable();
        if (tableID == BuiltInLootTables.EMPTY) {
            return Optional.empty();
        }

        final LootContext ctx = builder.withParameter(LootContextParams.BLOCK_STATE, state)
                                       .create(LootContextParamSets.BLOCK);
        final ServerLevel level = ctx.getLevel();
        final LootTable table = level.getServer().getLootTables().get(tableID);

        if (table == LootTable.EMPTY) return Optional.empty();
        return Optional.of(table.getRandomItems(ctx));
    }
}
