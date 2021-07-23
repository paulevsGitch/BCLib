package ru.bclib.complexmaterials;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
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
	public void initTags() {
		addBlockTag(TagAPI.makeBlockTag(getModID(), getBaseName() + "_logs"));
		addItemTag(TagAPI.makeItemTag(getModID(), getBaseName() + "_logs"));
	}
	
	@Override
	public void initDefault(FabricBlockSettings blockSettings, FabricItemSettings itemSettings) {
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
		}).setBlockTags(BlockTags.PLANKS));
		
		final Block planks = getBlock("planks");
		addBlockEntry(new BlockEntry("stairs", (complexMaterial, settings) -> {
			return new BaseStairsBlock(planks);
		}));
		addBlockEntry(new BlockEntry("slab", (complexMaterial, settings) -> {
			return new BaseSlabBlock(planks);
		}));
		addBlockEntry(new BlockEntry("fence", (complexMaterial, settings) -> {
			return new BaseFenceBlock(planks);
		}));
		addBlockEntry(new BlockEntry("gate", (complexMaterial, settings) -> {
			return new BaseGateBlock(planks);
		}));
		addBlockEntry(new BlockEntry("button", (complexMaterial, settings) -> {
			return new BaseWoodenButtonBlock(planks);
		}));
		addBlockEntry(new BlockEntry("plate", (complexMaterial, settings) -> {
			return new WoodenPressurePlateBlock(planks);
		}));
		addBlockEntry(new BlockEntry("trapdoor", (complexMaterial, settings) -> {
			return new BaseTrapdoorBlock(planks);
		}));
		addBlockEntry(new BlockEntry("door", (complexMaterial, settings) -> {
			return new BaseDoorBlock(planks);
		}));
		
		addBlockEntry(new BlockEntry("crafting_table", (complexMaterial, settings) -> {
			return new BaseCraftingTableBlock(planks);
		}));
		addBlockEntry(new BlockEntry("ladder", (complexMaterial, settings) -> {
			return new BaseLadderBlock(planks);
		}).setBlockTags(BlockTags.CLIMBABLE));
		addBlockEntry(new BlockEntry("sign", (complexMaterial, settings) -> {
			return new BaseSignBlock(planks);
		}));
		
		addBlockEntry(new BlockEntry("chest", (complexMaterial, settings) -> {
			return new BaseChestBlock(planks);
		}));
		addBlockEntry(new BlockEntry("barrel", (complexMaterial, settings) -> {
			return new BaseBarrelBlock(planks);
		}));
		addBlockEntry(new BlockEntry("bookshelf", (complexMaterial, settings) -> {
			return new BaseBookshelfBlock(planks);
		}));
		addBlockEntry(new BlockEntry("composter", (complexMaterial, settings) -> {
			return new BaseComposterBlock(planks);
		}));
	}
	
	@Override
	public void initRecipes() {
	
	}
}