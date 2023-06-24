package name.lowtechcrafting;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

public class AutoCraftingTableEntity extends BlockEntity implements Inventory, NamedScreenHandlerFactory {

    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(10, ItemStack.EMPTY);

    public AutoCraftingTableEntity(BlockPos pos, BlockState state) {
        super(LowTechCrafting.AUTOCRAFTING_TABLE_ENTITY, pos, state);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new AutoCraftingScreenHandler(syncId, playerInventory, this, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()));
    }
 
    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
    }
    

    @Override
    public void clear() {
        this.inventory.clear();
        // markDirty();
    }

    @Override
    public int size() {
        return 10;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            if (!inventory.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack removeStack(int slot) {
        // markDirty();
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        ItemStack result = Inventories.splitStack(this.inventory, slot, count);
        if (!result.isEmpty()) {
            // markDirty();
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        
        this.inventory.set(slot, stack);
        if (stack.getCount() > stack.getMaxCount()) {
            stack.setCount(stack.getMaxCount());
        }
        // markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        // TODO when hoppers want to take out
        return true;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return this.inventory.get(slot).getCount() == 0 && slot > 0;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot >= this.inventory.size()) {
            return ItemStack.EMPTY;
        }
        return this.inventory.get(slot);
    }



    //-0-------------------
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
