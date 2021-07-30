package ru.bclib.complexmaterials;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MaterialColor;
import ru.bclib.BCLib;
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
import ru.bclib.blocks.BaseStripableLogBlock;
import ru.bclib.blocks.BaseTrapdoorBlock;
import ru.bclib.blocks.BaseWoodenButtonBlock;
import ru.bclib.blocks.StripableBarkBlock;
import ru.bclib.blocks.WoodenPressurePlateBlock;
import ru.bclib.complexmaterials.entry.BlockEntry;
import ru.bclib.complexmaterials.entry.RecipeEntry;
import ru.bclib.recipes.GridRecipe;

public class WoodenComplexMaterial extends ComplexMaterial {
	public static final ResourceLocation MATERIAL_ID = BCLib.makeID("wooden_material");
	
	public static final String BLOCK_CRAFTING_TABLE = "crafting_table";
	public static final String BLOCK_STRIPPED_BARK = "stripped_bark";
	public static final String BLOCK_STRIPPED_LOG = "stripped_log";
	public static final String BLOCK_PRESSURE_PLATE = "plate";
	public static final String BLOCK_BOOKSHELF = "bookshelf";
	public static final String BLOCK_COMPOSTER = "composter";
	public static final String BLOCK_TRAPDOOR = "trapdoor";
	public static final String BLOCK_BARREL = "barrel";
	public static final String BLOCK_BUTTON = "button";
	public static final String BLOCK_LADDER = "ladder";
	public static final String BLOCK_PLANKS = "planks";
	public static final String BLOCK_STAIRS = "stairs";
	public static final String BLOCK_CHEST = "chest";
	public static final String BLOCK_FENCE = "fence";
	public static final String BLOCK_BARK = "bark";
	public static final String BLOCK_DOOR = "door";
	public static final String BLOCK_GATE = "gate";
	public static final String BLOCK_SIGN = "sign";
	public static final String BLOCK_SLAB = "slab";
	public static final String BLOCK_LOG = "log";
	
	public static final String TAG_LOGS = "logs";
	
	public final MaterialColor planksColor;
	public final MaterialColor woodColor;
	
	public WoodenComplexMaterial(String modID, String baseName, String receipGroupPrefix, MaterialColor woodColor, MaterialColor planksColor) {
		super(modID, baseName, receipGroupPrefix);
		this.planksColor = planksColor;
		this.woodColor = woodColor;
	}
	
	@Override
	protected FabricBlockSettings getBlockSettings() {
		return FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).materialColor(planksColor);
	}
	
	@Override
	public ResourceLocation getMaterialID() {
		return MATERIAL_ID;
	}
	
	@Override
	protected void initTags() {
		addBlockTag(TagAPI.makeBlockTag(getModID(), getBaseName() + "_logs"));
		addItemTag(TagAPI.makeItemTag(getModID(), getBaseName() + "_logs"));
	}
	
	@Override
	protected void initDefault(FabricBlockSettings blockSettings, FabricItemSettings itemSettings) {
		initBase(blockSettings, itemSettings);
		initStorage(blockSettings, itemSettings);
		initDecorations(blockSettings, itemSettings);
	}
	
	final protected void initBase(FabricBlockSettings blockSettings, FabricItemSettings itemSettings) {
		Tag.Named<Block> tagBlockLog = getBlockTag(TAG_LOGS);
		Tag.Named<Item> tagItemLog = getItemTag(TAG_LOGS);
		
		addBlockEntry(
			new BlockEntry(BLOCK_STRIPPED_LOG, (complexMaterial, settings) -> {
				return new BaseRotatedPillarBlock(settings);
			})
			.setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
			.setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
		);
		addBlockEntry(
			new BlockEntry(BLOCK_STRIPPED_BARK, (complexMaterial, settings) -> {
				return new BaseBarkBlock(settings);
			})
		  .setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
		  .setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
		);
		
		addBlockEntry(
			new BlockEntry(BLOCK_LOG, (complexMaterial, settings) -> {
				return new BaseStripableLogBlock(woodColor, getBlock("stripped_log"));
			})
			.setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
			.setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
		);
		addBlockEntry(
			new BlockEntry(BLOCK_BARK, (complexMaterial, settings) -> {
				return new StripableBarkBlock(woodColor, getBlock("stripped_bark"));
			})
			.setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
			.setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
		);
		addBlockEntry(new BlockEntry(BLOCK_PLANKS, (complexMaterial, settings) -> {
			return new BaseBlock(settings);
		}).setBlockTags(BlockTags.PLANKS).setItemTags(ItemTags.PLANKS));
		
		addBlockEntry(new BlockEntry(BLOCK_STAIRS, (complexMaterial, settings) -> {
			return new BaseStairsBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.WOODEN_STAIRS, BlockTags.STAIRS).setItemTags(ItemTags.WOODEN_STAIRS, ItemTags.STAIRS));
		addBlockEntry(new BlockEntry(BLOCK_SLAB, (complexMaterial, settings) -> {
			return new BaseSlabBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.WOODEN_SLABS, BlockTags.SLABS).setItemTags(ItemTags.WOODEN_SLABS, ItemTags.SLABS));
		addBlockEntry(new BlockEntry(BLOCK_FENCE, (complexMaterial, settings) -> {
			return new BaseFenceBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.FENCES, BlockTags.WOODEN_FENCES).setItemTags(ItemTags.FENCES, ItemTags.WOODEN_FENCES));
		addBlockEntry(new BlockEntry(BLOCK_GATE, (complexMaterial, settings) -> {
			return new BaseGateBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.FENCE_GATES));
		addBlockEntry(new BlockEntry(BLOCK_BUTTON, (complexMaterial, settings) -> {
			return new BaseWoodenButtonBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.BUTTONS, BlockTags.WOODEN_BUTTONS).setItemTags(ItemTags.BUTTONS, ItemTags.WOODEN_BUTTONS));
		addBlockEntry(new BlockEntry(BLOCK_PRESSURE_PLATE, (complexMaterial, settings) -> {
			return new WoodenPressurePlateBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.PRESSURE_PLATES, BlockTags.WOODEN_PRESSURE_PLATES).setItemTags(ItemTags.WOODEN_PRESSURE_PLATES));
		addBlockEntry(new BlockEntry(BLOCK_TRAPDOOR, (complexMaterial, settings) -> {
			return new BaseTrapdoorBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.TRAPDOORS, BlockTags.WOODEN_TRAPDOORS).setItemTags(ItemTags.TRAPDOORS, ItemTags.WOODEN_TRAPDOORS));
		addBlockEntry(new BlockEntry(BLOCK_DOOR, (complexMaterial, settings) -> {
			return new BaseDoorBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.DOORS, BlockTags.WOODEN_DOORS).setItemTags(ItemTags.DOORS, ItemTags.WOODEN_DOORS));
		
		
		addBlockEntry(new BlockEntry(BLOCK_LADDER, (complexMaterial, settings) -> {
			return new BaseLadderBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.CLIMBABLE));
		addBlockEntry(new BlockEntry(BLOCK_SIGN, (complexMaterial, settings) -> {
			return new BaseSignBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.SIGNS).setItemTags(ItemTags.SIGNS));
		
		
	}
	
	final protected void initStorage(FabricBlockSettings blockSettings, FabricItemSettings itemSettings){
		addBlockEntry(new BlockEntry(BLOCK_CHEST, (complexMaterial, settings) -> {
			return new BaseChestBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(TagAPI.BLOCK_CHEST).setItemTags(TagAPI.ITEM_CHEST));
		addBlockEntry(new BlockEntry(BLOCK_BARREL, (complexMaterial, settings) -> {
			return new BaseBarrelBlock(getBlock(BLOCK_PLANKS));
		}));
	}
	
	final protected void initDecorations(FabricBlockSettings blockSettings, FabricItemSettings itemSettings){
		addBlockEntry(new BlockEntry(BLOCK_CRAFTING_TABLE, (complexMaterial, settings) -> {
			return new BaseCraftingTableBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(TagAPI.BLOCK_WORKBENCHES).setItemTags(TagAPI.ITEM_WORKBENCHES));
		
		addBlockEntry(new BlockEntry(BLOCK_BOOKSHELF, (complexMaterial, settings) -> {
			return new BaseBookshelfBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(TagAPI.BLOCK_BOOKSHELVES));
		
		addBlockEntry(new BlockEntry(BLOCK_COMPOSTER, (complexMaterial, settings) -> {
			return new BaseComposterBlock(getBlock(BLOCK_PLANKS));
		}));
	}
	
	@Override
	protected void initFlammable(FlammableBlockRegistry registry) {
		getBlocks().forEach(block -> {
			registry.add(block, 5, 20);
		});
		
		registry.add(getBlock(BLOCK_LOG), 5, 5);
		registry.add(getBlock(BLOCK_BARK), 5, 5);
		registry.add(getBlock(BLOCK_STRIPPED_LOG), 5, 5);
		registry.add(getBlock(BLOCK_STRIPPED_BARK), 5, 5);
	}
	
	@Override
	public void initDefaultRecipes() {
		Block planks = getBlock(BLOCK_PLANKS);
		addRecipeEntry(new RecipeEntry("planks", (material, config, id) -> {
			Block log_stripped = getBlock(BLOCK_STRIPPED_LOG);
			Block bark_stripped = getBlock(BLOCK_STRIPPED_BARK);
			Block log = getBlock(BLOCK_LOG);
			Block bark = getBlock(BLOCK_BARK);
			GridRecipe.make(id, planks)
				.checkConfig(config)
				.setOutputCount(4)
				.setList("#")
				.addMaterial('#', log, bark, log_stripped, bark_stripped)
				.setGroup(receipGroupPrefix +"_planks")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("stairs", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_STAIRS))
				.checkConfig(config)
				.setOutputCount(4)
				.setShape("#  ", "## ", "###")
				.addMaterial('#', planks)
				.setGroup(receipGroupPrefix +"_planks_stairs")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("slab", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_SLAB))
				.checkConfig(config)
				.setOutputCount(6)
				.setShape("###")
				.addMaterial('#', planks)
				.setGroup(receipGroupPrefix +"_planks_slabs")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("fence", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_FENCE))
				.checkConfig(config)
				.setOutputCount(3)
				.setShape("#I#", "#I#")
				.addMaterial('#', planks)
				.addMaterial('I', Items.STICK)
				.setGroup(receipGroupPrefix +"_planks_fences")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("gate", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_GATE))
				.checkConfig(config)
				.setShape("I#I", "I#I")
				.addMaterial('#', planks)
				.addMaterial('I', Items.STICK)
				.setGroup(receipGroupPrefix +"_planks_gates")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("button", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_BUTTON))
				.checkConfig(config)
				.setList("#")
				.addMaterial('#', planks)
				.setGroup(receipGroupPrefix +"_planks_buttons")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("pressure_plate", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_PRESSURE_PLATE))
				.checkConfig(config)
				.setShape("##")
				.addMaterial('#', planks)
				.setGroup(receipGroupPrefix +"_planks_plates")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("trapdoor", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_TRAPDOOR))
				.checkConfig(config)
				.setOutputCount(2)
				.setShape("###", "###")
				.addMaterial('#', planks)
				.setGroup(receipGroupPrefix +"_trapdoors")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("door", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_DOOR))
				.checkConfig(config)
				.setOutputCount(3)
				.setShape("##", "##", "##")
				.addMaterial('#', planks)
				.setGroup(receipGroupPrefix +"_doors")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("crafting_table", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_CRAFTING_TABLE))
				.checkConfig(config)
				.setShape("##", "##")
				.addMaterial('#', planks)
				.setGroup(receipGroupPrefix +"_tables")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("ladder", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_LADDER))
				.checkConfig(config)
				.setOutputCount(3)
				.setShape("I I", "I#I", "I I")
				.addMaterial('#', planks)
				.addMaterial('I', Items.STICK)
				.setGroup(receipGroupPrefix +"_ladders")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("sign", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_SIGN))
				.checkConfig(config)
				.setOutputCount(3)
				.setShape("###", "###", " I ")
				.addMaterial('#', planks)
				.addMaterial('I', Items.STICK)
				.setGroup(receipGroupPrefix +"_signs")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("chest", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_CHEST))
				.checkConfig(config)
				.setShape("###", "# #", "###")
				.addMaterial('#', planks)
				.setGroup(receipGroupPrefix +"_chests")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("barrel", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_BARREL))
				.checkConfig(config)
				.setShape("#S#", "# #", "#S#")
				.addMaterial('#', planks)
				.addMaterial('S', getBlock(BLOCK_SLAB))
				.setGroup(receipGroupPrefix +"_barrels")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("bookshelf", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_BOOKSHELF))
				.checkConfig(config)
				.setShape("###", "PPP", "###")
				.addMaterial('#', planks)
				.addMaterial('P', Items.BOOK)
				.setGroup(receipGroupPrefix +"_bookshelves")
				.build();
		}));
		addRecipeEntry(new RecipeEntry("bark", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_BARK))
				.checkConfig(config)
				.setShape("##", "##")
				.addMaterial('#', getBlock(BLOCK_LOG))
				.setOutputCount(3)
				.build();
		}));
		addRecipeEntry(new RecipeEntry("log", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_LOG))
				.checkConfig(config)
				.setShape("##", "##")
				.addMaterial('#', getBlock(BLOCK_BARK))
				.setOutputCount(3)
				.build();
		}));
		addRecipeEntry(new RecipeEntry("stripped_bark", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_STRIPPED_BARK))
				.checkConfig(config)
				.setShape("##", "##")
				.addMaterial('#', getBlock(BLOCK_STRIPPED_LOG))
				.setOutputCount(3)
				.build();
		}));
		addRecipeEntry(new RecipeEntry("stripped_log", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_STRIPPED_LOG))
				.checkConfig(config)
				.setShape("##", "##")
				.addMaterial('#', getBlock(BLOCK_STRIPPED_BARK))
				.setOutputCount(3)
				.build();
		}));
		addRecipeEntry(new RecipeEntry("composter", (material, config, id) -> {
			GridRecipe.make(id, getBlock(BLOCK_COMPOSTER))
				.checkConfig(config)
				.setShape("# #", "# #", "###")
				.addMaterial('#', getBlock(BLOCK_SLAB))
				.build();
		}));
		addRecipeEntry(new RecipeEntry("shulker", (material, config, id) -> {
			GridRecipe.make(id, Blocks.SHULKER_BOX)
				.checkConfig(config)
				.setShape("S", "#", "S")
				.addMaterial('S', Items.SHULKER_SHELL)
				.addMaterial('#', getBlock(BLOCK_CHEST))
				.build();
		}));
	}
}