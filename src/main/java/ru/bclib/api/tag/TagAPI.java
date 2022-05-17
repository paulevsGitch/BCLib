package ru.bclib.api.tag;

import com.google.common.collect.Maps;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;

import ru.bclib.api.biomes.BiomeAPI;
import ru.bclib.mixin.common.DiggerItemAccessor;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class TagAPI {
	private static final Map<String, TagType<?>> TYPES = Maps.newHashMap();

	public static TagType.RegistryBacked<Block> BLOCKS = registerType(Registry.BLOCK);
	public static TagType.RegistryBacked<Item> ITEMS = registerType(Registry.ITEM);
	public static TagType.Simple<Biome> BIOMES = registerType(Registry.BIOME_REGISTRY, "tags/worldgen/biome", b->BiomeAPI.getBiomeID(b));

	private static <T> TagType.RegistryBacked<T> registerType(DefaultedRegistry<T> registry) {
		TagType<T> type = new TagType.RegistryBacked<>(registry);
		return (TagType.RegistryBacked<T>)TYPES.computeIfAbsent(type.directory, (dir)->type);
	}

	public static <T> TagType.Simple<T> registerType(ResourceKey<? extends Registry<T>> registry, String directory, Function<T, ResourceLocation> locationProvider) {
		return (TagType.Simple<T>)TYPES.computeIfAbsent(directory, (dir)->new TagType.Simple<>(registry, dir, locationProvider));
	}

	public static <T> TagType.UnTyped<T> registerType(ResourceKey<? extends Registry<T>> registry, String directory) {
		return (TagType.UnTyped<T>)TYPES.computeIfAbsent(directory, (dir)->new TagType.UnTyped<>(registry, dir));
	}



	
	/**
	 * Get or create {@link TagKey}.
	 *
	 * @param registry - {@link Registry<T>} tag collection;
	 * @param id				- {@link ResourceLocation} tag id.
	 * @return {@link TagKey}.
	 */
	@Deprecated(forRemoval = true)
	public static <T> TagKey<T> makeTag(Registry<T> registry, TagLocation<T> id) {
		return registry
				.getTagNames()
				.filter(tagKey -> tagKey.location().equals(id))
				.findAny()
				.orElse(TagKey.create(registry.key(), id));
	}

	/**
	 * Get or create {@link Block} {@link TagKey} with mod namespace.
	 *
	 * @param modID - {@link String} mod namespace (mod id);
	 * @param name  - {@link String} tag name.
	 * @return {@link Block} {@link TagKey}.
	 */
	public static TagKey<Biome> makeBiomeTag(String modID, String name) {
		return BIOMES.makeTag(new ResourceLocation(modID, name));
	}

	
	/**
	 * Get or create {@link Block} {@link TagKey} with mod namespace.
	 *
	 * @param modID - {@link String} mod namespace (mod id);
	 * @param name  - {@link String} tag name.
	 * @return {@link Block} {@link TagKey}.
	 */
	public static TagKey<Block> makeBlockTag(String modID, String name) {
		return BLOCKS.makeTag(new ResourceLocation(modID, name));
	}

	/**
	 * Get or create {@link Block} {@link TagKey} with mod namespace.
	 *
	 * @param id - {@link String} id for the tag;
	 * @return {@link Block} {@link TagKey}.
	 */
	public static TagKey<Block> makeBlockTag(ResourceLocation id) {
		return BLOCKS.makeTag(id);
	}
	
	/**
	 * Get or create {@link Item} {@link TagKey} with mod namespace.
	 *
	 * @param modID - {@link String} mod namespace (mod id);
	 * @param name  - {@link String} tag name.
	 * @return {@link Item} {@link TagKey}.
	 */
	public static TagKey<Item> makeItemTag(String modID, String name) {
		return ITEMS.makeTag(new ResourceLocation(modID, name));
	}

	/**
	 * Get or create {@link Item} {@link TagKey} with mod namespace.
	 *
	 * @param id - {@link String} id for the tag;
	 * @return {@link Item} {@link TagKey}.
	 */
	public static TagKey<Item> makeItemTag(ResourceLocation id) {
		return ITEMS.makeTag(id);
	}
	
	/**
	 * Get or create {@link Block} {@link TagKey}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Block} {@link TagKey}.
	 * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
	 */
	public static TagKey<Block> makeCommonBlockTag(String name) {
		return BLOCKS.makeCommonTag(name);
	}
	
	/**
	 * Get or create {@link Item} {@link TagKey}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Item} {@link TagKey}.
	 * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
	 */
	public static TagKey<Item> makeCommonItemTag(String name) {
		return ITEMS.makeCommonTag(name);
	}

	public static TagKey<Biome> makeCommonBiomeTag(String name) {
		return BIOMES.makeCommonTag(name);
	}
	
	/**
	 * Initializes basic tags. Should be called only in BCLib main class.
	 */
	public static void init() {
		addBlockTag(CommonBlockTags.BOOKSHELVES, Blocks.BOOKSHELF);
		addBlockTag(CommonBlockTags.CHEST, Blocks.CHEST);
		addItemTag(CommonItemTags.CHEST, Items.CHEST);
		addItemTag(CommonItemTags.IRON_INGOTS, Items.IRON_INGOT);
		addItemTag(CommonItemTags.FURNACES, Blocks.FURNACE);
	}

	/**
	 * Adds multiple Tags to one Biome.
	 * @param tagIDs array of {@link TagLocation<Biome>} tag IDs.
	 * @param biome The {@link Biome} to add tag.
	 */
	@SafeVarargs
	@Deprecated(forRemoval = true)
	public static void addBiomeTags(Biome biome, TagLocation<Biome>... tagIDs) {
		BIOMES.add(biome, tagIDs);
	}

	/**
	 * Adds one Tag to multiple Biomes.
	 * @param tagID {@link TagLocation<Biome>} tag ID.
	 * @param biomes array of {@link Biome} to add into tag.
	 */

	@Deprecated(forRemoval = true)
	public static void addBiomeTag(TagLocation<Biome> tagID, Biome... biomes) {
		BIOMES.add(tagID, biomes);
	}

	/**
	 * Adds one Tag to multiple Biomes.
	 * @param tagID {@link TagKey<Biome>} tag ID.
	 * @param biomes array of {@link Biome} to add into tag.
	 */
	public static void addBiomeTag(TagKey<Biome> tagID, Biome... biomes) {
		BIOMES.add(tagID, biomes);
	}


	/**
	 * Adds multiple Tags to one Block.
	 * @param tagIDs array of {@link TagLocation<Block>} tag IDs.
	 * @param block The {@link Block} to add tag.
	 */
	@SafeVarargs
	@Deprecated(forRemoval = true)
	public static void addBlockTags(Block block, TagLocation<Block>... tagIDs) {
		BLOCKS.add(block, tagIDs);
	}
	
	/**
	 * Adds one Tag to multiple Blocks.
	 * @param tagID {@link TagLocation<Block>} tag ID.
	 * @param blocks array of {@link Block} to add into tag.
	 */
	@Deprecated(forRemoval = true)
	public static void addBlockTag(TagLocation<Block> tagID, Block... blocks) {
		BLOCKS.add(tagID, blocks);
	}

	/**
	 * Adds one Tag to multiple Blocks.
	 * @param tagID {@link TagKey<Block>} tag ID.
	 * @param blocks array of {@link Block} to add into tag.
	 */
	public static void addBlockTag(TagKey<Block> tagID, Block... blocks) {
		BLOCKS.add(tagID, blocks);
	}
	
	/**
	 * Adds multiple Tags to one Item.
	 * @param tagIDs array of {@link TagLocation<Item>} tag IDs.
	 * @param item The {@link Item} to add tag.
	 */
	@Deprecated(forRemoval = true)
	@SafeVarargs
	public static void addItemTags(ItemLike item, TagLocation<Item>... tagIDs) {
		ITEMS.add(item.asItem(), tagIDs);
	}
	
	/**
	 * Adds one Tag to multiple Items.
	 * @param tagID {@link TagLocation<Item>} tag ID.
	 * @param items array of {@link ItemLike} to add into tag.
	 */
	@Deprecated(forRemoval = true)
	public static void addItemTag(TagLocation<Item> tagID, Item... items) {
		ITEMS.add(tagID, items);
	}

	/**
	 * Adds one Tag to multiple Items.
	 * @param tagID {@link TagLocation<Item>} tag ID.
	 * @param items array of {@link ItemLike} to add into tag.
	 */
	@Deprecated(forRemoval = true)
	public static void addItemTag(TagLocation<Item> tagID, ItemLike... items) {
		for(ItemLike i : items) {
			ITEMS.add(i.asItem(), tagID);
		}
	}

	/**
	 * Adds one Tag to multiple Items.
	 * @param tagID {@link TagKey<Item>} tag ID.
	 * @param items array of {@link ItemLike} to add into tag.
	 */
	public static void addItemTag(TagKey<Item> tagID, ItemLike... items) {
		for(ItemLike i : items){
			ITEMS.add(i.asItem(), tagID);
		}
	}

	/**
	* Adds one Tag to multiple Items.
	* @param tagID {@link TagKey<Item>} tag ID.
	* @param items array of {@link ItemLike} to add into tag.
	*/
	public static void addItemTag(TagKey<Item> tagID, Item... items) {
		ITEMS.add(tagID, items);
	}



	/**
	 * Automatically called in {@link net.minecraft.tags.TagLoader#loadAndBuild(ResourceManager)}.
	 * <p>
	 * In most cases there is no need to call this Method manually.
	 *
	 * @param directory The name of the Tag-directory. Should be either <i>"tags/blocks"</i> or
	 *				  <i>"tags/items"</i>.
	 * @param tagsMap   The map that will hold the registered Tags
	 * @return The {@code tagsMap} Parameter.
	 */
	public static <T> Map<ResourceLocation, Tag.Builder> apply(String directory, Map<ResourceLocation, Tag.Builder> tagsMap) {

		TagType<?> type = TYPES.get(directory);
		if (type!=null){
			type.apply(tagsMap);
		}

//		final BiConsumer<ResourceLocation, Set<ResourceLocation>> consumer;
//		consumer = (id, ids) -> apply(tagsMap.computeIfAbsent(id, key -> Tag.Builder.tag()), ids);
//
//		if ("tags/blocks".equals(directory)) {
//			TAGS_BLOCK.forEach(consumer);
//		}
//		else if ("tags/items".equals(directory)) {
//			TAGS_ITEM.forEach(consumer);
//		}
//		else if ("tags/worldgen/biome".equals(directory)) {
//			TAGS_BIOME.forEach(consumer);
//		}
		return tagsMap;
	}
	
	/**
	 * Adds all {@code ids} to the {@code builder}.
	 *
	 * @param builder
	 * @param ids
	 * @return The Builder passed as {@code builder}.
	 */
	public static Tag.Builder apply(Tag.Builder builder, Set<ResourceLocation> ids) {
		ids.forEach(value -> builder.addElement(value, "BCLib Code"));
		return builder;
	}


	/**
	 * Extends (without changing) {@link ResourceLocation}. This Type was introduced to allow type-safe definition af
	 * Tags using their ResourceLocation.
	 * @param <T> The Type of the underlying {@link Tag}
	 */
	public static class TagLocation<T> extends ResourceLocation {
		public TagLocation(String string) {
			super(string);
		}

		public TagLocation(String string, String string2) {
			super(string, string2);
		}

		public TagLocation(ResourceLocation location) {
			super(location.getNamespace(), location.getPath());
		}

		public static<R> TagLocation<R> of(TagKey<R> tag){
			return new TagLocation<R>(tag.location());
		}
	}

	public static boolean isToolWithMineableTag(ItemStack stack, TagKey<Block> tag){
		return isToolWithUntypedMineableTag(stack, tag.location());
	}

	public static boolean isToolWithMineableTag(ItemStack stack, TagLocation<Block> tag){
		return isToolWithUntypedMineableTag(stack, tag);
	}

	private static boolean isToolWithUntypedMineableTag(ItemStack stack, ResourceLocation tag){
		if (stack.getItem() instanceof DiggerItemAccessor dig){
			return dig.bclib_getBlockTag().location().equals(tag);
		}
		return false;
	}
}
