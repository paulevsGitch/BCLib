package ru.bclib.interfaces;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface SurvivesOnSpecialGround {
    String getSurvivableBlocksString();

    @Environment(EnvType.CLIENT)
    static List<String>  splitLines(String input){
        final int MAX_LEN = 30;
        final int MAX_LEN_SECOND = 40;
        List<String> lines = Lists.newArrayList();
        int maxLen = MAX_LEN;

        while (input.length()>maxLen){
            int idx = input.lastIndexOf(",", maxLen);
            if (idx>=0) {
                lines.add( input.substring(0, idx+1).trim());
                input = input.substring(idx+1, input.length()).trim();
            } else {
                break;
            }

            maxLen = MAX_LEN_SECOND;
        }
        lines.add(input.trim());

        return lines;
    }

    @Environment(EnvType.CLIENT)
    static void appendHoverText(List<Component> list, String description) {
        final int MAX_LINES = 5;
        List<String>  lines = splitLines(description);
        if (lines.size()>0) {
            list.add(new TranslatableComponent("tooltip.bclib.place_on", lines.get(0)).withStyle(ChatFormatting.GREEN));
        }
        for (int i=1; i<Math.min(lines.size(),MAX_LINES); i++){
            String line = lines.get(i);
            if (i==MAX_LINES-1 && i<lines.size()-1) line +=" ...";
            list.add(new TextComponent("    " + line).withStyle(ChatFormatting.GREEN));
        }
    }

    boolean isSurvivable(BlockState state);

   default boolean canSurviveOnTop(BlockState state, LevelReader world, BlockPos pos) {
        return isSurvivable(world.getBlockState(pos.below()));
    }

    default boolean canSurviveOnBottom(BlockState state, LevelReader world, BlockPos pos) {
        return isSurvivable(world.getBlockState(pos.above()));
    }
}