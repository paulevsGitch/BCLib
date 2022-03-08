package ru.bclib.api.tag;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import ru.bclib.mixin.common.DiggerItemAccessor;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class TagAPI {
	private static final Map<ResourceLocation, Set<ResourceLocation>> TAGS_BLOCK = Maps.newConcurrentMap();
	private static final Map<ResourceLocation, Set<ResourceLocation>> TAGS_ITEM = Maps.newConcurrentMap();
	
	/**
	 * Get or create {@link TagKey}.
	 *
	 * @param registry - {@link Registry<T>} tag collection;
	 * @param id				- {@link ResourceLocation} tag id.
	 * @return {@link TagKey}.
	 */
	public static <T> TagKey<T> makeTag(Registry<T> registry, TagLocation<T> id) {
		//TODO: 1.18.2 check if registry.key() gets the correct result
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
	public static TagKey<Block> makeBlockTag(String modID, String name) {
		return makeTag(Registry.BLOCK, new TagLocation<>(modID, name));
	}
	
	/**
	 * Get or create {@link Item} {@link TagKey} with mod namespace.
	 *
	 * @param modID - {@link String} mod namespace (mod id);
	 * @param name  - {@link String} tag name.
	 * @return {@link Item} {@link TagKey}.
	 */
	public static TagKey<Item> makeItemTag(String modID, String name) {
		return makeTag(Registry.ITEM, new TagLocation<>(modID, name));
	}
	
	/**
	 * Get or create {@link Block} {@link TagKey}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Block} {@link TagKey}.
	 * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
	 */
	public static TagKey<Block> makeCommonBlockTag(String name) {
		return makeTag(Registry.BLOCK, new TagLocation<>("c", name));
	}
	
	/**
	 * Get or create {@link Item} {@link TagKey}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Item} {@link TagKey}.
	 * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
	 */
	public static TagKey<Item> makeCommonItemTag(String name) {
		return makeTag(Registry.ITEM, new TagLocation<>("c", name));
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
	 * Adds multiple Tags to one Block.
	 * @param tagIDs array of {@link TagLocation<Block>} tag IDs.
	 * @param block The {@link Block} to add tag.
	 */
	@SafeVarargs
	public static void addBlockTags(Block block, TagLocation<Block>... tagIDs) {
		for (TagLocation<Block> tagID : tagIDs) {
			addBlockTagUntyped(tagID, block);
		}
	}
	
	/**
	 * Adds one Tag to multiple Blocks.
	 * @param tagID {@link TagLocation<Block>} tag ID.
	 * @param blocks array of {@link Block} to add into tag.
	 */
	public static void addBlockTag(TagLocation<Block> tagID, Block... blocks) {
		addBlockTagUntyped(tagID, blocks);
	}

	/**
	 * Adds one Tag to multiple Blocks.
	 * @param tagID {@link TagKey<Block>} tag ID.
	 * @param blocks array of {@link Block} to add into tag.
	 */
	public static void addBlockTag(TagKey<Block> tagID, Block... blocks) {
		addBlockTagUntyped(tagID.location(), blocks);
	}
	
	/**
	 * Adds one Tag to multiple Blocks.
	 * @param tagID {@link ResourceLocation} tag ID.
	 * @param blocks array of {@link Block} to add into tag.
	 */
	protected static void addBlockTagUntyped(ResourceLocation tagID, Block... blocks) {
		Set<ResourceLocation> set = TAGS_BLOCK.computeIfAbsent(tagID, k -> Sets.newHashSet());
		for (Block block : blocks) {
			ResourceLocation id = Registry.BLOCK.getKey(block);
			if (id != Registry.BLOCK.getDefaultKey()) {
				set.add(id);
			}
		}
	}
	
	/**
	 * Adds multiple Tags to one Item.
	 * @param tagIDs array of {@link TagLocation<Item>} tag IDs.
	 * @param item The {@link Item} to add tag.
	 */
	@SafeVarargs
	public static void addItemTags(ItemLike item, TagLocation<Item>... tagIDs) {
		for (TagLocation<Item> tagID : tagIDs) {
			addItemTagUntyped(tagID, item);
		}
	}
	
	/**
	 * Adds one Tag to multiple Items.
	 * @param tagID {@link TagLocation<Item>} tag ID.
	 * @param items array of {@link ItemLike} to add into tag.
	 */
	public static void addItemTag(TagLocation<Item> tagID, ItemLike... items) {
		addItemTagUntyped(tagID, items);
	}

	/**
	 * Adds one Tag to multiple Items.
	 * @param tagID {@link TagKey<Item>} tag ID.
	 * @param items array of {@link ItemLike} to add into tag.
	 */
	public static void addItemTag(TagKey<Item> tagID, ItemLike... items) {
		addItemTagUntyped(tagID.location(), items);
	}
	
	/**
	 * Adds one Tag to multiple Items.
	 * @param tagID {@link ResourceLocation} tag ID.
	 * @param items array of {@link ItemLike} to add into tag.
	 */
	protected static void addItemTagUntyped(ResourceLocation tagID, ItemLike... items) {
		Set<ResourceLocation> set = TAGS_ITEM.computeIfAbsent(tagID, k -> Sets.newHashSet());
		for (ItemLike item : items) {
			ResourceLocation id = Registry.ITEM.getKey(item.asItem());
			if (id != Registry.ITEM.getDefaultKey()) {
				set.add(id);
			}
		}
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
		final BiConsumer<ResourceLocation, Set<ResourceLocation>> consumer;
		consumer = (id, ids) -> apply(tagsMap.computeIfAbsent(id, key -> Tag.Builder.tag()), ids);
		if ("tags/blocks".equals(directory)) {
			TAGS_BLOCK.forEach(consumer);
		}
		else if ("tags/items".equals(directory)) {
			TAGS_ITEM.forEach(consumer);
		}
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

	public static boolean isToolWithMineableTag(ItemStack stack, TagLocation<Block> tag){
		if (stack.getItem() instanceof DiggerItemAccessor dig){
			return dig.bclib_getBlockTag().location().equals(tag);
		}
		return false;
	}
}
