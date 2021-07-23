package ru.bclib.complexmaterials;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MaterialColor;
import ru.bclib.api.TagAPI;
import ru.bclib.blocks.BaseBarkBlock;
import ru.bclib.blocks.BaseBarrelBlock;
import ru.bclib.blocks.BaseBlock;
import ru.bclib.blocks.BaseBookshelfBlock;
import ru.bclib.blocks.BaseChestBlock;
import ru.bclib.blocks.BaseComposterBlock;
import ru.bclib.blocks.BaseCraftingTableBlock;
import ru.bclib.blocks.BaseDoorBlock;
import ru.bclib.blocks.BaseFenceBlock;
import ru.bclib.blocks.BaseGateBlock;
import ru.bclib.blocks.BaseLadderBlock;
import ru.bclib.blocks.BaseRotatedPillarBlock;
import ru.bclib.blocks.BaseSignBlock;
import ru.bclib.blocks.BaseSlabBlock;
import ru.bclib.blocks.BaseStairsBlock;
import ru.bclib.blocks.BaseTrapdoorBlock;
import ru.bclib.blocks.BaseWoodenButtonBlock;
import ru.bclib.blocks.StripableBarkBlock;
import ru.bclib.blocks.WoodenPressurePlateBlock;
import ru.bclib.complexmaterials.entry.BlockEntry;
import ru.bclib.registry.BlocksRegistry;
import ru.bclib.registry.ItemsRegistry;

public class WoodenMaterial extends ComplexMaterial {
	public final MaterialColor planksColor;
	public final MaterialColor woodColor;
	
	public WoodenMaterial(String modID, String baseName, MaterialColor woodColor, MaterialColor planksColor, BlocksRegistry blocksRegistry, ItemsRegistry itemsRegistry) {
		super(modID, baseName, blocksRegistry, itemsRegistry);
		this.planksColor = planksColor;
		this.woodColor = woodColor;
	}
	
	@Override
	protected FabricBlockSettings getBlockSettings() {
		return FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).materialColor(planksColor);
	}
	
	@Override
	protected void initTags() {
		addBlockTag(TagAPI.makeBlockTag(getModID(), getBaseName() + "_logs"));
		addItemTag(TagAPI.makeItemTag(getModID(), getBaseName() + "_logs"));
	}
	
	@Override
	protected void initDefault(FabricBlockSettings blockSettings, FabricItemSettings itemSettings) {
		Tag.Named<Block> tagBlockLog = getBlockTag(getBaseName() + "_logs");
		Tag.Named<Item> tagItemLog = getItemTag(getBaseName() + "_logs");
		
		addBlockEntry(
			new BlockEntry("stripped_log", (complexMaterial, settings) -> {
				return new BaseRotatedPillarBlock(settings);
			})
			.setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
			.setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
		);
		addBlockEntry(
			new BlockEntry("stripped_bark", (complexMaterial, settings) -> {
				return new BaseBarkBlock(settings);
			})
		  .setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
		  .setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
		);
		
		addBlockEntry(
			new BlockEntry("log", (complexMaterial, settings) -> {
				return new StripableBarkBlock(woodColor, getBlock("log_stripped"));
			})
			.setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
			.setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
		);
		addBlockEntry(
			new BlockEntry("bark", (complexMaterial, settings) -> {
				return new StripableBarkBlock(woodColor, getBlock("bark_stripped"));
			})
			.setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
			.setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
		);
		addBlockEntry(new BlockEntry("planks", (complexMaterial, settings) -> {
			return new BaseBlock(settings);
		}).setBlockTags(BlockTags.PLANKS).setItemTags(ItemTags.PLANKS));
		
		final Block planks = getBlock("planks");
		addBlockEntry(new BlockEntry("stairs", (complexMaterial, settings) -> {
			return new BaseStairsBlock(planks);
		}).setBlockTags(BlockTags.WOODEN_STAIRS, BlockTags.STAIRS).setItemTags(ItemTags.WOODEN_STAIRS, ItemTags.STAIRS));
		addBlockEntry(new BlockEntry("slab", (complexMaterial, settings) -> {
			return new BaseSlabBlock(planks);
		}).setBlockTags(BlockTags.WOODEN_SLABS, BlockTags.SLABS).setItemTags(ItemTags.WOODEN_SLABS, ItemTags.SLABS));
		addBlockEntry(new BlockEntry("fence", (complexMaterial, settings) -> {
			return new BaseFenceBlock(planks);
		}).setBlockTags(BlockTags.FENCES, BlockTags.WOODEN_FENCES).setItemTags(ItemTags.FENCES, ItemTags.WOODEN_FENCES));
		addBlockEntry(new BlockEntry("gate", (complexMaterial, settings) -> {
			return new BaseGateBlock(planks);
		}).setBlockTags(BlockTags.FENCE_GATES));
		addBlockEntry(new BlockEntry("button", (complexMaterial, settings) -> {
			return new BaseWoodenButtonBlock(planks);
		}).setBlockTags(BlockTags.BUTTONS, BlockTags.WOODEN_BUTTONS).setItemTags(ItemTags.BUTTONS, ItemTags.WOODEN_BUTTONS));
		addBlockEntry(new BlockEntry("plate", (complexMaterial, settings) -> {
			return new WoodenPressurePlateBlock(planks);
		}).setBlockTags(BlockTags.PRESSURE_PLATES, BlockTags.WOODEN_PRESSURE_PLATES).setItemTags(ItemTags.WOODEN_PRESSURE_PLATES));
		addBlockEntry(new BlockEntry("trapdoor", (complexMaterial, settings) -> {
			return new BaseTrapdoorBlock(planks);
		}).setBlockTags(BlockTags.TRAPDOORS, BlockTags.WOODEN_TRAPDOORS).setItemTags(ItemTags.TRAPDOORS, ItemTags.WOODEN_TRAPDOORS));
		addBlockEntry(new BlockEntry("door", (complexMaterial, settings) -> {
			return new BaseDoorBlock(planks);
		}).setBlockTags(BlockTags.DOORS, BlockTags.WOODEN_DOORS).setItemTags(ItemTags.DOORS, ItemTags.WOODEN_DOORS));
		
		addBlockEntry(new BlockEntry("crafting_table", (complexMaterial, settings) -> {
			return new BaseCraftingTableBlock(planks);
		}).setBlockTags(TagAPI.BLOCK_WORKBENCHES).setItemTags(TagAPI.ITEM_WORKBENCHES));
		addBlockEntry(new BlockEntry("ladder", (complexMaterial, settings) -> {
			return new BaseLadderBlock(planks);
		}).setBlockTags(BlockTags.CLIMBABLE));
		addBlockEntry(new BlockEntry("sign", (complexMaterial, settings) -> {
			return new BaseSignBlock(planks);
		}).setBlockTags(BlockTags.SIGNS).setItemTags(ItemTags.SIGNS));
		
		addBlockEntry(new BlockEntry("chest", (complexMaterial, settings) -> {
			return new BaseChestBlock(planks);
		}).setBlockTags(TagAPI.BLOCK_CHEST).setItemTags(TagAPI.ITEM_CHEST));
		addBlockEntry(new BlockEntry("barrel", (complexMaterial, settings) -> {
			return new BaseBarrelBlock(planks);
		}));
		addBlockEntry(new BlockEntry("bookshelf", (complexMaterial, settings) -> {
			return new BaseBookshelfBlock(planks);
		}).setBlockTags(TagAPI.BLOCK_BOOKSHELVES));
		addBlockEntry(new BlockEntry("composter", (complexMaterial, settings) -> {
			return new BaseComposterBlock(planks);
		}));
	}
	
	protected void initFlammable() {
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("log"), 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("bark"), 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("log_stripped"), 5, 5);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("bark_stripped"), 5, 5);
		
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("planks"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("stairs"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("slab"), 5, 20);
		
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("fence"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("gate"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("button"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("pressurePlate"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("trapdoor"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("door"), 5, 20);
		
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("craftingTable"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("ladder"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("sign"), 5, 20);
		
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("chest"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("barrel"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("shelf"), 5, 20);
		FlammableBlockRegistry.getDefaultInstance().add(getBlock("composter"), 5, 20);
	}
	
	@Override
	public void initRecipes() {
	
	}
}