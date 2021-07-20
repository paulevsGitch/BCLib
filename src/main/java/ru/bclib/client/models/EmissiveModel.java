package ru.bclib.client.models;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class EmissiveModel implements FabricBakedModel, BakedModel, UnbakedModel {
	private static final Material[] SPRITE_IDS = new Material[] {
		new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("minecraft:block/furnace_front_on")),
		new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("minecraft:block/furnace_top"))
	};
	private TextureAtlasSprite[] SPRITES = new TextureAtlasSprite[2];
	
	private ItemTransforms transformation;
	private TextureAtlasSprite particles;
	private Mesh mesh;
	
	public EmissiveModel(BlockModel source) {
		transformation = source == null ? ItemTransforms.NO_TRANSFORMS : source.getTransforms();
	}
	
	// FabricBakedModel //
	
	@Override
	public boolean isVanillaAdapter() {
		return false;
	}
	
	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(mesh);
	}
	
	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		context.meshConsumer().accept(mesh);
	}
	
	// BakedModel //
	
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean useAmbientOcclusion() {
		return false;
	}
	
	@Override
	public boolean isGui3d() {
		return true;
	}
	
	@Override
	public boolean usesBlockLight() {
		return true;
	}
	
	@Override
	public boolean isCustomRenderer() {
		return true;
	}
	
	@Override
	public TextureAtlasSprite getParticleIcon() {
		return particles;
	}
	
	@Override
	public ItemTransforms getTransforms() {
		return transformation;
	}
	
	@Override
	public ItemOverrides getOverrides() {
		return ItemOverrides.EMPTY;
	}
	
	// UnbakedModel //
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> function, Set<Pair<String, String>> set) {
		return Collections.EMPTY_LIST;
	}
	
	@Nullable
	@Override
	public BakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState, ResourceLocation resourceLocation) {
		for(int i = 0; i < 2; ++i) {
			SPRITES[i] = textureGetter.apply(SPRITE_IDS[i]);
		}
		particles = SPRITES[0];
		// Build the mesh using the Renderer API
		Renderer renderer = RendererAccess.INSTANCE.getRenderer();
		MeshBuilder builder = renderer.meshBuilder();
		QuadEmitter emitter = builder.getEmitter();
		
		for(Direction direction : Direction.values()) {
			int spriteIdx = direction == Direction.UP || direction == Direction.DOWN ? 1 : 0;
			// Add a new face to the mesh
			emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
			// Set the sprite of the face, must be called after .square()
			// We haven't specified any UV coordinates, so we want to use the whole texture. BAKE_LOCK_UV does exactly that.
			emitter.spriteBake(0, SPRITES[spriteIdx], MutableQuadView.BAKE_LOCK_UV);
			// Enable texture usage
			emitter.spriteColor(0, -1, -1, -1, -1);
			// Add the quad to the mesh
			emitter.emit();
		}
		mesh = builder.build();
		
		return this;
	}
}
