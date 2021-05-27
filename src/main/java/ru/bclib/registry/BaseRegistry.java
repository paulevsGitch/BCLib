package ru.bclib.registry;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import ru.bclib.BCLib;

import java.util.List;

public abstract class BaseRegistry<T> {

	private static final List<BaseRegistry<?>> REGISTRIES = Lists.newArrayList();

	protected static final List<Item> MOD_BLOCKS = Lists.newArrayList();
	protected static final List<Item> MOD_ITEMS = Lists.newArrayList();

	public static List<Item> getModBlocks() {
		return MOD_BLOCKS;
	}

	public static List<Item> getModItems() {
		return MOD_ITEMS;
	}

	public static void register() {
		REGISTRIES.forEach(BaseRegistry::registerDependency);
	}

	protected final CreativeModeTab creativeTab;

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

	private void registerDependency() {}
}
