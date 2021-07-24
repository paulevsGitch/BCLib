package ru.bclib.client.models;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class OBJBlockModel implements UnbakedModel, BakedModel {
	private static final byte[] QUAD_INDEXES = new byte[] {0, 1, 2, 0, 2, 3};
	
	protected TextureAtlasSprite[] sprites;
	protected TextureAtlasSprite particles;
	protected ItemTransforms transforms;
	protected ItemOverrides overrides;
	
	protected List<UnbakedQuad> quadsUnbaked;
	protected Material particleMaterial;
	protected List<Material> materials;
	protected List<BakedQuad> quads;
	
	public OBJBlockModel(ResourceLocation location, ResourceLocation particleTextureID, ResourceLocation... textureIDs) {
		transforms = ItemTransforms.NO_TRANSFORMS;
		overrides = ItemOverrides.EMPTY;
		
		quadsUnbaked = Lists.newArrayList();
		materials = Lists.newArrayList();
		
		loadModel(location, textureIDs);
		
		quads = new ArrayList<>(quadsUnbaked.size());
		particleMaterial = new Material(TextureAtlas.LOCATION_BLOCKS, particleTextureID);
		sprites = new TextureAtlasSprite[materials.size()];
	}
	
	// UnbakedModel //
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Collections.emptyList();
	}
	
	@Override
	public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> function, Set<Pair<String, String>> set) {
		return materials;
	}
	
	@Nullable
	@Override
	public BakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState, ResourceLocation resourceLocation) {
		for (int i = 0; i < sprites.length; i++) {
			sprites[i] = textureGetter.apply(materials.get(i));
		}
		particles = textureGetter.apply(particleMaterial);
		quads.clear();
		quadsUnbaked.forEach(quad -> quads.add(quad.bake(sprites)));
		return this;
	}
	
	// Baked Model //
	
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
		return direction == null ? quads : Collections.emptyList();
	}
	
	@Override
	public boolean useAmbientOcclusion() {
		return true;
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
		return false;
	}
	
	@Override
	public TextureAtlasSprite getParticleIcon() {
		return particles;
	}
	
	@Override
	public ItemTransforms getTransforms() {
		return transforms;
	}
	
	@Override
	public ItemOverrides getOverrides() {
		return overrides;
	}
	
	private Resource getResource(ResourceLocation location) {
		Resource resource = null;
		try {
			resource = Minecraft.getInstance().getResourceManager().getResource(location);
		}
		catch (IOException e) {
			e.printStackTrace();
			if (resource != null) {
				try {
					resource.close();
				}
				catch (IOException ioException) {
					ioException.printStackTrace();
				}
				resource = null;
			}
		}
		return resource;
	}
	
	private void loadModel(ResourceLocation location, ResourceLocation[] textureIDs) {
		Resource resource = getResource(location);
		if (resource == null) {
			return;
		}
		InputStream input = resource.getInputStream();
		
		List<Float> vertecies = new ArrayList<>(12);
		List<Float> uvs = new ArrayList<>(8);
		
		List<Integer> vertexIndex = new ArrayList<>(4);
		List<Integer> uvIndex = new ArrayList<>(4);
		
		byte materialIndex = 0;
		int vertCount = 0;
		
		try {
			InputStreamReader streamReader = new InputStreamReader(input);
			BufferedReader reader = new BufferedReader(streamReader);
			String string;
			
			while ((string = reader.readLine()) != null) {
				if ((string.startsWith("usemtl") || string.startsWith("g")) && vertCount != vertecies.size()) {
					vertCount = vertecies.size();
					materialIndex++;
				}
				else if (string.startsWith("vt")) {
					String[] uv = string.split(" ");
					uvs.add(Float.parseFloat(uv[1]));
					uvs.add(Float.parseFloat(uv[2]));
				}
				else if (string.startsWith("v")) {
					String[] vert = string.split(" ");
					for (int i = 1; i < 4; i++) {
						vertecies.add(Float.parseFloat(vert[i]));
					}
				}
				else if (string.startsWith("f")) {
					String[] members = string.split(" ");
					if (members.length != 5) {
						System.out.println("Only quads in OBJ are supported! Model [" + location + "] has n-gons or triangles!");
						continue;
					}
					vertexIndex.clear();
					uvIndex.clear();
					
					for (int i = 1; i < members.length; i++) {
						String member = members[i];
						
						if (member.contains("/")) {
							String[] sub = member.split("/");
							vertexIndex.add(Integer.parseInt(sub[0]) - 1); // Vertex
							uvIndex.add(Integer.parseInt(sub[1]) - 1);     // UV
						}
						else {
							vertexIndex.add(Integer.parseInt(member) - 1); // Vertex
						}
					}
					
					boolean hasUV = !uvIndex.isEmpty();
					UnbakedQuad quad = new UnbakedQuad();
					for (int i = 0; i < 4; i++) {
						int index = vertexIndex.get(i) * 3;
						int quadIndex = i * 5;
						quad.addData(quadIndex++, vertecies.get(index++)); // X
						quad.addData(quadIndex++, vertecies.get(index++)); // Y
						quad.addData(quadIndex++, vertecies.get(index));   // Z
						if (hasUV) {
							index = uvIndex.get(i) * 2;
							quad.addData(quadIndex++, uvs.get(index++) * 16F);   // U
							quad.addData(quadIndex, (1 - uvs.get(index)) * 16F); // V
						}
					}
					quad.setSpriteIndex(materialIndex);
					quadsUnbaked.add(quad);
				}
			}
			
			reader.close();
			streamReader.close();
			input.close();
			resource.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		int maxID = textureIDs.length - 1;
		for (int i = 0; i <= materialIndex; i++) {
			int index = Math.min(materialIndex, maxID);
			materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, textureIDs[index]));
		}
	}
}
