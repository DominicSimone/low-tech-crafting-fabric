package name.lowtechcrafting;

import java.util.List;
import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class AutoCraftingTableEntity extends BlockEntity implements RecipeInputInventory, NamedScreenHandlerFactory {

    private AutoCraftingInventory recipeInventory = new AutoCraftingInventory(null, 3, 3);
    private DefaultedList<ItemStack> outputBufferInventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private DefaultedList<ItemStack> recipeOutput = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public AutoCraftingTableEntity(BlockPos pos, BlockState state) {
        super(LowTechCrafting.AUTOCRAFTING_TABLE_ENTITY, pos, state);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new AutoCraftingScreenHandler(syncId, playerInventory, this,
                ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()));
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    public ItemStack tryCraftItem() {
        Optional<CraftingRecipe> recipe = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, recipeInventory, world);
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack itemStack2;
        if (recipe.isPresent() && (itemStack2 = recipe.get().craft(recipeInventory, world.getRegistryManager())).isItemEnabled(world.getEnabledFeatures())) {
            itemStack = itemStack2;
        }
        return itemStack;
    }

    // Called from CraftingResultSlot in normal Crafting workflow
    public void removeIngredients() {
        DefaultedList<ItemStack> defaultedList = world.getServer().getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, recipeInventory, this.world);
        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack itemStack = recipeInventory.getStack(i);
            ItemStack itemStack2 = defaultedList.get(i);
            if (!itemStack.isEmpty()) {
                recipeInventory.removeStack(i, 1);
                itemStack = recipeInventory.getStack(i);
            }
            if (itemStack2.isEmpty()) continue;
            if (itemStack.isEmpty()) {
                recipeInventory.setStack(i, itemStack2);
                continue;
            }
            if (ItemStack.canCombine(itemStack, itemStack2)) {
                itemStack2.increment(itemStack.getCount());
                recipeInventory.setStack(i, itemStack2);
                continue;
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, recipeInventory.stacks);
        Inventories.writeNbt(nbt, outputBufferInventory);
        Inventories.writeNbt(nbt, recipeOutput);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, recipeInventory.stacks);
        Inventories.readNbt(nbt, outputBufferInventory);
        Inventories.readNbt(nbt, recipeOutput);
    }

    @Override
    public void clear() {
        recipeInventory.clear();
        outputBufferInventory.clear();
        recipeOutput.clear();
        markDirty();
    }

    @Override
    public int size() {
        return 10;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            if (!recipeInventory.getStack(i).isEmpty() && !outputBufferInventory.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack removeStack(int slot) {
        markDirty();
        if (slot == 0) {
            return Inventories.removeStack(outputBufferInventory, slot);
        } else {
            return Inventories.removeStack(recipeInventory.stacks, slot-1);
        }
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        ItemStack result;
        if (slot == 0) {
            result = Inventories.splitStack(outputBufferInventory, slot, count);
        } else {
            result = Inventories.splitStack(recipeInventory.stacks, slot-1, count);
        }
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0){
            outputBufferInventory.set(slot, stack);
        } else {
            recipeInventory.setStack(slot-1, stack);
        }
        if (stack.getCount() > stack.getMaxCount()) {
            stack.setCount(stack.getMaxCount());
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        if (outputBufferInventory.get(0).isEmpty()) {
            return slot > 0;
        } else {
            return slot == 0;
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot > 0 && this.recipeInventory.getStack(slot-1).getCount() == 0;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= this.size()) {
            return ItemStack.EMPTY;
        }
        if (slot == 0) {
            return outputBufferInventory.get(slot);
        } else {
            return recipeInventory.getStack(slot - 1);
        }
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (int i = 1; i <= 9; i++) {
            finder.addUnenchantedInput(recipeInventory.getStack(i));
        }
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public List<ItemStack> getInputStacks() {
        return recipeInventory.stacks;
    }

    // -0-------------------
    // public static class ItemHandlerWrapperCrafterExternal implements IItemHandler
    // {
    // private final IItemHandler inventoryCrafter;

    // public ItemHandlerWrapperCrafterExternal(IItemHandler inventoryCrafter) {
    // this.inventoryCrafter = inventoryCrafter;
    // }

    // @Override
    // public int getSlots() {
    // return this.inventoryCrafter.getSlots();
    // }

    // @Override
    // public int getSlotLimit(int slot) {
    // return slot == 0 ? this.inventoryCrafter.getSlotLimit(slot) : 1;
    // }

    // @Override
    // public ItemStack getStackInSlot(int slot) {
    // return this.inventoryCrafter.getStackInSlot(slot);
    // }

    // @Override
    // public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    // if (this.isItemValid(slot, stack) == false) {
    // return stack;
    // }

    // // Trying to insert more than one item: only insert one of them
    // if (stack.getCount() > 1) {
    // ItemStack stackInsert = stack.copy();
    // stackInsert.setCount(1);
    // stackInsert = this.inventoryCrafter.insertItem(slot, stackInsert, simulate);

    // // Successfully inserted, return the original stack shrunk by one
    // if (stackInsert.isEmpty()) {
    // stack = stack.copy();
    // stack.decrement(1);
    // return stack;
    // }
    // // else: Could not insert, return the original stack
    // return stack;
    // }
    // // Only inserting one item, handle it directly
    // else {
    // return this.inventoryCrafter.insertItem(slot, stack, simulate);
    // }
    // }

    // @Override
    // public ItemStack extractItem(int slot, int amount, boolean simulate) {
    // return this.inventoryCrafter.extractItem(slot, amount, simulate);
    // }

    // @Override
    // public boolean isItemValid(int slot, ItemStack stack) {
    // return slot != 0 && this.inventoryCrafter.getStackInSlot(slot).isEmpty() !=
    // false;
    // }
    // }

}
