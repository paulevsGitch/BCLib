package ru.bclib.blocks.complex;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.NotNull;
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
import ru.bclib.config.Configs;
import ru.bclib.recipes.GridRecipe;

import java.util.function.BiFunction;

public class WoodenMaterial {
    public final static String NAME_STRIPPED_LOG = "stripped_log";
    public final static String NAME_STRIPPED_BARK = "stripped_bark";
    public final static String NAME_LOG = "log";
    public final static String NAME_BARK = "bark";
    public final static String NAME_PLANKS = "planks";
    public final static String NAME_STAIRS = "stairs";
    public final static String NAME_SLAB = "slab";
    public final static String NAME_FENCE = "fence";
    public final static String NAME_GATE = "gate";
    public final static String NAME_BUTTON = "button";
    public final static String NAME_PLATE = "plate";
    public final static String NAME_TRAPDOOR = "trapdoor";
    public final static String NAME_DOOR = "door";
    public final static String NAME_CRAFTING_TABLE = "crafting_table";
    public final static String NAME_LADDER = "ladder";
    public final static String NAME_SIGN = "sign";
    public final static String NAME_CHEST = "chest";
    public final static String NAME_BARREL = "barrel";
    public final static String NAME_BOOKSHELF = "bookshelf";
    public final static String NAME_COMPOSTER = "composter";

    public final static String NAME_PRESSURE_PLATE = "pressure_plate";
    public final static String NAME_SHULKER = "shulker";

    public final Block log;
    public final Block bark;

    public final Block log_stripped;
    public final Block bark_stripped;

    public final Block planks;

    public final Block stairs;
    public final Block slab;
    public final Block fence;
    public final Block gate;
    public final Block button;
    public final Block pressurePlate;
    public final Block trapdoor;
    public final Block door;

    public final Block craftingTable;
    public final Block ladder;
    public final Block sign;

    public final Block chest;
    public final Block barrel;
    //public final Block shelf;
    //public final Block composter;

    protected final FabricBlockSettings materialPlanks;
    protected final String modID;
    protected final String name;
    protected final String receipGroupPrefix;

    public final Tag.Named<Block> logBlockTag;
    public final Tag.Named<Item> logItemTag;

    protected Block newLogStripped(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseRotatedPillarBlock(materialPlanks); }
    protected Block newBarkStripped(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseBarkBlock(materialPlanks); }
    protected Block newLog(FabricBlockSettings materialPlanks, MaterialColor woodColor) { if (log_stripped!=null) return new BaseStripableLogBlock(woodColor, log_stripped); else return null;}
    protected Block newBark(FabricBlockSettings materialPlanks, MaterialColor woodColor) { if (bark_stripped!=null) return new StripableBarkBlock(woodColor, bark_stripped); else return null;}
    protected Block newPlanks(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseBlock(materialPlanks); }
    protected Block newStairs(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseStairsBlock(planks); }
    protected Block newSlab(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseSlabBlock(planks); }
    protected Block newFence(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseFenceBlock(planks); }
    protected Block newGate(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseGateBlock(planks); }
    protected Block newButton(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseWoodenButtonBlock(planks); }
    protected Block newPressurePlate(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new WoodenPressurePlateBlock(planks); }
    protected Block newTrapdoor(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseTrapdoorBlock(planks); }
    protected Block newDoor(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseDoorBlock(planks); }
    protected Block newCraftingTable(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseCraftingTableBlock(planks); }
    protected Block newLadder(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseLadderBlock(planks); }
    protected Block newSign(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseSignBlock(planks); }
    protected Block newChest(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseChestBlock(planks); }
    protected Block newBarrel(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseBarrelBlock(planks); }
    //protected Block newShelf(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseBookshelfBlock(planks); }
    //protected Block newComposter(FabricBlockSettings materialPlanks, MaterialColor woodColor) { return new BaseComposterBlock(planks); }

    public WoodenMaterial(String modID, String name, MaterialColor woodColor, MaterialColor planksColor, String receipGroupPrefix, BiFunction<String, Block, Block> registerBlock) {
        materialPlanks = FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).mapColor(planksColor);
        this.modID = modID;
        this.receipGroupPrefix = receipGroupPrefix;
        this.name = name;

        log_stripped = registerBlock.apply(name + "_" + NAME_STRIPPED_LOG, newLogStripped(materialPlanks, woodColor));
        bark_stripped = registerBlock.apply(name + "_" + NAME_STRIPPED_BARK, newBarkStripped(materialPlanks, woodColor));

        log = registerBlock.apply(name + "_" + NAME_LOG, newLog(materialPlanks, woodColor));
        bark = registerBlock.apply(name + "_" + NAME_BARK, newBark(materialPlanks, woodColor));

        planks = registerBlock.apply(name + "_" + NAME_PLANKS, newPlanks(materialPlanks, woodColor));
        stairs = registerBlock.apply(name + "_" + NAME_STAIRS, newStairs(materialPlanks, woodColor));
        slab = registerBlock.apply(name + "_" + NAME_SLAB, newSlab(materialPlanks, woodColor));
        fence = registerBlock.apply(name + "_" + NAME_FENCE, newFence(materialPlanks, woodColor));
        gate = registerBlock.apply(name + "_" + NAME_GATE, newGate(materialPlanks, woodColor));
        button = registerBlock.apply(name + "_" + NAME_BUTTON, newButton(materialPlanks, woodColor));
        pressurePlate = registerBlock.apply(name + "_" + NAME_PLATE, newPressurePlate(materialPlanks, woodColor));
        trapdoor = registerBlock.apply(name + "_" + NAME_TRAPDOOR, newTrapdoor(materialPlanks, woodColor));
        door = registerBlock.apply(name + "_" + NAME_DOOR, newDoor(materialPlanks, woodColor));

        craftingTable = registerBlock.apply(name + "_" + NAME_CRAFTING_TABLE, newCraftingTable(materialPlanks, woodColor));
        ladder = registerBlock.apply(name + "_" + NAME_LADDER, newLadder(materialPlanks, woodColor));
        sign = registerBlock.apply(name + "_" + NAME_SIGN, newSign(materialPlanks, woodColor));

        chest = registerBlock.apply(name + "_" + NAME_CHEST, newChest(materialPlanks, woodColor));
        barrel = registerBlock.apply(name + "_" + NAME_BARREL, newBarrel(materialPlanks, woodColor));
        //shelf = registerBlock.apply(name + "_" + NAME_BOOKSHELF, newShelf(materialPlanks, woodColor));
        //composter = registerBlock.apply(name + "_" + NAME_COMPOSTER, newComposter(materialPlanks, woodColor));

        // Recipes //
        GridRecipe.make(modID, name + "_" + NAME_PLANKS, planks).checkConfig(Configs.RECIPE_CONFIG).setOutputCount(4).setList("#").addMaterial('#', log, bark, log_stripped, bark_stripped).setGroup(receipGroupPrefix + "_planks").build();
        GridRecipe.make(modID, name + "_" + NAME_STAIRS, stairs).checkConfig(Configs.RECIPE_CONFIG).setOutputCount(4).setShape("#  ", "## ", "###").addMaterial('#', planks).setGroup(receipGroupPrefix + "_planks_stairs").build();
        GridRecipe.make(modID, name + "_" + NAME_SLAB, slab).checkConfig(Configs.RECIPE_CONFIG).setOutputCount(6).setShape("###").addMaterial('#', planks).setGroup(receipGroupPrefix + "_planks_slabs").build();
        GridRecipe.make(modID, name + "_" + NAME_FENCE, fence).checkConfig(Configs.RECIPE_CONFIG).setOutputCount(3).setShape("#I#", "#I#").addMaterial('#', planks).addMaterial('I', Items.STICK).setGroup(receipGroupPrefix + "_planks_fences").build();
        GridRecipe.make(modID, name + "_" + NAME_GATE, gate).checkConfig(Configs.RECIPE_CONFIG).setShape("I#I", "I#I").addMaterial('#', planks).addMaterial('I', Items.STICK).setGroup(receipGroupPrefix + "_planks_gates").build();
        GridRecipe.make(modID, name + "_" + NAME_BUTTON, button).checkConfig(Configs.RECIPE_CONFIG).setList("#").addMaterial('#', planks).setGroup(receipGroupPrefix + "_planks_buttons").build();
        GridRecipe.make(modID, name + "_" + NAME_PRESSURE_PLATE, pressurePlate).checkConfig(Configs.RECIPE_CONFIG).setShape("##").addMaterial('#', planks).setGroup(receipGroupPrefix + "_planks_plates").build();
        GridRecipe.make(modID, name + "_" + NAME_TRAPDOOR, trapdoor).checkConfig(Configs.RECIPE_CONFIG).setOutputCount(2).setShape("###", "###").addMaterial('#', planks).setGroup(receipGroupPrefix + "_trapdoors").build();
        GridRecipe.make(modID, name + "_" + NAME_DOOR, door).checkConfig(Configs.RECIPE_CONFIG).setOutputCount(3).setShape("##", "##", "##").addMaterial('#', planks).setGroup(receipGroupPrefix + "_doors").build();
        GridRecipe.make(modID, name + "_" + NAME_CRAFTING_TABLE, craftingTable).checkConfig(Configs.RECIPE_CONFIG).setShape("##", "##").addMaterial('#', planks).setGroup(receipGroupPrefix + "_tables").build();
        GridRecipe.make(modID, name + "_" + NAME_LADDER, ladder).checkConfig(Configs.RECIPE_CONFIG).setOutputCount(3).setShape("I I", "I#I", "I I").addMaterial('#', planks).addMaterial('I', Items.STICK).setGroup(receipGroupPrefix + "_ladders").build();
        GridRecipe.make(modID, name + "_" + NAME_SIGN, sign).checkConfig(Configs.RECIPE_CONFIG).setOutputCount(3).setShape("###", "###", " I ").addMaterial('#', planks).addMaterial('I', Items.STICK).setGroup(receipGroupPrefix + "_signs").build();
        GridRecipe.make(modID, name + "_" + NAME_CHEST, chest).checkConfig(Configs.RECIPE_CONFIG).setShape("###", "# #", "###").addMaterial('#', planks).setGroup(receipGroupPrefix + "_chests").build();
        GridRecipe.make(modID, name + "_" + NAME_BARREL, barrel).checkConfig(Configs.RECIPE_CONFIG).setShape("#S#", "# #", "#S#").addMaterial('#', planks).addMaterial('S', slab).setGroup(receipGroupPrefix + "_barrels").build();
        //GridRecipe.make(modID, name + "_" + NAME_BOOKSHELF, shelf).checkConfig(Configs.RECIPE_CONFIG).setShape("###", "PPP", "###").addMaterial('#', planks).addMaterial('P', Items.BOOK).setGroup(receipGroupPrefix + "_bookshelves").build();
        GridRecipe.make(modID, name + "_" + NAME_BARK, bark).checkConfig(Configs.RECIPE_CONFIG).setShape("##", "##").addMaterial('#', log).setOutputCount(3).build();
        GridRecipe.make(modID, name + "_" + NAME_LOG, log).checkConfig(Configs.RECIPE_CONFIG).setShape("##", "##").addMaterial('#', bark).setOutputCount(3).build();
        //GridRecipe.make(modID, name + "_" + NAME_COMPOSTER, composter).checkConfig(Configs.RECIPE_CONFIG).setShape("# #", "# #", "###").addMaterial('#', slab).build();
        GridRecipe.make(modID, name + "_" + NAME_SHULKER, Items.SHULKER_BOX).checkConfig(Configs.RECIPE_CONFIG).setShape("S", "#", "S").addMaterial('S', Items.SHULKER_SHELL).addMaterial('#', chest).build();

        // Item Tags //
        TagAPI.addTag(ItemTags.PLANKS, planks);
        TagAPI.addTag(ItemTags.WOODEN_PRESSURE_PLATES, pressurePlate);
        TagAPI.addTag(ItemTags.LOGS, log, bark, log_stripped, bark_stripped);
        TagAPI.addTag(ItemTags.LOGS_THAT_BURN, log, bark, log_stripped, bark_stripped);


        TagAPI.addTags(button, ItemTags.WOODEN_BUTTONS, ItemTags.BUTTONS);
        TagAPI.addTags(door, ItemTags.WOODEN_DOORS, ItemTags.DOORS);
        TagAPI.addTags(fence, ItemTags.WOODEN_FENCES, ItemTags.FENCES);
        TagAPI.addTags(slab, ItemTags.WOODEN_SLABS, ItemTags.SLABS);
        TagAPI.addTags(stairs, ItemTags.WOODEN_STAIRS, ItemTags.STAIRS);
        TagAPI.addTags(trapdoor, ItemTags.WOODEN_TRAPDOORS, ItemTags.TRAPDOORS);
        TagAPI.addTag(TagAPI.ITEM_CHEST, chest);

        // Block Tags //
        TagAPI.addTag(BlockTags.PLANKS, planks);
        TagAPI.addTag(BlockTags.CLIMBABLE, ladder);
        TagAPI.addTag(BlockTags.LOGS, log, bark, log_stripped, bark_stripped);
        TagAPI.addTag(BlockTags.LOGS_THAT_BURN, log, bark, log_stripped, bark_stripped);


        TagAPI.addTags(button, BlockTags.WOODEN_BUTTONS, BlockTags.BUTTONS);
        TagAPI.addTags(door, BlockTags.WOODEN_DOORS, BlockTags.DOORS);
        TagAPI.addTags(fence, BlockTags.WOODEN_FENCES, BlockTags.FENCES);
        TagAPI.addTags(slab, BlockTags.WOODEN_SLABS, BlockTags.SLABS);
        TagAPI.addTags(stairs, BlockTags.WOODEN_STAIRS, BlockTags.STAIRS);
        TagAPI.addTags(trapdoor, BlockTags.WOODEN_TRAPDOORS, BlockTags.TRAPDOORS);
        //TagAPI.addTag(TagAPI.BLOCK_BOOKSHELVES, shelf);
        TagAPI.addTag(TagAPI.BLOCK_CHEST, chest);

        logBlockTag = TagAPI.makeBlockTag(modID, name + "_logs");
        logItemTag = TagAPI.makeItemTag(modID, name + "_logs");
        TagAPI.addTag(logBlockTag, log_stripped, bark_stripped, log, bark);
        TagAPI.addTag(logItemTag, log_stripped, bark_stripped, log, bark);

        addFlammable();
    }

    protected void addFlammable() {
        FlammableBlockRegistry.getDefaultInstance().add(log, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(bark, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(log_stripped, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(bark_stripped, 5, 5);

        FlammableBlockRegistry.getDefaultInstance().add(planks, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(stairs, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(slab, 5, 20);

        FlammableBlockRegistry.getDefaultInstance().add(fence, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(gate, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(button, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(pressurePlate, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(trapdoor, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(door, 5, 20);

        FlammableBlockRegistry.getDefaultInstance().add(craftingTable, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(ladder, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(sign, 5, 20);

        FlammableBlockRegistry.getDefaultInstance().add(chest, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(barrel, 5, 20);
        //FlammableBlockRegistry.getDefaultInstance().add(shelf, 5, 20);
        //FlammableBlockRegistry.getDefaultInstance().add(composter, 5, 20);
    }

    public boolean isTreeLog(Block block) {
        return block!=null && (block == log || block == bark);
    }

    public boolean isTreeLog(BlockState state) {
        return isTreeLog(state.getBlock());
    }
}