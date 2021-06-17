package ru.bclib.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;
import ru.bclib.client.models.ItemModelProvider;

public class BaseDiscItem extends RecordItem implements ItemModelProvider {
	public BaseDiscItem(int comparatorOutput, SoundEvent sound, Properties settings) {
		super(comparatorOutput, sound, settings);
	}
}
