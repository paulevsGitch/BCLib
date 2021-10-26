package ru.bclib.registry;

import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.DispenserBlock;
import ru.bclib.api.TagAPI;
import ru.bclib.config.PathConfig;
import ru.bclib.items.BaseDiscItem;
import ru.bclib.items.BaseDrinkItem;
import ru.bclib.items.BaseSpawnEggItem;
import ru.bclib.items.ModelProviderItem;
import ru.bclib.items.tool.BaseAxeItem;
import ru.bclib.items.tool.BaseHoeItem;
import ru.bclib.items.tool.BasePickaxeItem;
import ru.bclib.items.tool.BaseShearsItem;

public class ItemRegistry extends BaseRegistry<Item> {
	public ItemRegistry(CreativeModeTab creativeTab, PathConfig config) {
		super(creativeTab, config);
	}
	
	public Item registerDisc(ResourceLocation itemId, int power, SoundEvent sound) {
		BaseDiscItem item = new BaseDiscItem(power, sound, makeItemSettings().stacksTo(1));
		
		if (!config.getBoolean("musicDiscs", itemId.getPath(), true)) {
			return item;
		}
		
		return register(itemId, new BaseDiscItem(power, sound, makeItemSettings().stacksTo(1)));
	}
	
	public Item register(ResourceLocation itemId) {
		return register(itemId, new ModelProviderItem(makeItemSettings()));
	}
	
	@Override
	public Item register(ResourceLocation itemId, Item item) {
		if (!config.getBoolean("items", itemId.getPath(), true)) {
			return item;
		}
		
		registerItem(itemId, item);
		
		return item;
	}
	
	public Item registerTool(ResourceLocation itemId, Item item) {
		if (!config.getBoolean("tools", itemId.getPath(), true)) {
			return item;
		}
		
		registerItem(itemId, item);
		
		if (item instanceof ShovelItem) {
			TagAPI.addTag((Tag.Named<Item>) FabricToolTags.SHOVELS, item);
		}
		else if (item instanceof SwordItem) {
			TagAPI.addTag((Tag.Named<Item>) FabricToolTags.SWORDS, item);
		}
		else if (item instanceof BasePickaxeItem) {
			TagAPI.addTag((Tag.Named<Item>) FabricToolTags.PICKAXES, item);
		}
		else if (item instanceof BaseAxeItem) {
			TagAPI.addTag((Tag.Named<Item>) FabricToolTags.AXES, item);
		}
		else if (item instanceof BaseHoeItem) {
			TagAPI.addTag((Tag.Named<Item>) FabricToolTags.HOES, item);
		}
		else if (item instanceof BaseShearsItem) {
			TagAPI.addTags(item, (Tag.Named<Item>) FabricToolTags.SHEARS, TagAPI.ITEM_SHEARS, TagAPI.ITEM_COMMON_SHEARS);
			DispenserBlock.registerBehavior(item.asItem(), new ShearsDispenseItemBehavior());
		}
		
		return item;
	}
	
	public Item registerEgg(ResourceLocation itemId, EntityType<? extends Mob> type, int background, int dots) {
		SpawnEggItem item = new BaseSpawnEggItem(type, background, dots, makeItemSettings());
		
		if (!config.getBoolean("spawnEggs", itemId.getPath(), true)) {
			return item;
		}
		
		DefaultDispenseItemBehavior behavior = new DefaultDispenseItemBehavior() {
			public ItemStack execute(BlockSource pointer, ItemStack stack) {
				Direction direction = pointer.getBlockState().getValue(DispenserBlock.FACING);
				EntityType<?> entityType = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
				entityType.spawn(
					pointer.getLevel(),
					stack,
					null,
					pointer.getPos().relative(direction),
					MobSpawnType.DISPENSER,
					direction != Direction.UP,
					false
				);
				stack.shrink(1);
				return stack;
			}
		};
		DispenserBlock.registerBehavior(item, behavior);
		return register(itemId, item);
	}
	
	public Item registerFood(ResourceLocation itemId, int hunger, float saturation, MobEffectInstance... effects) {
		FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(hunger).saturationMod(saturation);
		for (MobEffectInstance effect : effects) {
			builder.effect(effect, 1F);
		}
		return registerFood(itemId, builder.build());
	}
	
	public Item registerFood(ResourceLocation itemId, FoodProperties foodComponent) {
		return register(itemId, new ModelProviderItem(makeItemSettings().food(foodComponent)));
	}
	
	public Item registerDrink(ResourceLocation itemId, FoodProperties foodComponent) {
		return register(itemId, new BaseDrinkItem(makeItemSettings().stacksTo(1).food(foodComponent)));
	}
	
	public Item registerDrink(ResourceLocation itemId, int hunger, float saturation) {
		FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(hunger).saturationMod(saturation);
		return registerDrink(itemId, builder.build());
	}
	
	@Override
	public void registerItem(ResourceLocation id, Item item) {
		if (item != null && item != Items.AIR) {
			Registry.register(Registry.ITEM, id, item);
			getModItems(id.getNamespace()).add(item);
		}
	}
	
	public Item register(ResourceLocation itemId, Item item, String category) {
		if (config.getBoolean(category, itemId.getPath(), true)) {
			registerItem(itemId, item);
		}
		return item;
	}
}
