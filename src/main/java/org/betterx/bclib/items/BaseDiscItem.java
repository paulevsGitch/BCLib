package org.betterx.bclib.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;

import org.betterx.bclib.interfaces.ItemModelProvider;

public class BaseDiscItem extends RecordItem implements ItemModelProvider {
    public BaseDiscItem(int comparatorOutput, SoundEvent sound, Properties settings) {
        super(comparatorOutput, sound, settings);
    }
}
