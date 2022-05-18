package ru.bclib.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import ru.bclib.api.tag.NamedBlockTags;
import ru.bclib.api.tag.NamedItemTags;

import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.interfaces.RenderLayerProvider;
import ru.bclib.interfaces.TagProvider;
import ru.bclib.interfaces.tools.AddMineableHoe;
import ru.bclib.interfaces.tools.AddMineableShears;

import java.util.List;

public class SimpleLeavesBlock extends BaseBlockNotFull implements RenderLayerProvider, TagProvider, AddMineableShears, AddMineableHoe {
	public SimpleLeavesBlock(MaterialColor color) {
		this(
			FabricBlockSettings
				.of(Material.LEAVES)
				.strength(0.2F)
				.color(color)
				.sound(SoundType.GRASS)
				.noOcclusion()
				.isValidSpawn((state, world, pos, type) -> false)
				.isSuffocating((state, world, pos) -> false)
				.isViewBlocking((state, world, pos) -> false)
		);
	}
	
	public SimpleLeavesBlock(MaterialColor color, int light) {
		this(
			FabricBlockSettings
				.of(Material.LEAVES)
				.luminance(light)
				.color(color)
				.strength(0.2F)
				.sound(SoundType.GRASS)
				.noOcclusion()
				.isValidSpawn((state, world, pos, type) -> false)
				.isSuffocating((state, world, pos) -> false)
				.isViewBlocking((state, world, pos) -> false)
		);
	}
	
	public SimpleLeavesBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}
	
	@Override
	public BCLRenderLayer getRenderLayer() {
		return BCLRenderLayer.CUTOUT;
	}
	
	@Override
	public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
		blockTags.add(NamedBlockTags.LEAVES);
		itemTags.add(NamedItemTags.LEAVES);
	}
}