package ru.bclib.complexmaterials.entry;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import ru.bclib.api.TagAPI;
import ru.bclib.complexmaterials.ComplexMaterial;
import ru.bclib.registry.BlockRegistry;

import java.util.function.BiFunction;

public class BlockEntry extends ComplexMaterialEntry {
	final BiFunction<ComplexMaterial, FabricBlockSettings, Block> initFunction;
	final boolean hasItem;
	
	Tag.Named<Block>[] blockTags;
	Tag.Named<Item>[] itemTags;
	
	public BlockEntry(String suffix, BiFunction<ComplexMaterial, FabricBlockSettings, Block> initFunction) {
		this(suffix, true, initFunction);
	}
	
	public BlockEntry(String suffix, boolean hasItem, BiFunction<ComplexMaterial, FabricBlockSettings, Block> initFunction) {
		super(suffix);
		this.initFunction = initFunction;
		this.hasItem = hasItem;
	}
	
	public BlockEntry setBlockTags(Tag.Named<Block>... blockTags) {
		this.blockTags = blockTags;
		return this;
	}
	
	public BlockEntry setItemTags(Tag.Named<Item>... itemTags) {
		this.itemTags = itemTags;
		return this;
	}
	
	public Block init(ComplexMaterial material, FabricBlockSettings blockSettings, BlockRegistry registry) {
		ResourceLocation location = getLocation(material.getModID(), material.getBaseName());
		Block block = initFunction.apply(material, blockSettings);
		if (hasItem) {
			registry.register(location, block);
			if (itemTags != null) {
				TagAPI.addTags(block, itemTags);
			}
		}
		else {
			registry.registerBlockOnly(location, block);
		}
		if (blockTags != null) {
			TagAPI.addTags(block, blockTags);
		}
		return block;
	}
}
