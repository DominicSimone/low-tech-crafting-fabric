package com.lowtechcrafting;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class AutoCraftingScreenHandler
        extends AbstractRecipeScreenHandler<RecipeInputInventory> {
    private final RecipeInputInventory input = new AutoCraftingInventory(this, 3, 3);
    private final CraftingResultInventory result = new CraftingResultInventory();
    private final Inventory persistentInventory;
    private final ScreenHandlerContext context;
    private final PlayerEntity player;

    public AutoCraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(10), ScreenHandlerContext.EMPTY);

    }

    public AutoCraftingScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory,
            ScreenHandlerContext context) {
        super(LowTechCrafting.AUTOCRAFTING_TABLE_SCREEN_HANDLER, syncId);
        checkSize(inventory, 10);
        persistentInventory = inventory;
        persistentInventory.onOpen(playerInventory.player);

        int j;
        int i;
        this.context = context;
        this.player = playerInventory.player;

        this.addSlot(new CraftingResultSlot(playerInventory.player, this.input, this.result, 0, 124, 35));
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.input, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
        syncFromPersistentInv();
    }

    public void syncToPersistentInv() {
        if (persistentInventory instanceof AutoCraftingTableEntity) {
            ((AutoCraftingTableEntity) persistentInventory).syncFromScreen(input);
        }
    }

    public void syncFromPersistentInv() {
        ((AutoCraftingInventory) input).copyStacks(persistentInventory, 1);
    }

    protected static void updateResult(ScreenHandler handler, World world, PlayerEntity player,
            RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory) {
        ItemStack itemStack2;
        CraftingRecipe craftingRecipe;
        if (world.isClient) {
            return;
        }

        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
        ItemStack itemStack = ItemStack.EMPTY;
        Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING,
                craftingInventory, world);
        if (optional.isPresent()
                && resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe = optional.get())
                && (itemStack2 = craftingRecipe.craft(craftingInventory, world.getRegistryManager()))
                        .isItemEnabled(world.getEnabledFeatures())) {
            itemStack = itemStack2;
        }
        resultInventory.setStack(0, itemStack);
        handler.setPreviousTrackedSlot(0, itemStack);
        serverPlayerEntity.networkHandler
                .sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack));
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        this.context.run((world, pos) -> AutoCraftingScreenHandler.updateResult(this, world, this.player, this.input,
                this.result));

        syncToPersistentInv();
    }

    public void onContentSynced() {
        this.context.run((world, pos) -> AutoCraftingScreenHandler.updateResult(this, world, this.player, this.input,
                this.result));
    }

    @Override
    public void populateRecipeFinder(RecipeMatcher finder) {
        this.input.provideRecipeInputs(finder);
    }

    @Override
    public void clearCraftingSlots() {
        this.input.clear();
        this.result.clear();
        this.persistentInventory.clear();
    }

    @Override
    public boolean matches(Recipe<? super RecipeInputInventory> recipe) {
        return recipe.matches(this.input, this.player.getWorld());
    }

    @Override
    public void onClosed(PlayerEntity player) {
        syncToPersistentInv();
        super.onClosed(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = (Slot) this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot == 0) {
                this.context.run((world, pos) -> itemStack2.getItem().onCraft(itemStack2, (World) world, player));
                if (!this.insertItem(itemStack2, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
                slot2.onQuickTransfer(itemStack2, itemStack);
            } else if (slot >= 10 && slot < 46
                    ? !this.insertItem(itemStack2, 1, 10, false)
                            && (slot < 37 ? !this.insertItem(itemStack2, 37, 46, false)
                                    : !this.insertItem(itemStack2, 10, 37, false))
                    : !this.insertItem(itemStack2, 10, 46, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot2.onTakeItem(player, itemStack2);
            if (slot == 0) {
                player.dropItem(itemStack2, false);
            }
        }
        return itemStack;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.result && super.canInsertIntoSlot(stack, slot);
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 0;
    }

    @Override
    public int getCraftingWidth() {
        return this.input.getWidth();
    }

    @Override
    public int getCraftingHeight() {
        return this.input.getHeight();
    }

    @Override
    public int getCraftingSlotCount() {
        return 10;
    }

    @Override
    public RecipeBookCategory getCategory() {
        return RecipeBookCategory.CRAFTING;
    }

    @Override
    public boolean canInsertIntoSlot(int index) {
        return index != this.getCraftingResultSlotIndex();
    }
}
