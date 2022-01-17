package ru.bclib.api;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.api.tag.TagRegistry;
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
import ru.bclib.BCLib;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

public class TagAPI {
	private static final Map<ResourceLocation, Set<ResourceLocation>> TAGS_BLOCK = Maps.newConcurrentMap();
	private static final Map<ResourceLocation, Set<ResourceLocation>> TAGS_ITEM = Maps.newConcurrentMap();
	
	// Vanilla Block Tags
	public static final TagLocation<Block> NAMED_SOUL_SPEED_BLOCKS = new TagLocation<>("soul_speed_blocks");
	public static final TagLocation<Block> NAMED_SOUL_FIRE_BASE_BLOCKS = new TagLocation<>("soul_fire_base_blocks");
	public static final TagLocation<Block> NAMED_CLIMBABLE = new TagLocation<>("climbable");
	public static final TagLocation<Block> NAMED_NYLIUM = new TagLocation<>("nylium");
	public static final TagLocation<Block> NAMED_ANVIL = new TagLocation<>("anvil");
	public static final TagLocation<Block> NAMED_WALLS = new TagLocation<>("walls");
	public static final TagLocation<Block> LEAVES = new TagLocation<>("leaves");
	public static final TagLocation<Block> NAMED_MINEABLE_AXE = new TagLocation<>("mineable/axe");
	public static final TagLocation<Block> NAMED_MINEABLE_PICKAXE = new TagLocation<>("mineable/pickaxe");
	public static final TagLocation<Block> NAMED_MINEABLE_SHOVEL = new TagLocation<>("mineable/shovel");
	public static final TagLocation<Block> NAMED_MINEABLE_HOE = new TagLocation<>("mineable/hoe");
	
	// Block Tags
	public static final TagNamed<Block> BLOCK_BOOKSHELVES = makeCommonBlockTag("bookshelves");
	public static final TagNamed<Block> BLOCK_GEN_TERRAIN = makeBlockTag(BCLib.MOD_ID, "gen_terrain");
	public static final TagNamed<Block> BLOCK_NETHER_GROUND = makeBlockTag(BCLib.MOD_ID, "nether_ground");
	public static final TagNamed<Block> BLOCK_END_GROUND = makeBlockTag(BCLib.MOD_ID, "end_ground");
	
	public static final TagNamed<Block> BLOCK_CHEST = makeCommonBlockTag("chest");
	public static final TagNamed<Block> BLOCK_WOODEN_CHEST = makeCommonBlockTag("wooden_chests");
	public static final TagNamed<Block> BLOCK_BARREL = makeCommonBlockTag("barrel");
	public static final TagNamed<Block> BLOCK_WOODEN_BARREL = makeCommonBlockTag("wooden_barrels");
	public static final TagNamed<Block> BLOCK_END_STONES = makeCommonBlockTag("end_stones");
	public static final TagNamed<Block> BLOCK_NETHER_STONES = makeCommonBlockTag("nether_stones");
	public static final TagNamed<Block> BLOCK_NETHER_PORTAL_FRAME = makeCommonBlockTag("nether_pframe");
	public static final TagNamed<Block> BLOCK_WORKBENCHES = makeCommonBlockTag("workbench");
	public static final TagNamed<Block> BLOCK_SAPLINGS = makeCommonBlockTag("saplings");
	public static final TagNamed<Block> BLOCK_LEAVES = makeCommonBlockTag("leaves");
	public static final TagNamed<Block> BLOCK_IMMOBILE = makeCommonBlockTag("immobile");
	public static final TagNamed<Block> BLOCK_SOUL_GROUND = makeCommonBlockTag("soul_ground");
	public static final TagNamed<Block> BLOCK_NETHERRACK = makeCommonBlockTag("netherrack");
	public static final TagNamed<Block> BLOCK_NETHER_MYCELIUM = makeCommonBlockTag("nether_mycelium");
	
	public static final TagNamed<Block> BLOCK_DRAGON_IMMUNE = getMCBlockTag("dragon_immune");
	
	public static final TagNamed<Block> MINEABLE_AXE = getMCBlockTag("mineable/axe");
	public static final TagNamed<Block> MINEABLE_PICKAXE = getMCBlockTag("mineable/pickaxe");
	public static final TagNamed<Block> MINEABLE_SHOVEL = getMCBlockTag("mineable/shovel");
	public static final TagNamed<Block> MINEABLE_HOE = getMCBlockTag("mineable/hoe");
	
	// Item Tags
	public static final TagNamed<Item> ITEM_CHEST = makeCommonItemTag("chest");
	public static final TagNamed<Item> ITEM_WOODEN_CHEST = makeCommonItemTag("wooden_chests");
	public static final TagNamed<Item> ITEM_BARREL = makeCommonItemTag("barrel");
	public static final TagNamed<Item> ITEM_WOODEN_BARREL = makeCommonItemTag("wooden_barrels");
	public static final TagNamed<Item> ITEM_IRON_INGOTS = makeCommonItemTag("iron_ingots");
	public static final TagNamed<Item> ITEM_FURNACES = makeCommonItemTag("furnaces");
	public static final TagNamed<Item> ITEM_WORKBENCHES = makeCommonItemTag("workbench");
	public final static TagNamed<Item> ITEM_HAMMERS = makeCommonItemTag("hammers");
	public static final TagNamed<Item> ITEM_SAPLINGS = makeCommonItemTag("saplings");
	public static final TagNamed<Item> ITEM_LEAVES = makeCommonItemTag("leaves");
	public static final TagNamed<Item> ITEM_SHEARS = getMCItemTag("shears");
	public static final TagNamed<Item> ITEM_COMMON_SHEARS = makeCommonItemTag("shears");
	public static final TagNamed<Item> ITEM_SOUL_GROUND = makeCommonItemTag("soul_ground");

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
	public static TagNamed<Block> getMCBlockTag(String name) {
		ResourceLocation id = new ResourceLocation(name);
		Tag<Block> tag = BlockTags.getAllTags().getTag(id);
		return CommonDelegate.proxy(tag == null ? (Named<Block>) TagFactory.BLOCK.create(id): (Named<Block>) tag);
	}
	
	/**
	 * Get or create Minecraft {@link Item} {@link Tag.Named}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Item} {@link Tag.Named}.
	 */
	public static TagNamed<Item> getMCItemTag(String name) {
		ResourceLocation id = new ResourceLocation(name);
		Tag<Item> tag = ItemTags.getAllTags().getTag(id);
		return  CommonDelegate.proxy(tag == null ? (Named<Item>) TagRegistry.item(id) : (Named<Item>) tag);
	}
	
	/**
	 * Initializes basic tags. Should be called only in BCLib main class.
	 */
	public static void init() {
		addBlockTag(BLOCK_BOOKSHELVES.getName(), Blocks.BOOKSHELF);
		addBlockTag(BLOCK_GEN_TERRAIN.getName(), Blocks.END_STONE, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
		addBlockTag(BLOCK_NETHER_GROUND.getName(), Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
		addBlockTag(BLOCK_END_GROUND.getName(), Blocks.END_STONE);
		addBlockTag(BLOCK_CHEST.getName(), Blocks.CHEST);
		addItemTag(ITEM_CHEST.getName(), Items.CHEST);
		addItemTag(ITEM_IRON_INGOTS.getName(), Items.IRON_INGOT);
		addItemTag(ITEM_FURNACES.getName(), Blocks.FURNACE);
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
	
	//    DEPRECATED SECTION    //
	// WILL BE REMOVED IN 1.3.0 //
	
	/**
	 * Deprecated due to low compatibility. Use addTag({@link ResourceLocation}, {@link ItemLike}... items) instead.
	 *
	 * Adds one Tag to multiple Items.
	 * <p>
	 * Example:
	 * <pre>{@code  Tag.Named<Item> METALS = makeBlockTag("mymod", "metals");
	 * addTag(METALS, Items.IRON_INGOT, Items.GOLD_INGOT, Items.COPPER_INGOT);}</pre>
	 * <p>
	 * The call will reserve the Tag. The Tag is added to the items once
	 * {@link #apply(String, Map)} was executed.
	 *
	 * @param tag   The new Tag
	 * @param items One or more item that should receive the Tag.
	 */
	@Deprecated
	public static void addTag(Tag.Named<Item> tag, ItemLike... items) {
		addItemTagUntyped(tag.getName(), items);
	}
	
	/**
	 * Deprecated, use addTag({@link ResourceLocation}, {@link ItemLike}... items) instead.
	 *
	 * Adds multiple Tags to one Item.
	 * <p>
	 * The call will reserve the Tags. The Tags are added to the Item once
	 * * {@link #apply(String, Map)} was executed.
	 *
	 * @param item The Item that will receive all Tags
	 * @param tags One or more Tags
	 */
	@Deprecated
	@SafeVarargs
	public static void addTags(ItemLike item, Tag.Named<Item>... tags) {
		for (Tag.Named<Item> tag : tags) {
			addItemTagUntyped(tag.getName(), item);
		}
	}
	
	/**
	 * Deprecated, use addTag({@link ResourceLocation}, {@link Block}... blocks) instead.
	 *
	 * Adds multiple Tags to one Block.
	 * <p>
	 * The call will reserve the Tags. The Tags are added to the Block once
	 * * {@link #apply(String, Map)} was executed.
	 *
	 * @param block The Block that will receive all Tags
	 * @param tags  One or more Tags
	 */
	@Deprecated
	@SafeVarargs
	public static void addTags(Block block, Tag.Named<Block>... tags) {
		for (Tag.Named<Block> tag : tags) {
			addTag(tag, block);
		}
	}
	
	/**
	 * Deprecated due to low compatibility. Use addTag({@link ResourceLocation}, {@link Block}... blocks) instead.
	 *
	 * Adds one Tag to multiple Blocks.
	 * <p>
	 * Example:
	 * <pre>{@code  Tag.Named<Block> DIMENSION_STONE = makeBlockTag("mymod", "dim_stone");
	 * addTag(DIMENSION_STONE, Blocks.END_STONE, Blocks.NETHERRACK);}</pre>
	 * <p>
	 * The call will reserve the Tag. The Tag is added to the blocks once
	 * {@link #apply(String, Map)} was executed.
	 *
	 * @param tag	The new Tag
	 * @param blocks One or more blocks that should receive the Tag.
	 */
	@Deprecated
	public static void addTag(Tag.Named<Block> tag, Block... blocks) {
		addBlockTagUntyped(tag.getName(), blocks);
	}
	
	/**
	 * Adds {@link Block} to NETHER_GROUND and GEN_TERRAIN tags to process it properly in terrain generators and block logic.
	 *
	 * @param block - {@link Block}.
	 */
	@Deprecated
	public static void addNetherGround(Block block) {
		addTag(BLOCK_NETHER_GROUND, block);
		addTag(BLOCK_GEN_TERRAIN, block);
	}
	
	/**
	 * Adds {@link Block} to END_GROUND and GEN_TERRAIN tags to process it properly in terrain generators and block logic.
	 *
	 * @param block - {@link Block}.
	 */
	@Deprecated
	public static void addEndGround(Block block) {
		addTag(BLOCK_GEN_TERRAIN, block);
		addTag(BLOCK_END_GROUND, block);
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
