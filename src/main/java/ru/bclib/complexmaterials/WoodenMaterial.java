package ru.bclib.complexmaterials;

import com.google.common.collect.Lists;
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
import ru.bclib.complexmaterials.entry.ItemEntry;
import ru.bclib.config.PathConfig;
import ru.bclib.recipes.GridRecipe;

import java.util.List;

public class WoodenMaterial extends ComplexMaterial {
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
	
	public WoodenMaterial(String modID, String baseName, MaterialColor woodColor, MaterialColor planksColor) {
		super(modID, baseName);
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
		
		addBlockEntry(new BlockEntry(BLOCK_CRAFTING_TABLE, (complexMaterial, settings) -> {
			return new BaseCraftingTableBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(TagAPI.BLOCK_WORKBENCHES).setItemTags(TagAPI.ITEM_WORKBENCHES));
		addBlockEntry(new BlockEntry(BLOCK_LADDER, (complexMaterial, settings) -> {
			return new BaseLadderBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.CLIMBABLE));
		addBlockEntry(new BlockEntry(BLOCK_SIGN, (complexMaterial, settings) -> {
			return new BaseSignBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(BlockTags.SIGNS).setItemTags(ItemTags.SIGNS));
		
		addBlockEntry(new BlockEntry(BLOCK_CHEST, (complexMaterial, settings) -> {
			return new BaseChestBlock(getBlock(BLOCK_PLANKS));
		}).setBlockTags(TagAPI.BLOCK_CHEST).setItemTags(TagAPI.ITEM_CHEST));
		addBlockEntry(new BlockEntry(BLOCK_BARREL, (complexMaterial, settings) -> {
			return new BaseBarrelBlock(getBlock(BLOCK_PLANKS));
		}));
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
	public void initRecipes(PathConfig recipeConfig) {
		Block log_stripped = getBlock(BLOCK_STRIPPED_LOG);
		Block bark_stripped = getBlock(BLOCK_STRIPPED_BARK);
		Block log = getBlock(BLOCK_LOG);
		Block bark = getBlock(BLOCK_BARK);
		Block planks = getBlock(BLOCK_PLANKS);
		Block stairs = getBlock(BLOCK_STAIRS);
		Block slab = getBlock(BLOCK_SLAB);
		Block fence = getBlock(BLOCK_FENCE);
		Block gate = getBlock("gate");
		Block button = getBlock("button");
		Block pressurePlate = getBlock("plate");
		Block trapdoor = getBlock("trapdoor");
		Block door = getBlock("door");
		Block craftingTable = getBlock("crafting_table");
		Block ladder = getBlock("ladder");
		Block sign = getBlock("sign");
		Block chest = getBlock("chest");
		Block barrel = getBlock("barrel");
		Block shelf = getBlock("bookshelf");
		Block composter = getBlock("composter");
		
		GridRecipe.make(getModID(), getBaseName() + "_planks", planks)
				  .checkConfig(recipeConfig)
				  .setOutputCount(4)
				  .setList("#")
				  .addMaterial('#', log, bark, log_stripped, bark_stripped)
				  .setGroup("end_planks")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_stairs", stairs)
				  .checkConfig(recipeConfig)
				  .setOutputCount(4)
				  .setShape("#  ", "## ", "###")
				  .addMaterial('#', planks)
				  .setGroup("end_planks_stairs")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_slab", slab)
				  .checkConfig(recipeConfig)
				  .setOutputCount(6)
				  .setShape("###")
				  .addMaterial('#', planks)
				  .setGroup("end_planks_slabs")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_fence", fence)
				  .checkConfig(recipeConfig)
				  .setOutputCount(3)
				  .setShape("#I#", "#I#")
				  .addMaterial('#', planks)
				  .addMaterial('I', Items.STICK)
				  .setGroup("end_planks_fences")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_gate", gate)
				  .checkConfig(recipeConfig)
				  .setShape("I#I", "I#I")
				  .addMaterial('#', planks)
				  .addMaterial('I', Items.STICK)
				  .setGroup("end_planks_gates")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_button", button)
				  .checkConfig(recipeConfig)
				  .setList("#")
				  .addMaterial('#', planks)
				  .setGroup("end_planks_buttons")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_pressure_plate", pressurePlate)
				  .checkConfig(recipeConfig)
				  .setShape("##")
				  .addMaterial('#', planks)
				  .setGroup("end_planks_plates")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_trapdoor", trapdoor)
				  .checkConfig(recipeConfig)
				  .setOutputCount(2)
				  .setShape("###", "###")
				  .addMaterial('#', planks)
				  .setGroup("end_trapdoors")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_door", door)
				  .checkConfig(recipeConfig)
				  .setOutputCount(3)
				  .setShape("##", "##", "##")
				  .addMaterial('#', planks)
				  .setGroup("end_doors")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_crafting_table", craftingTable)
				  .checkConfig(recipeConfig)
				  .setShape("##", "##")
				  .addMaterial('#', planks)
				  .setGroup("end_tables")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_ladder", ladder)
				  .checkConfig(recipeConfig)
				  .setOutputCount(3)
				  .setShape("I I", "I#I", "I I")
				  .addMaterial('#', planks)
				  .addMaterial('I', Items.STICK)
				  .setGroup("end_ladders")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_sign", sign)
				  .checkConfig(recipeConfig)
				  .setOutputCount(3)
				  .setShape("###", "###", " I ")
				  .addMaterial('#', planks)
				  .addMaterial('I', Items.STICK)
				  .setGroup("end_signs")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_chest", chest)
				  .checkConfig(recipeConfig)
				  .setShape("###", "# #", "###")
				  .addMaterial('#', planks)
				  .setGroup("end_chests")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_barrel", barrel)
				  .checkConfig(recipeConfig)
				  .setShape("#S#", "# #", "#S#")
				  .addMaterial('#', planks)
				  .addMaterial('S', slab)
				  .setGroup("end_barrels")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_bookshelf", shelf)
				  .checkConfig(recipeConfig)
				  .setShape("###", "PPP", "###")
				  .addMaterial('#', planks)
				  .addMaterial('P', Items.BOOK)
				  .setGroup("end_BLOCK_BOOKSHELVES")
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_bark", bark)
				  .checkConfig(recipeConfig)
				  .setShape("##", "##")
				  .addMaterial('#', log)
				  .setOutputCount(3)
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_log", log)
				  .checkConfig(recipeConfig)
				  .setShape("##", "##")
				  .addMaterial('#', bark)
				  .setOutputCount(3)
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_composter", composter)
				  .checkConfig(recipeConfig)
				  .setShape("# #", "# #", "###")
				  .addMaterial('#', slab)
				  .build();
		GridRecipe.make(getModID(), getBaseName() + "_shulker", Items.SHULKER_BOX)
				  .checkConfig(recipeConfig)
				  .setShape("S", "#", "S")
				  .addMaterial('S', Items.SHULKER_SHELL)
				  .addMaterial('#', chest)
				  .build();
	}
}