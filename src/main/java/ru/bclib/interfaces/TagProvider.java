package ru.bclib.interfaces;

import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

public interface TagProvider {
	void addTags(List<Named<Block>> blockTags, List<Tag.Named<Item>> itemTags);
}
