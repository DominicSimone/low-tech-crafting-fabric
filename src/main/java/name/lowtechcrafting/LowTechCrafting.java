package name.lowtechcrafting;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class LowTechCrafting implements ModInitializer {

    public static final String MOD_ID = "lowtechcrafting";

    public static final Identifier BOX = new Identifier(MOD_ID, "box_block");
    public static final Block BOX_BLOCK;
    public static final BlockItem BOX_BLOCK_ITEM;
    public static final BlockEntityType<BoxBlockEntity> BOX_BLOCK_ENTITY;
    public static final ScreenHandlerType<BoxScreenHandler> BOX_SCREEN_HANDLER;

    public static final Identifier AUTOCRAFTING_ID = new Identifier(MOD_ID, "autocrafting_table");
    public static final Block AUTOCRAFTING_TABLE_BLOCK;
    public static final BlockItem AUTOCRAFTING_TABLE_ITEM;
    public static final BlockEntityType<AutoCraftingTableEntity> AUTOCRAFTING_TABLE_ENTITY;
    public static final ScreenHandlerType<AutoCraftingScreenHandler> AUTOCRAFTING_TABLE_SCREEN_HANDLER;

    static {
        BOX_BLOCK = Registry.register(Registries.BLOCK, BOX, new BoxBlock(FabricBlockSettings.copyOf(Blocks.CHEST)));
        BOX_BLOCK_ITEM = Registry.register(Registries.ITEM, BOX, new BlockItem(BOX_BLOCK, new FabricItemSettings()));
        BOX_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, BOX,
                FabricBlockEntityTypeBuilder.create(BoxBlockEntity::new, BOX_BLOCK).build());
        BOX_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(BOX, BoxScreenHandler::new);

        AUTOCRAFTING_TABLE_BLOCK = Registry.register(Registries.BLOCK, AUTOCRAFTING_ID, new AutoCraftingTable(FabricBlockSettings.copyOf(Blocks.CRAFTING_TABLE)));
        AUTOCRAFTING_TABLE_ITEM = Registry.register(Registries.ITEM, AUTOCRAFTING_ID, new BlockItem(AUTOCRAFTING_TABLE_BLOCK, new FabricItemSettings()));
        AUTOCRAFTING_TABLE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, AUTOCRAFTING_ID,
                FabricBlockEntityTypeBuilder.create(AutoCraftingTableEntity::new, AUTOCRAFTING_TABLE_BLOCK).build());
        AUTOCRAFTING_TABLE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(AUTOCRAFTING_ID, AutoCraftingScreenHandler::new);
    }

    @Override
    public void onInitialize() {
        
    }
}