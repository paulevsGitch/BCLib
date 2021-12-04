package ru.bclib.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import ru.bclib.api.TagAPI;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.interfaces.RenderLayerProvider;

public class SimpleLeavesBlock extends BaseBlockNotFull implements RenderLayerProvider {
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
		// TODO handle all tags instead of adding them like this
		TagAPI.addTags(this, TagAPI.BLOCK_LEAVES);
	}
	
	@Override
	public BCLRenderLayer getRenderLayer() {
		return BCLRenderLayer.CUTOUT;
	}
}