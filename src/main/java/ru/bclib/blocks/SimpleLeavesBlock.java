package ru.bclib.blocks;

import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import ru.bclib.api.TagAPI;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.interfaces.RenderLayerProvider;
import ru.bclib.interfaces.TagProvider;

import java.util.List;

public class SimpleLeavesBlock extends BaseBlockNotFull implements RenderLayerProvider, TagProvider {
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
	public void addTags(List<Named<Block>> blockTags, List<Named<Item>> itemTags) {
		blockTags.add(FabricMineableTags.SHEARS_MINEABLE);
		blockTags.add(TagAPI.MINEABLE_HOE);
		blockTags.add(BlockTags.LEAVES);
	}
}