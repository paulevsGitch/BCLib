package ru.bclib.api;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Named;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import ru.bclib.BCLib;
import ru.bclib.util.TagHelper;

public class TagAPI {
	// Block Tags
	public static final Tag.Named<Block> BOOKSHELVES = makeCommonBlockTag("bookshelves");
	public static final Tag.Named<Block> GEN_TERRAIN = makeBlockTag(BCLib.MOD_ID, "gen_terrain");
	public static final Tag.Named<Block> NETHER_GROUND = makeBlockTag(BCLib.MOD_ID, "nether_ground");
	public static final Tag.Named<Block> END_GROUND = makeBlockTag(BCLib.MOD_ID, "end_ground");
	
	public static final Tag.Named<Block> BLOCK_CHEST = makeCommonBlockTag("chest");
	public static final Tag.Named<Block> END_STONES = makeCommonBlockTag("end_stones");
	public static final Tag.Named<Block> NETHER_STONES = makeCommonBlockTag("nether_stones");
	
	public static final Tag.Named<Block> DRAGON_IMMUNE = getMCBlockTag("dragon_immune");
	
	public static final Tag.Named<Block> MINEABLE_AXE = getMCBlockTag("mineable/axe");
	public static final Tag.Named<Block> MINEABLE_PICKAXE = getMCBlockTag("mineable/pickaxe");
	public static final Tag.Named<Block> MINEABLE_SHOVEL = getMCBlockTag("mineable/shovel");
	public static final Tag.Named<Block> MINEABLE_HOE = getMCBlockTag("mineable/hoe");
	
	// Item Tags
	public static final Tag.Named<Item> ITEM_CHEST = makeCommonItemTag("chest");
	public static final Tag.Named<Item> IRON_INGOTS = makeCommonItemTag("iron_ingots");
	public static final Tag.Named<Item> FURNACES = makeCommonItemTag("furnaces");
	public final static Tag.Named<Item> HAMMERS = makeItemTag("fabric", "hammers");
	
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
	 * Adds {@link Block} to NETHER_GROUND and GEN_TERRAIN tags to process it properly in terrain generators and block logic.
	 *
	 * @param block - {@link Block}.
	 */
	public static void addNetherGround(Block block) {
		TagHelper.addTag(NETHER_GROUND, block);
		TagHelper.addTag(GEN_TERRAIN, block);
	}
	
	/**
	 * Adds {@link Block} to END_GROUND and GEN_TERRAIN tags to process it properly in terrain generators and block logic.
	 *
	 * @param block - {@link Block}.
	 */
	public static void addEndGround(Block block) {
		TagHelper.addTag(GEN_TERRAIN, block);
		TagHelper.addTag(END_GROUND, block);
	}
	
	/**
	 * Initializes basic tags. Should be called only in BCLib main class.
	 */
	public static void init() {
		TagHelper.addTag(BOOKSHELVES, Blocks.BOOKSHELF);
		TagHelper.addTag(GEN_TERRAIN, Blocks.END_STONE, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
		TagHelper.addTag(NETHER_GROUND, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
		TagHelper.addTag(END_GROUND, Blocks.END_STONE);
		TagHelper.addTag(BLOCK_CHEST, Blocks.CHEST);
		TagHelper.addTag(ITEM_CHEST, Items.CHEST);
		TagHelper.addTag(IRON_INGOTS, Items.IRON_INGOT);
		TagHelper.addTag(FURNACES, Blocks.FURNACE);
	}
}
