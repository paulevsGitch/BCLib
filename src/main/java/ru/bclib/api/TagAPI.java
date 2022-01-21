package ru.bclib.api;

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
	
	// LOCATIONS //
	
	// Mineable Block Tags
	public static final TagLocation<Block> NAMED_MINEABLE_AXE = new TagLocation<>("mineable/axe");
	public static final TagLocation<Block> NAMED_MINEABLE_HOE = new TagLocation<>("mineable/hoe");
	public static final TagLocation<Block> NAMED_MINEABLE_PICKAXE = new TagLocation<>("mineable/pickaxe");
	public static final TagLocation<Block> NAMED_MINEABLE_SHEARS = new TagLocation<>("fabric", "mineable/shears");
	public static final TagLocation<Block> NAMED_MINEABLE_SHOVEL = new TagLocation<>("mineable/shovel");
	public static final TagLocation<Block> NAMED_MINEABLE_SWORD = new TagLocation<>("fabric", "mineable/sword");
	
	// Fabric Tools
	public static final TagLocation<Item> NAMED_FABRIC_AXES = new TagLocation<>("fabric", "axes");
	public static final TagLocation<Item> NAMED_FABRIC_HOES = new TagLocation<>("fabric", "hoes");
	public static final TagLocation<Item> NAMED_FABRIC_PICKAXES = new TagLocation<>("fabric", "pickaxes");
	public static final TagLocation<Item> NAMED_FABRIC_SHEARS = new TagLocation<>("fabric", "shears");
	public static final TagLocation<Item> NAMED_FABRIC_SHOVELS = new TagLocation<>("fabric", "shovels");
	public static final TagLocation<Item> NAMED_FABRIC_SWORDS = new TagLocation<>("fabric", "swords");
	
	// Vanilla Block Tags
	public static final TagLocation<Block> NAMED_BLOCK_ANVIL = new TagLocation<>("anvil");
	public static final TagLocation<Block> NAMED_BLOCK_BUTTONS = new TagLocation<>("buttons");
	public static final TagLocation<Block> NAMED_BLOCK_CLIMBABLE = new TagLocation<>("climbable");
	public static final TagLocation<Block> NAMED_BLOCK_DOORS = new TagLocation<>("doors");
	public static final TagLocation<Block> NAMED_BLOCK_FENCES = new TagLocation<>("fences");
	public static final TagLocation<Block> NAMED_BLOCK_FENCE_GATES = new TagLocation<>("fence_gates");
	public static final TagLocation<Block> NAMED_BLOCK_LEAVES = new TagLocation<>("leaves");
	public static final TagLocation<Block> NAMED_BLOCK_LOGS = new TagLocation<>("logs");
	public static final TagLocation<Block> NAMED_BLOCK_LOGS_THAT_BURN = new TagLocation<>("logs_that_burn");
	public static final TagLocation<Block> NAMED_BLOCK_NYLIUM = new TagLocation<>("nylium");
	public static final TagLocation<Block> NAMED_BLOCK_PLANKS = new TagLocation<>("planks");
	public static final TagLocation<Block> NAMED_BLOCK_PRESSURE_PLATES = new TagLocation<>("pressure_plates");
	public static final TagLocation<Block> NAMED_BLOCK_SAPLINGS = new TagLocation<>("saplings");
	public static final TagLocation<Block> NAMED_BLOCK_SIGNS = new TagLocation<>("signs");
	public static final TagLocation<Block> NAMED_BLOCK_SLABS = new TagLocation<>("slabs");
	public static final TagLocation<Block> NAMED_BLOCK_STAIRS = new TagLocation<>("stairs");
	public static final TagLocation<Block> NAMED_BLOCK_STONE_PRESSURE_PLATES = new TagLocation<>("stone_pressure_plates");
	public static final TagLocation<Block> NAMED_BLOCK_TRAPDOORS = new TagLocation<>("trapdoors");
	public static final TagLocation<Block> NAMED_BLOCK_WALLS = new TagLocation<>("walls");
	public static final TagLocation<Block> NAMED_BLOCK_WOODEN_BUTTONS = new TagLocation<>("wooden_buttons");
	public static final TagLocation<Block> NAMED_BLOCK_WOODEN_DOORS = new TagLocation<>("wooden_doors");
	public static final TagLocation<Block> NAMED_BLOCK_WOODEN_FENCES = new TagLocation<>("wooden_fences");
	public static final TagLocation<Block> NAMED_BLOCK_WOODEN_PRESSURE_PLATES = new TagLocation<>("wooden_pressure_plates");
	public static final TagLocation<Block> NAMED_BLOCK_WOODEN_SLABS = new TagLocation<>("wooden_slabs");
	public static final TagLocation<Block> NAMED_BLOCK_WOODEN_STAIRS = new TagLocation<>("wooden_stairs");
	public static final TagLocation<Block> NAMED_BLOCK_WOODEN_TRAPDOORS = new TagLocation<>("wooden_trapdoors");
	public static final TagLocation<Block> NAMED_SOUL_FIRE_BASE_BLOCKS = new TagLocation<>("soul_fire_base_blocks");
	public static final TagLocation<Block> NAMED_SOUL_SPEED_BLOCKS = new TagLocation<>("soul_speed_blocks");
	
	// Vanilla Item Tags
	public static final TagLocation<Item> NAMED_ITEM_BUTTONS = new TagLocation<>("buttons");
	public static final TagLocation<Item> NAMED_ITEM_DOORS = new TagLocation<>("doors");
	public static final TagLocation<Item> NAMED_ITEM_FENCES = new TagLocation<>("fences");
	public static final TagLocation<Item> NAMED_ITEM_FENCE_GATES = new TagLocation<>("fence_gates");
	public static final TagLocation<Item> NAMED_ITEM_LEAVES = new TagLocation<>("leaves");
	public static final TagLocation<Item> NAMED_ITEM_LOGS = new TagLocation<>("logs");
	public static final TagLocation<Item> NAMED_ITEM_LOGS_THAT_BURN = new TagLocation<>("logs_that_burn");
	public static final TagLocation<Item> NAMED_ITEM_PLANKS = new TagLocation<>("planks");
	public static final TagLocation<Item> NAMED_ITEM_PRESSURE_PLATES = new TagLocation<>("pressure_plates");
	public static final TagLocation<Item> NAMED_ITEM_SAPLINGS = new TagLocation<>("saplings");
	public static final TagLocation<Item> NAMED_ITEM_SHEARS = new TagLocation<>("shears");
	public static final TagLocation<Item> NAMED_ITEM_SIGNS = new TagLocation<>("signs");
	public static final TagLocation<Item> NAMED_ITEM_SLABS = new TagLocation<>("slabs");
	public static final TagLocation<Item> NAMED_ITEM_STAIRS = new TagLocation<>("stairs");
	public static final TagLocation<Item> NAMED_ITEM_STONE_PRESSURE_PLATES = new TagLocation<>("stone_pressure_plates");
	public static final TagLocation<Item> NAMED_ITEM_TRAPDOORS = new TagLocation<>("trapdoors");
	public static final TagLocation<Item> NAMED_ITEM_WOODEN_BUTTONS = new TagLocation<>("wooden_buttons");
	public static final TagLocation<Item> NAMED_ITEM_WOODEN_DOORS = new TagLocation<>("wooden_doors");
	public static final TagLocation<Item> NAMED_ITEM_WOODEN_FENCES = new TagLocation<>("wooden_fences");
	public static final TagLocation<Item> NAMED_ITEM_WOODEN_PRESSURE_PLATES = new TagLocation<>("wooden_pressure_plates");
	public static final TagLocation<Item> NAMED_ITEM_WOODEN_SLABS = new TagLocation<>("wooden_slabs");
	public static final TagLocation<Item> NAMED_ITEM_WOODEN_STAIRS = new TagLocation<>("wooden_stairs");
	public static final TagLocation<Item> NAMED_ITEM_WOODEN_TRAPDOORS = new TagLocation<>("wooden_trapdoors");
	
	// Common Block Tags
	public static final TagLocation<Block> NAMED_BLOCK_DRAGON_IMMUNE = new TagLocation<>("c", "dragon_immune");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_BARREL = new TagLocation<>("c", "barrel");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_BOOKSHELVES = new TagLocation<>("c", "bookshelves");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_CHEST = new TagLocation<>("c", "chest");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_END_STONES = new TagLocation<>("c", "end_stones");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_IMMOBILE = new TagLocation<>("c", "immobile");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_LEAVES = new TagLocation<>("c", "leaves");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_NETHERRACK = new TagLocation<>("c", "netherrack");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_NETHER_MYCELIUM = new TagLocation<>("c", "nether_mycelium");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_NETHER_PORTAL_FRAME = new TagLocation<>("c", "nether_pframe");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_NETHER_STONES = new TagLocation<>("c", "nether_stones");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_SAPLINGS = new TagLocation<>("c", "saplings");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_SOUL_GROUND = new TagLocation<>("c", "soul_ground");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_WOODEN_BARREL = new TagLocation<>("c", "wooden_barrels");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_WOODEN_CHEST = new TagLocation<>("c", "wooden_chests");
	public static final TagLocation<Block> NAMED_COMMON_BLOCK_WORKBENCHES = new TagLocation<>("c", "workbench");
	
	// Common Item Tags
	public static final TagLocation<Item> NAMED_COMMON_ITEM_BARREL = new TagLocation<>("c", "barrel");
	public static final TagLocation<Item> NAMED_COMMON_ITEM_CHEST = new TagLocation<>("c", "chest");
	public static final TagLocation<Item> NAMED_COMMON_ITEM_FURNACES = new TagLocation<>("c", "furnaces");
	public static final TagLocation<Item> NAMED_COMMON_ITEM_HAMMERS = new TagLocation<>("c", "hammers");
	public static final TagLocation<Item> NAMED_COMMON_ITEM_IRON_INGOTS = new TagLocation<>("c", "iron_ingots");
	public static final TagLocation<Item> NAMED_COMMON_ITEM_LEAVES = new TagLocation<>("c", "leaves");
	public static final TagLocation<Item> NAMED_COMMON_ITEM_SAPLINGS = new TagLocation<>("c", "saplings");
	public static final TagLocation<Item> NAMED_COMMON_ITEM_SHEARS = new TagLocation<>("c", "shears");
	public static final TagLocation<Item> NAMED_COMMON_ITEM_SOUL_GROUND = new TagLocation<>("c", "soul_ground");
	public static final TagLocation<Item> NAMED_COMMON_ITEM_WOODEN_BARREL = new TagLocation<>("c", "wooden_barrels");
	public static final TagLocation<Item> NAMED_COMMON_ITEM_WOODEN_CHEST = new TagLocation<>("c", "wooden_chests");
	public static final TagLocation<Item> NAMED_COMMON_ITEM_WORKBENCHES = new TagLocation<>("c", "workbench");
	
	// TAGS //
	
	// Common Block Tags
	public static final TagNamed<Block> COMMON_BLOCK_BARREL = makeCommonBlockTag("barrel");
	public static final TagNamed<Block> COMMON_BLOCK_BOOKSHELVES = makeCommonBlockTag("bookshelves");
	public static final TagNamed<Block> COMMON_BLOCK_CHEST = makeCommonBlockTag("chest");
	public static final TagNamed<Block> COMMON_BLOCK_END_STONES = makeCommonBlockTag("end_stones");
	public static final TagNamed<Block> COMMON_BLOCK_IMMOBILE = makeCommonBlockTag("immobile");
	public static final TagNamed<Block> COMMON_BLOCK_LEAVES = makeCommonBlockTag("leaves");
	public static final TagNamed<Block> COMMON_BLOCK_NETHERRACK = makeCommonBlockTag("netherrack");
	public static final TagNamed<Block> COMMON_BLOCK_NETHER_MYCELIUM = makeCommonBlockTag("nether_mycelium");
	public static final TagNamed<Block> COMMON_BLOCK_NETHER_PORTAL_FRAME = makeCommonBlockTag("nether_pframe");
	public static final TagNamed<Block> COMMON_BLOCK_NETHER_STONES = makeCommonBlockTag("nether_stones");
	public static final TagNamed<Block> COMMON_BLOCK_SAPLINGS = makeCommonBlockTag("saplings");
	public static final TagNamed<Block> COMMON_BLOCK_SOUL_GROUND = makeCommonBlockTag("soul_ground");
	public static final TagNamed<Block> COMMON_BLOCK_WOODEN_BARREL = makeCommonBlockTag("wooden_barrels");
	public static final TagNamed<Block> COMMON_BLOCK_WOODEN_CHEST = makeCommonBlockTag("wooden_chests");
	public static final TagNamed<Block> COMMON_BLOCK_WORKBENCHES = makeCommonBlockTag("workbench");
	
	// Common Item Tags
	public final static TagNamed<Item> COMMON_ITEM_HAMMERS = makeCommonItemTag("hammers");
	public static final TagNamed<Item> COMMON_ITEM_BARREL = makeCommonItemTag("barrel");
	public static final TagNamed<Item> COMMON_ITEM_CHEST = makeCommonItemTag("chest");
	public static final TagNamed<Item> COMMON_ITEM_SHEARS = makeCommonItemTag("shears");
	public static final TagNamed<Item> COMMON_ITEM_FURNACES = makeCommonItemTag("furnaces");
	public static final TagNamed<Item> COMMON_ITEM_IRON_INGOTS = makeCommonItemTag("iron_ingots");
	public static final TagNamed<Item> COMMON_ITEM_LEAVES = makeCommonItemTag("leaves");
	public static final TagNamed<Item> COMMON_ITEM_SAPLINGS = makeCommonItemTag("saplings");
	public static final TagNamed<Item> COMMON_ITEM_SOUL_GROUND = makeCommonItemTag("soul_ground");
	public static final TagNamed<Item> COMMON_ITEM_WOODEN_BARREL = makeCommonItemTag("wooden_barrels");
	public static final TagNamed<Item> COMMON_ITEM_WOODEN_CHEST = makeCommonItemTag("wooden_chests");
	public static final TagNamed<Item> COMMON_ITEM_WORKBENCHES = makeCommonItemTag("workbench");

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
		addBlockTag(COMMON_BLOCK_BOOKSHELVES.getName(), Blocks.BOOKSHELF);
		addBlockTag(COMMON_BLOCK_CHEST.getName(), Blocks.CHEST);
		addItemTag(COMMON_ITEM_CHEST.getName(), Items.CHEST);
		addItemTag(COMMON_ITEM_IRON_INGOTS.getName(), Items.IRON_INGOT);
		addItemTag(COMMON_ITEM_FURNACES.getName(), Blocks.FURNACE);
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
