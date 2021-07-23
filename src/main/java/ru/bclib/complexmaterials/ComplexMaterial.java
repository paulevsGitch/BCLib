package ru.bclib.complexmaterials;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import ru.bclib.complexmaterials.entry.BlockEntry;
import ru.bclib.complexmaterials.entry.ItemEntry;
import ru.bclib.config.PathConfig;
import ru.bclib.registry.BlockRegistry;
import ru.bclib.registry.ItemRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class ComplexMaterial {
	private static final Map<ResourceLocation, List<BlockEntry>> BLOCK_ENTRIES = Maps.newHashMap();
	private static final Map<ResourceLocation, List<ItemEntry>> ITEM_ENTRIES = Maps.newHashMap();
	private static final List<ComplexMaterial> MATERIALS = Lists.newArrayList();
	
	private final List<BlockEntry> defaultBlockEntries = Lists.newArrayList();
	private final List<ItemEntry> defaultItemEntries = Lists.newArrayList();
	private final Map<String, Tag.Named<Block>> blockTags = Maps.newHashMap();
	private final Map<String, Tag.Named<Item>> itemTags = Maps.newHashMap();
	private final Map<String, Block> blocks = Maps.newHashMap();
	private final Map<String, Item> items = Maps.newHashMap();
	
	private final String baseName;
	private final String modID;
	
	public ComplexMaterial(String modID, String baseName) {
		this.baseName = baseName;
		this.modID = modID;
		MATERIALS.add(this);
	}
	
	public ComplexMaterial init(BlockRegistry blocksRegistry, ItemRegistry itemsRegistry, PathConfig recipeConfig) {
		initTags();
		
		final FabricBlockSettings blockSettings = getBlockSettings();
		final FabricItemSettings itemSettings = getItemSettings(itemsRegistry);
		initDefault(blockSettings, itemSettings);
		
		getBlockEntries().forEach(entry -> {
			Block block = entry.init(this, blockSettings, blocksRegistry);
			blocks.put(entry.getSuffix(), block);
		});
		
		getItemEntries().forEach(entry -> {
			Item item = entry.init(this, itemSettings, itemsRegistry);
			items.put(entry.getSuffix(), item);
		});
		
		initRecipes(recipeConfig);
		initFlammable();
		return this;
	}
	
	protected abstract void initDefault(FabricBlockSettings blockSettings, FabricItemSettings itemSettings);
	
	protected void initTags() {}
	
	protected void initRecipes(PathConfig recipeConfig) {}
	
	protected void initFlammable() {}
	
	protected void addBlockTag(Tag.Named<Block> tag) {
		blockTags.put(tag.getName().getPath(), tag);
	}
	
	protected void addItemTag(Tag.Named<Item> tag) {
		itemTags.put(tag.getName().getPath(), tag);
	}
	
	@Nullable
	public Tag.Named<Block> getBlockTag(String key) {
		return blockTags.get(key);
	}
	
	@Nullable
	public Tag.Named<Item> getItemTag(String key) {
		return itemTags.get(key);
	}
	
	@Nullable
	public Block getBlock(String key) {
		return blocks.get(key);
	}
	
	@Nullable
	public Item getItem(String key) {
		return items.get(key);
	}
	
	protected abstract FabricBlockSettings getBlockSettings();
	
	protected FabricItemSettings getItemSettings(ItemRegistry registry) {
		return registry.makeItemSettings();
	}
	
	private Collection<BlockEntry> getBlockEntries() {
		List<BlockEntry> result = Lists.newArrayList(defaultBlockEntries);
		List<BlockEntry> entries = BLOCK_ENTRIES.get(this.getMaterialID());
		if (entries != null) {
			result.addAll(entries);
		}
		return result;
	}
	
	private Collection<ItemEntry> getItemEntries() {
		List<ItemEntry> result = Lists.newArrayList(defaultItemEntries);
		List<ItemEntry> entries = ITEM_ENTRIES.get(this.getMaterialID());
		if (entries != null) {
			result.addAll(entries);
		}
		return result;
	}
	
	public String getBaseName() {
		return baseName;
	}
	
	public String getModID() {
		return modID;
	}
	
	public abstract ResourceLocation getMaterialID();
	
	protected void addBlockEntry(BlockEntry entry) {
		defaultBlockEntries.add(entry);
	}
	
	protected void addItemEntry(ItemEntry entry) {
		defaultItemEntries.add(entry);
	}
	
	public static void addBlockEntry(ResourceLocation materialName, BlockEntry entry) {
		List<BlockEntry> entries = BLOCK_ENTRIES.get(materialName);
		if (entries == null) {
			entries = Lists.newArrayList();
			BLOCK_ENTRIES.put(materialName, entries);
		}
		entries.add(entry);
	}
	
	public static void addItemEntry(ResourceLocation materialName, ItemEntry entry) {
		List<ItemEntry> entries = ITEM_ENTRIES.get(materialName);
		if (entries == null) {
			entries = Lists.newArrayList();
			ITEM_ENTRIES.put(materialName, entries);
		}
		entries.add(entry);
	}
	
	public static Collection<ComplexMaterial> getAllMaterials() {
		return MATERIALS;
	}
}
