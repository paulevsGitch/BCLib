package ru.bclib.api;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.fabricmc.fabric.api.tag.TagRegistry;
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

public class TagAPI {
	private static final Map<ResourceLocation, Set<ResourceLocation>> TAGS_BLOCK = Maps.newConcurrentMap();
	private static final Map<ResourceLocation, Set<ResourceLocation>> TAGS_ITEM = Maps.newConcurrentMap();
	
	// Block Tags
	public static final Tag.Named<Block> BLOCK_BOOKSHELVES = makeCommonBlockTag("bookshelves");
	public static final Tag.Named<Block> BLOCK_GEN_TERRAIN = makeBlockTag(BCLib.MOD_ID, "gen_terrain");
	public static final Tag.Named<Block> BLOCK_NETHER_GROUND = makeBlockTag(BCLib.MOD_ID, "nether_ground");
	public static final Tag.Named<Block> BLOCK_END_GROUND = makeBlockTag(BCLib.MOD_ID, "end_ground");
	
	public static final Tag.Named<Block> BLOCK_CHEST = makeCommonBlockTag("chest");
	public static final Tag.Named<Block> BLOCK_WOODEN_CHEST = makeCommonBlockTag("wooden_chests");
	public static final Tag.Named<Block> BLOCK_BARREL = makeCommonBlockTag("barrel");
	public static final Tag.Named<Block> BLOCK_WOODEN_BARREL = makeCommonBlockTag("wooden_barrels");
	public static final Tag.Named<Block> BLOCK_END_STONES = makeCommonBlockTag("end_stones");
	public static final Tag.Named<Block> BLOCK_NETHER_STONES = makeCommonBlockTag("nether_stones");
	public static final Tag.Named<Block> BLOCK_NETHER_PORTAL_FRAME = makeCommonBlockTag("nether_pframe");
	public static final Tag.Named<Block> BLOCK_WORKBENCHES = makeCommonBlockTag("workbench");
	public static final Tag.Named<Block> BLOCK_SAPLINGS = makeCommonBlockTag("saplings");
	public static final Tag.Named<Block> BLOCK_LEAVES = makeCommonBlockTag("leaves");
	
	public static final Tag.Named<Block> BLOCK_DRAGON_IMMUNE = getMCBlockTag("dragon_immune");
	
	public static final Tag.Named<Block> MINEABLE_AXE = getMCBlockTag("mineable/axe");
	public static final Tag.Named<Block> MINEABLE_PICKAXE = getMCBlockTag("mineable/pickaxe");
	public static final Tag.Named<Block> MINEABLE_SHOVEL = getMCBlockTag("mineable/shovel");
	public static final Tag.Named<Block> MINEABLE_HOE = getMCBlockTag("mineable/hoe");
	
	// Item Tags
	public static final Tag.Named<Item> ITEM_CHEST = makeCommonItemTag("chest");
	public static final Tag.Named<Item> ITEM_WOODEN_CHEST = makeCommonItemTag("wooden_chests");
	public static final Tag.Named<Item> ITEM_BARREL = makeCommonItemTag("barrel");
	public static final Tag.Named<Item> ITEM_WOODEN_BARREL = makeCommonItemTag("wooden_barrels");
	public static final Tag.Named<Item> ITEM_IRON_INGOTS = makeCommonItemTag("iron_ingots");
	public static final Tag.Named<Item> ITEM_FURNACES = makeCommonItemTag("furnaces");
	public static final Tag.Named<Item> ITEM_WORKBENCHES = makeCommonItemTag("workbench");
	public final static Tag.Named<Item> ITEM_HAMMERS = makeCommonItemTag("hammers");
	public static final Tag.Named<Item> ITEM_SAPLINGS = makeCommonItemTag("saplings");
	public static final Tag.Named<Item> ITEM_LEAVES = makeCommonItemTag("leaves");
	public static final Tag.Named<Item> ITEM_SHEARS = getMCItemTag("shears");
	public static final Tag.Named<Item> ITEM_COMMON_SHEARS = makeCommonItemTag("shears");


	/**
	 * Get or create {@link Tag.Named}.
	 *
	 * @param containerSupplier - {@link TagCollection} {@link Supplier} tag collection;
	 * @param id                - {@link ResourceLocation} tag id.
	 * @return {@link Tag.Named}.
	 */
	public static <T> Tag.Named<T> makeTag(Supplier<TagCollection<T>> containerSupplier, ResourceLocation id) {
		Tag<T> tag = containerSupplier.get().getTag(id);
		return tag == null ? TagRegistry.create(id, containerSupplier) : (Named<T>) tag;
	}
	
	/**
	 * Get or create {@link Block} {@link Tag.Named} with mod namespace.
	 *
	 * @param modID - {@link String} mod namespace (mod id);
	 * @param name  - {@link String} tag name.
	 * @return {@link Block} {@link Tag.Named}.
	 */
	public static Tag.Named<Block> makeBlockTag(String modID, String name) {
		return makeTag(BlockTags::getAllTags, new ResourceLocation(modID, name));
	}
	
	/**
	 * Get or create {@link Item} {@link Tag.Named} with mod namespace.
	 *
	 * @param modID - {@link String} mod namespace (mod id);
	 * @param name  - {@link String} tag name.
	 * @return {@link Item} {@link Tag.Named}.
	 */
	public static Tag.Named<Item> makeItemTag(String modID, String name) {
		return makeTag(ItemTags::getAllTags, new ResourceLocation(modID, name));
	}
	
	/**
	 * Get or create {@link Block} {@link Tag.Named}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Block} {@link Tag.Named}.
	 * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
	 */
	public static Tag.Named<Block> makeCommonBlockTag(String name) {
		return makeTag(BlockTags::getAllTags, new ResourceLocation("c", name));
	}
	
	/**
	 * Get or create {@link Item} {@link Tag.Named}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Item} {@link Tag.Named}.
	 * @see <a href="https://fabricmc.net/wiki/tutorial:tags">Fabric Wiki (Tags)</a>
	 */
	public static Tag.Named<Item> makeCommonItemTag(String name) {
		return makeTag(ItemTags::getAllTags, new ResourceLocation("c", name));
	}
	
	/**
	 * Get or create Minecraft {@link Block} {@link Tag.Named}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Block} {@link Tag.Named}.
	 */
	public static Tag.Named<Block> getMCBlockTag(String name) {
		ResourceLocation id = new ResourceLocation(name);
		Tag<Block> tag = BlockTags.getAllTags().getTag(id);
		return tag == null ? (Named<Block>) TagRegistry.block(id) : (Named<Block>) tag;
	}
	
	/**
	 * Get or create Minecraft {@link Item} {@link Tag.Named}.
	 *
	 * @param name - {@link String} tag name.
	 * @return {@link Item} {@link Tag.Named}.
	 */
	public static Tag.Named<Item> getMCItemTag(String name) {
		ResourceLocation id = new ResourceLocation(name);
		Tag<Item> tag = ItemTags.getAllTags().getTag(id);
		return tag == null ? (Named<Item>) TagRegistry.item(id) : (Named<Item>) tag;
	}
	
	/**
	 * Adds {@link Block} to NETHER_GROUND and GEN_TERRAIN tags to process it properly in terrain generators and block logic.
	 *
	 * @param block - {@link Block}.
	 */
	public static void addNetherGround(Block block) {
		addTag(BLOCK_NETHER_GROUND, block);
		addTag(BLOCK_GEN_TERRAIN, block);
	}
	
	/**
	 * Adds {@link Block} to END_GROUND and GEN_TERRAIN tags to process it properly in terrain generators and block logic.
	 *
	 * @param block - {@link Block}.
	 */
	public static void addEndGround(Block block) {
		addTag(BLOCK_GEN_TERRAIN, block);
		addTag(BLOCK_END_GROUND, block);
	}
	
	/**
	 * Initializes basic tags. Should be called only in BCLib main class.
	 */
	public static void init() {
		addTag(BLOCK_BOOKSHELVES, Blocks.BOOKSHELF);
		addTag(BLOCK_GEN_TERRAIN, Blocks.END_STONE, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
		addTag(BLOCK_NETHER_GROUND, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
		addTag(BLOCK_END_GROUND, Blocks.END_STONE);
		addTag(BLOCK_CHEST, Blocks.CHEST);
		addTag(ITEM_CHEST, Items.CHEST);
		addTag(ITEM_IRON_INGOTS, Items.IRON_INGOT);
		addTag(ITEM_FURNACES, Blocks.FURNACE);
	}
	
	/**
	 * Adds one Tag to multiple Blocks.
	 * <p>
	 * Example:
	 * <pre>{@code  Tag.Named<Block> DIMENSION_STONE = makeBlockTag("mymod", "dim_stone");
	 * addTag(DIMENSION_STONE, Blocks.END_STONE, Blocks.NETHERRACK);}</pre>
	 * <p>
	 * The call will reserve the Tag. The Tag is added to the blocks once
	 * {@link #apply(String, Map)} was executed.
	 *
	 * @param tag    The new Tag
	 * @param blocks One or more blocks that should receive the Tag.
	 */
	public static void addTag(Tag.Named<Block> tag, Block... blocks) {
		ResourceLocation tagID = tag.getName();
		Set<ResourceLocation> set = TAGS_BLOCK.computeIfAbsent(tagID, k -> Sets.newHashSet());
		for (Block block : blocks) {
			ResourceLocation id = Registry.BLOCK.getKey(block);
			if (id != Registry.BLOCK.getDefaultKey()) {
				set.add(id);
			}
		}
	}
	
	/**
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
	public static void addTag(Tag.Named<Item> tag, ItemLike... items) {
		ResourceLocation tagID = tag.getName();
		Set<ResourceLocation> set = TAGS_ITEM.computeIfAbsent(tagID, k -> Sets.newHashSet());
		for (ItemLike item : items) {
			ResourceLocation id = Registry.ITEM.getKey(item.asItem());
			if (id != Registry.ITEM.getDefaultKey()) {
				set.add(id);
			}
		}
	}
	
	/**
	 * Adds multiple Tags to one Item.
	 * <p>
	 * The call will reserve the Tags. The Tags are added to the Item once
	 * * {@link #apply(String, Map)} was executed.
	 *
	 * @param item The Item that will receive all Tags
	 * @param tags One or more Tags
	 */
	@SafeVarargs
	public static void addTags(ItemLike item, Tag.Named<Item>... tags) {
		for (Tag.Named<Item> tag : tags) {
			addTag(tag, item);
		}
	}
	
	/**
	 * Adds multiple Tags to one Block.
	 * <p>
	 * The call will reserve the Tags. The Tags are added to the Block once
	 * * {@link #apply(String, Map)} was executed.
	 *
	 * @param block The Block that will receive all Tags
	 * @param tags  One or more Tags
	 */
	@SafeVarargs
	public static void addTags(Block block, Tag.Named<Block>... tags) {
		for (Tag.Named<Block> tag : tags) {
			addTag(tag, block);
		}
	}
	
	/**
	 * Adds all {@code ids} to the {@code builder}.
	 *
	 * @param builder
	 * @param ids
	 * @return The Builder passed as {@code builder}.
	 */
	public static Tag.Builder apply(Tag.Builder builder, Set<ResourceLocation> ids) {
		ids.forEach(value -> builder.addElement(value, "Better End Code"));
		return builder;
	}
	
	/**
	 * Automatically called in {@link net.minecraft.tags.TagLoader#loadAndBuild(ResourceManager)}.
	 * <p>
	 * In most cases there is no need to call this Method manually.
	 *
	 * @param directory The name of the Tag-directory. Should be either <i>"tags/blocks"</i> or
	 *                  <i>"tags/items"</i>.
	 * @param tagsMap   The map that will hold the registered Tags
	 * @return The {@code tagsMap} Parameter.
	 */
	public static Map<ResourceLocation, Tag.Builder> apply(String directory, Map<ResourceLocation, Tag.Builder> tagsMap) {
		Map<ResourceLocation, Set<ResourceLocation>> endTags = null;
		if ("tags/blocks".equals(directory)) {
			endTags = TAGS_BLOCK;
		}
		else if ("tags/items".equals(directory)) {
			endTags = TAGS_ITEM;
		}
		if (endTags != null) {
			endTags.forEach((id, ids) -> apply(tagsMap.computeIfAbsent(id, key -> Tag.Builder.tag()), ids));
		}
		return tagsMap;
	}
}
