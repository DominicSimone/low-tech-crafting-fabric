package name.lowtechcrafting;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

public class AutoCraftingInventory
        implements RecipeInputInventory {

    public final DefaultedList<ItemStack> stacks;
    private final int width;
    private final int height;
    private final ScreenHandler handler;

    public AutoCraftingInventory(ScreenHandler handler, int width, int height) {
        this(handler, width, height, DefaultedList.ofSize(width * height, ItemStack.EMPTY));
    }

    public AutoCraftingInventory(ScreenHandler handler, int width, int height, DefaultedList<ItemStack> stacks) {
        this.stacks = stacks;
        this.handler = handler;
        this.width = width;
        this.height = height;
    }

    @Override
    public int size() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.stacks) {
            if (itemStack.isEmpty())
                continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= this.size()) {
            return ItemStack.EMPTY;
        }
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.stacks, slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.stacks, slot, amount);
        if (!itemStack.isEmpty()) {
            onContentChanged();
        }
        return itemStack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
        onContentChanged();
    }

    public void copyStacks(Inventory inv, int offset) {
        if (inv.size() >= size() + offset) {
            for (int i = 0; i < size(); i++) {
                this.stacks.set(i, inv.getStack(i + offset));
            }
        }
        ((AutoCraftingScreenHandler) this.handler).onContentSynced();
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public List<ItemStack> getInputStacks() {
        return List.copyOf(this.stacks);
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (ItemStack itemStack : this.stacks) {
            finder.addUnenchantedInput(itemStack);
        }
    }

    private void onContentChanged() {
        if (this.handler != null) {
            this.handler.onContentChanged(this);
        }
    }
}