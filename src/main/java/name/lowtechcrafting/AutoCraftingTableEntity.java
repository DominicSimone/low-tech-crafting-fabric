package name.lowtechcrafting;

import java.util.List;
import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class AutoCraftingTableEntity extends BlockEntity implements RecipeInputInventory, NamedScreenHandlerFactory {

    private AutoCraftingInventory recipeInventory = new AutoCraftingInventory(null, 3, 3);
    private DefaultedList<ItemStack> outputBufferInventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private AutoCraftingScreenHandler screen;

    public AutoCraftingTableEntity(BlockPos pos, BlockState state) {
        super(LowTechCrafting.AUTOCRAFTING_TABLE_ENTITY, pos, state);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        screen = new AutoCraftingScreenHandler(syncId, playerInventory, this,
                ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()));
        return screen;
    }

    public ItemStack tryCraftItem() {
        Optional<CraftingRecipe> recipe = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING,
                recipeInventory, world);
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack itemStack2;
        if (recipe.isPresent() && (itemStack2 = recipe.get().craft(recipeInventory, world.getRegistryManager()))
                .isItemEnabled(world.getEnabledFeatures())) {
            itemStack = itemStack2;
        }
        return itemStack;
    }

    // Called from CraftingResultSlot in normal Crafting workflow
    public void removeIngredients() {
        DefaultedList<ItemStack> defaultedList = world.getServer().getRecipeManager()
                .getRemainingStacks(RecipeType.CRAFTING, recipeInventory, this.world);
        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack itemStack = recipeInventory.getStack(i);
            ItemStack itemStack2 = defaultedList.get(i);
            if (!itemStack.isEmpty()) {
                recipeInventory.removeStack(i, 1);
                itemStack = recipeInventory.getStack(i);
            }
            if (itemStack2.isEmpty())
                continue;
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

    public void doCraft() {
        if (outputBufferInventory.get(0).isEmpty()) {
            ItemStack item = tryCraftItem();
            outputBufferInventory.set(0, item);
            removeIngredients();
            markDirty();
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, recipeInventory.stacks);

        ItemStack outputItems = outputBufferInventory.get(0);
        if (!outputItems.isEmpty()) {
            NbtList nbtList = (NbtList) nbt.get("Items");
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putByte("Slot", (byte)10);
            outputItems.writeNbt(nbtCompound);
            nbtList.add(nbtCompound);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        Inventories.readNbt(nbt, recipeInventory.stacks);
        NbtList nbtList = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
        NbtCompound nbtCompound = nbtList.getCompound(nbtList.size() - 1);
        if (nbtCompound.getByte("Slot") == (byte) 10) {
            outputBufferInventory.set(0, ItemStack.fromNbt(nbtCompound));
        }
    }

    @Override
    public void clear() {
        recipeInventory.clear();
        outputBufferInventory.clear();
        markDirty();
    }

    @Override
    public int size() {
        return 10;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            if (!recipeInventory.getStack(i).isEmpty() && !outputBufferInventory.get(0).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack removeStack(int slot) {
        markDirty();
        if (slot == 0) {
            doCraft();
            return Inventories.removeStack(outputBufferInventory, slot);
        } else {
            ItemStack result = Inventories.removeStack(recipeInventory.stacks, slot - 1);
            updateScreen();
            return result;
        }
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        ItemStack result;
        if (slot == 0) {
            doCraft();
            result = Inventories.splitStack(outputBufferInventory, slot, count);
        } else {
            result = Inventories.splitStack(recipeInventory.stacks, slot - 1, count);
        }
        if (!result.isEmpty()) {
            updateScreen();
            markDirty();
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (stack.getCount() > stack.getMaxCount()) {
            stack.setCount(stack.getMaxCount());
        }
        if (slot == 0) {
            outputBufferInventory.set(slot, stack);
            updateScreen();
        } else {
            recipeInventory.setStack(slot - 1, stack);
            updateScreen();
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        if (outputBufferInventory.get(0).isEmpty() && tryCraftItem().isEmpty()) {
            return slot > 0;
        } else {
            return slot == 0;
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return slot > 0 && this.recipeInventory.getStack(slot - 1).getCount() == 0;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= this.size()) {
            return ItemStack.EMPTY;
        }
        if (slot == 0) {
            if (outputBufferInventory.get(slot).isEmpty()) {
                return tryCraftItem();
            }
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

    public void syncFromScreen(RecipeInputInventory input) {
        if (input.size() > 8) {
            for (int i = 0; i < 9; i++) {
                recipeInventory.setStack(i, input.getStack(i));
            }
        }
        markDirty();
    }

    public Inventory getDroppableStacks() {
        SimpleInventory inv = new SimpleInventory(10);
        for (int i = 0; i < 9; i++) {
            if (!recipeInventory.getStack(i).isEmpty()) {
                inv.addStack(recipeInventory.getStack(i));
            }
        }
        if (!outputBufferInventory.get(0).isEmpty()) {
            inv.addStack(outputBufferInventory.get(0));
        }
        return inv;
    }

    public int calcRedstoneFromInventory() {
        int nonEmptyStacks = 0;
        for (int i = 0; i < 9; i++) {
            if (!recipeInventory.getStack(i).isEmpty()) {
                nonEmptyStacks++;
            }
        }
        if (!outputBufferInventory.get(0).isEmpty()) {
            nonEmptyStacks++;
        }
        return nonEmptyStacks;
    }

    public void updateScreen() {
        if (screen != null) {
            screen.syncFromPersistentInv();
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.minecraft.crafting_table");
    }

}
