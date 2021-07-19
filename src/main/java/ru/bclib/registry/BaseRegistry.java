package ru.bclib.registry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import ru.bclib.BCLib;

import java.util.List;
import java.util.Map;

public abstract class BaseRegistry<T> {
	private static final List<BaseRegistry<?>> REGISTRIES = Lists.newArrayList();
	private static final Map<String, List<Item>> MOD_BLOCKS = Maps.newHashMap();
	private static final Map<String, List<Item>> MOD_ITEMS = Maps.newHashMap();
	
	protected final CreativeModeTab creativeTab;
	
	public static Map<String, List<Item>> getRegisteredBlocks() {
		return MOD_BLOCKS;
	}
	
	public static Map<String, List<Item>> getRegisteredItems() {
		return MOD_ITEMS;
	}
	
	public static List<Item> getModBlocks(String modId) {
		if (MOD_BLOCKS.containsKey(modId)) {
			return MOD_BLOCKS.get(modId);
		}
		List<Item> modBlocks = Lists.newArrayList();
		MOD_BLOCKS.put(modId, modBlocks);
		return modBlocks;
	}
	
	public static List<Item> getModItems(String modId) {
		if (MOD_ITEMS.containsKey(modId)) {
			return MOD_ITEMS.get(modId);
		}
		List<Item> modBlocks = Lists.newArrayList();
		MOD_ITEMS.put(modId, modBlocks);
		return modBlocks;
	}
	
	public static void register() {
		REGISTRIES.forEach(BaseRegistry::registerInternal);
	}
	
	protected BaseRegistry(CreativeModeTab creativeTab) {
		this.creativeTab = creativeTab;
		REGISTRIES.add(this);
	}
	
	public T register(String name, T obj) {
		return register(createModId(name), obj);
	}
	
	public abstract T register(ResourceLocation objId, T obj);
	
	public ResourceLocation createModId(String name) {
		return BCLib.makeID(name);
	}
	
	public void registerItem(ResourceLocation id, Item item, List<Item> registry) {
		if (item != Items.AIR) {
			Registry.register(Registry.ITEM, id, item);
			registry.add(item);
		}
	}
	
	public FabricItemSettings makeItemSettings() {
		FabricItemSettings properties = new FabricItemSettings();
		return (FabricItemSettings) properties.tab(creativeTab);
	}
	
	private void registerInternal() {}
}
