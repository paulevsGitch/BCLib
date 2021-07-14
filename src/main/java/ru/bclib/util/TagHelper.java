package ru.bclib.util;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * Utility functions to manage Minecraft Tags
 */
public class TagHelper {
	private static final Map<ResourceLocation, Set<ResourceLocation>> TAGS_BLOCK = Maps.newConcurrentMap();
	private static final Map<ResourceLocation, Set<ResourceLocation>> TAGS_ITEM = Maps.newConcurrentMap();

	/**
	 * Adds one Tag to multiple Blocks.
	 *
	 * Example:
	 * <pre>
	 * {@code
	 * Tag.Named<Block> DIMENSION_STONE = makeBlockTag("mymod", "dim_stone");
	 * TagHelper.addTag(DIMENSION_STONE, Blocks.END_STONE, Blocks.NETHERRACK);
	 * }
	 * </pre>
	 *
	 * The call will reserve the Tag. The Tag is added to the blocks once
	 * {@link #apply(String, Map)} was executed.
	 *
	 * @param tag The new Tag
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
	 *
	 * Example:
	 * <pre>
	 * {@code
	 * Tag.Named<Item> METALS = makeBlockTag("mymod", "metals");
	 * TagHelper.addTag(METALS, Items.IRON_INGOT, Items.GOLD_INGOT, Items.COPPER_INGOT);
	 * }
	 * </pre>
	 *
	 * The call will reserve the Tag. The Tag is added to the items once
	 * {@link #apply(String, Map)} was executed.
	 *
	 * @param tag The new Tag
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
	 *
	 * The call will reserve the Tags. The Tags are added to the Item once
	 * 	 * {@link #apply(String, Map)} was executed.
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
	 *
	 * The call will reserve the Tags. The Tags are added to the Block once
	 * 	 * {@link #apply(String, Map)} was executed.
	 *
	 * @param block The Block that will receive all Tags
	 * @param tags One or more Tags
	 */
	@SafeVarargs
	public static void addTags(Block block, Tag.Named<Block>... tags) {
		for (Tag.Named<Block> tag : tags) {
			addTag(tag, block);
		}
	}

	/**
	 * Adds all {@code ids} to the {@code builder}.
	 * @param builder
	 * @param ids
	 *
	 * @return The Builder passed as {@code builder}.
	 */
	public static Tag.Builder apply(Tag.Builder builder, Set<ResourceLocation> ids) {
		ids.forEach(value -> builder.addElement(value, "Better End Code"));
		return builder;
	}

	/**
	 * Automatically called in {@link net.minecraft.tags.TagLoader#loadAndBuild(ResourceManager)}.
	 *
	 * In most cases there is no need to call this Method manually.
	 *
	 * @param directory The name of the Tag-directory. Should be either <i>"tags/blocks"</i> or
	 *                     <i>"tags/items"</i>.
	 * @param tagsMap The map that will hold the registered Tags
	 *
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
