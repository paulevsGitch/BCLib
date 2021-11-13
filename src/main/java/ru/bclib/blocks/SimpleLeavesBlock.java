package ru.bclib.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import ru.bclib.api.TagAPI;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.interfaces.RenderLayerProvider;

public class SimpleLeavesBlock extends BaseBlockNotFull implements RenderLayerProvider {
	public SimpleLeavesBlock(MaterialColor color) {
		super(FabricBlockSettings.of(Material.LEAVES)
								 .strength(0.2F)
								 .mapColor(color)
								 .sound(SoundType.GRASS)
								 .noOcclusion()
								 .isValidSpawn((state, world, pos, type) -> false)
								 .isSuffocating((state, world, pos) -> false)
								 .isViewBlocking((state, world, pos) -> false));

		TagAPI.addTags(this, TagAPI.BLOCK_LEAVES);
	}
	
	public SimpleLeavesBlock(MaterialColor color, int light) {
		super(FabricBlockSettings.of(Material.LEAVES)
								 .luminance(light)
								 .mapColor(color)
								 .strength(0.2F)
								 .sound(SoundType.GRASS)
								 .noOcclusion()
								 .isValidSpawn((state, world, pos, type) -> false)
								 .isSuffocating((state, world, pos) -> false)
								 .isViewBlocking((state, world, pos) -> false));

		TagAPI.addTags(this, TagAPI.BLOCK_LEAVES);
	}
	
	@Override
	public BCLRenderLayer getRenderLayer() {
		return BCLRenderLayer.CUTOUT;
	}
}