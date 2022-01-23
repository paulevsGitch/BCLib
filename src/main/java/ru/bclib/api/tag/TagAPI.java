package ru.bclib.api.tag;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.impl.tag.extension.TagDelegate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Named;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

public class TagAPI {
	private static final Map<ResourceLocation, Set<ResourceLocation>> TAGS_BLOCK = Maps.newConcurrentMap();
	private static final Map<ResourceLocation, Set<ResourceLocation>> TAGS_ITEM = Maps.newConcurrentMap();
	
	/**
	 * Get or create {@link Tag.Named}.
	 *
	 * @param containerSupplier - {@link TagCollection} {@link Supplier} tag collection;
	 * @param id				- {@link ResourceLocation} tag id.
	 * @return {@link Tag.Named}.
	 */
	public static <T> TagNamed<T> makeTag(Supplier<TagCollection<T>> containerSupplier, TagLocation<T> id) {
		Tag<T> tag = containerSupplier.get().getTag(id);
		return tag == null ? new Delegate<>(id, containerSupplier) : CommonDelegate.proxy((Named<T>) tag);
	}
	
	/**
	 * Get or create {@link Block} {@link Tag.Named} with mod namespace.
	 *
	 * @param modID - {@link String} mod namespace (mod id);
	 * @param name  - {@link String} tag name.
	 * @return {@link Block} {@link Tag.Named}.
	 */
	public static TagNamed<Block> makeBlockTag(String modID, String name) {
		return makeTag(BlockTags::getAllTags, new TagLocation<>(modID, name));
	}
	
	/**
	 * Get or create {@link Item} {@link Tag.Named} with mod namespace.
	 *
	 * @param modID - {@link String} mod namespace (mod id);
	 * @param name  - {@link String} tag name.
	 * @return {@link Item} {@link Tag.Named}.
	 */
	public static TagNamed<Item> makeItemTag(String modID, String name) {
		return makeTag(ItemTags::getAllTags, new TagLocation<>(modID, name));
	}
	
	/**
	 * Get or create {@link Block} {@link Tag.Named}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Block} {@link Tag.Named}.
	 * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
	 */
	public static TagNamed<Block> makeCommonBlockTag(String name) {
		return makeTag(BlockTags::getAllTags, new TagLocation<>("c", name));
	}
	
	/**
	 * Get or create {@link Item} {@link Tag.Named}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Item} {@link Tag.Named}.
	 * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
	 */
	public static TagNamed<Item> makeCommonItemTag(String name) {
		return makeTag(ItemTags::getAllTags, new TagLocation<>("c", name));
	}
	
	/**
	 * Get or create Minecraft {@link Block} {@link Tag.Named}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Block} {@link Tag.Named}.
	 */
	@Deprecated(forRemoval = true)
	public static TagNamed<Block> getMCBlockTag(String name) {
		ResourceLocation id = new ResourceLocation(name);
		Tag<Block> tag = BlockTags.getAllTags().getTag(id);
		return CommonDelegate.proxy(tag == null ? (Named<Block>) TagFactory.BLOCK.create(id): (Named<Block>) tag);
	}
	
	/**
	 * Initializes basic tags. Should be called only in BCLib main class.
	 */
	public static void init() {
		addBlockTag(CommonBlockTags.BOOKSHELVES.getName(), Blocks.BOOKSHELF);
		addBlockTag(CommonBlockTags.CHEST.getName(), Blocks.CHEST);
		addItemTag(CommonItemTags.CHEST.getName(), Items.CHEST);
		addItemTag(CommonItemTags.IRON_INGOTS.getName(), Items.IRON_INGOT);
		addItemTag(CommonItemTags.FURNACES.getName(), Blocks.FURNACE);
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
	public static Map<ResourceLocation, Tag.Builder> apply(String directory, Map<ResourceLocation, Tag.Builder> tagsMap) {
		Map<ResourceLocation, Set<ResourceLocation>> tagMap = null;
		if ("tags/blocks".equals(directory)) {
			tagMap = TAGS_BLOCK;
		}
		else if ("tags/items".equals(directory)) {
			tagMap = TAGS_ITEM;
		}
		if (tagMap != null) {
			tagMap.forEach((id, ids) -> apply(tagsMap.computeIfAbsent(id, key -> Tag.Builder.tag()), ids));
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
	 * Extends {@link Tag.Named} to return a type safe {@link TagLocation}. This Type was introduced to
	 * allow type-safe definition of Tags using their ResourceLocation.
	 * @param <T> The Type of the underlying {@link Tag}
	 */
	public interface TagNamed<T> extends Tag.Named<T>{
		TagLocation<T> getName();
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

		public static<R> TagLocation<R> of(Tag.Named<R> tag){
			return new TagLocation<R>(tag.getName());
		}
	}
	
	private abstract static class CommonDelegate<T>  implements TagNamed<T> {
		protected final Tag.Named<T> delegate;
		protected CommonDelegate(Tag.Named<T> source){
			this.delegate = source;
		}
		
		public static<T> TagNamed<T> proxy(Tag.Named<T> source){
			if (source instanceof TagNamed typed) return typed;
			return new ProxyDelegate<>(source);
		}
		
		@Override
		public boolean contains(T object) {
			return delegate.contains(object);
		}
		
		@Override
		public List<T> getValues() {
			return delegate.getValues();
		}
		
		@Override
		public T getRandomElement(Random random) {
			return delegate.getRandomElement(random);
		}
	}
	
	private static final class ProxyDelegate<T> extends CommonDelegate<T>{
		private final TagLocation<T> id;
		private ProxyDelegate(Tag.Named<T> source) {
			super( source);
			id = new TagLocation<>(source.getName()
										 .getNamespace(), source.getName()
																.getPath());
		}
		@Override
		public TagLocation<T> getName(){
			return id;
		}
	}
	
	private static final class Delegate<T> extends CommonDelegate<T>{
		public Delegate(TagLocation<T> id, Supplier<TagCollection<T>> containerSupplier) {
			super( new TagDelegate<>(id, containerSupplier));
		}
		@Override
		public TagLocation<T> getName(){
			return (TagLocation<T>)delegate.getName();
		}
	}
}
