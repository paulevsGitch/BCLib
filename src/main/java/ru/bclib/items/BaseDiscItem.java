package ru.bclib.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;
import ru.bclib.interfaces.ItemModelGetter;

public class BaseDiscItem extends RecordItem implements ItemModelGetter {
	public BaseDiscItem(int comparatorOutput, SoundEvent sound, Properties settings) {
		super(comparatorOutput, sound, settings);
	}
}
