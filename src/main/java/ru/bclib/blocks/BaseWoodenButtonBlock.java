package ru.bclib.blocks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import ru.bclib.util.BlocksHelper;

public class BaseWoodenButtonBlock extends BaseButtonBlock {

	public BaseWoodenButtonBlock(Block source) {
		super(source, BlocksHelper.copySettingsOf(source).strength(0.5F, 0.5F).noOcclusion(), true);
	}

	@Override
	protected SoundEvent getSound(boolean clicked) {
		return clicked ? SoundEvents.WOODEN_BUTTON_CLICK_ON : SoundEvents.WOODEN_BUTTON_CLICK_OFF;
	}
}
