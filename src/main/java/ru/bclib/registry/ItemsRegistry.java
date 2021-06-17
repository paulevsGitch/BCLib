package ru.bclib.registry;

import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.DispenserBlock;
import ru.bclib.items.BaseDrinkItem;
import ru.bclib.items.BaseSpawnEggItem;
import ru.bclib.items.BaseDiscItem;
import ru.bclib.items.ModelProviderItem;
import ru.bclib.items.tool.BaseAxeItem;
import ru.bclib.items.tool.BaseHoeItem;
import ru.bclib.items.tool.BasePickaxeItem;
import ru.bclib.util.TagHelper;

public abstract class ItemsRegistry extends BaseRegistry<Item> {

	protected ItemsRegistry(CreativeModeTab creativeTab) {
		super(creativeTab);
	}

	public Item registerDisc(String name, int power, SoundEvent sound) {
		return register(name, new BaseDiscItem(power, sound, makeItemSettings().stacksTo(1)));
	}

	public Item registerItem(String name) {
		return register(name, new ModelProviderItem(makeItemSettings()));
	}

	@Override
	public Item register(ResourceLocation itemId, Item item) {
		registerItem(itemId, item, BaseRegistry.getModItems(itemId.getNamespace()));
		return item;
	}

	public TieredItem registerTool(String name, TieredItem item) {
		ResourceLocation id = createModId(name);
		registerItem(id, item, BaseRegistry.getModItems(id.getNamespace()));

		if (item instanceof ShovelItem) {
			TagHelper.addTag((Tag.Named<Item>) FabricToolTags.SHOVELS, item);
		} else if (item instanceof SwordItem) {
			TagHelper.addTag((Tag.Named<Item>) FabricToolTags.SWORDS, item);
		} else if (item instanceof BasePickaxeItem) {
			TagHelper.addTag((Tag.Named<Item>) FabricToolTags.PICKAXES, item);
		} else if (item instanceof BaseAxeItem) {
			TagHelper.addTag((Tag.Named<Item>) FabricToolTags.AXES, item);
		} else if (item instanceof BaseHoeItem) {
			TagHelper.addTag((Tag.Named<Item>) FabricToolTags.HOES, item);
		}

		return item;
	}

	public Item registerEgg(String name, EntityType<?> type, int background, int dots) {
		SpawnEggItem item = new BaseSpawnEggItem(type, background, dots, makeItemSettings());
		DefaultDispenseItemBehavior behavior = new DefaultDispenseItemBehavior() {
			public ItemStack execute(BlockSource pointer, ItemStack stack) {
				Direction direction = pointer.getBlockState().getValue(DispenserBlock.FACING);
				EntityType<?> entityType = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
				entityType.spawn(pointer.getLevel(), stack, null, pointer.getPos().relative(direction), MobSpawnType.DISPENSER, direction != Direction.UP, false);
				stack.shrink(1);
				return stack;
			}
		};
		DispenserBlock.registerBehavior(item, behavior);
		return register(name, item);
	}

	public Item registerFood(String name, int hunger, float saturation, MobEffectInstance... effects) {
		FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(hunger).saturationMod(saturation);
		for (MobEffectInstance effect: effects) {
			builder.effect(effect, 1F);
		}
		return registerFood(name, builder.build());
	}

	public Item registerFood(String name, FoodProperties foodComponent) {
		return register(name, new ModelProviderItem(makeItemSettings().food(foodComponent)));
	}

	public Item registerDrink(String name) {
		return register(name, new BaseDrinkItem(makeItemSettings().stacksTo(1)));
	}

	public Item registerDrink(String name, FoodProperties foodComponent) {
		return register(name, new BaseDrinkItem(makeItemSettings().stacksTo(1).food(foodComponent)));
	}

	public Item registerDrink(String name, int hunger, float saturation) {
		FoodProperties.Builder builder = new FoodProperties.Builder().nutrition(hunger).saturationMod(saturation);
		return registerDrink(name, builder.build());
	}
}
