package name.lowtechcrafting;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AutoCraftingTable extends BlockWithEntity {

    public AutoCraftingTable(Settings settings)
    {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AutoCraftingTableEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public boolean isTileEntityValid(BlockEntity te)
    {
        return te != null && te.isRemoved() == false;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            //This will call the createScreenHandlerFactory method from BlockWithEntity, which will return our blockEntity casted to
            //a namedScreenHandlerFactory. If your block class does not extend BlockWithEntity, it needs to implement createScreenHandlerFactory.
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if (screenHandlerFactory != null) {
                //With this call the server will request the client to open the appropriate Screenhandler
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }


    @Override
    @Deprecated
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AutoCraftingTableEntity) {
                ItemScatterer.spawn(world, pos, (AutoCraftingTableEntity)blockEntity);
                // update comparators
                world.updateComparators(pos,this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    // @Override
    // public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    // {
    //     BlockEntity te = world.getBlockEntity(pos);

    //     if (te instanceof BlockEntityCrafting tec)
    //     {
    //         CompoundTag nbt = stack.getTag();

    //         // If the ItemStack has a tag containing saved TE data, restore it to the just placed block/TE
    //         if (nbt != null && nbt.contains("BlockEntityTag", Tag.TAG_COMPOUND))
    //         {
    //             tec.readFromNBTCustom(nbt.getCompound("BlockEntityTag"));
    //         }
    //         else
    //         {
    //             if (stack.hasCustomHoverName())
    //             {
    //                 tec.setInventoryName(stack.getHoverName().getString());
    //             }
    //         }
    //     }
    // }

    // @Override
    // @Deprecated
    // public boolean hasAnalogOutputSignal(BlockState state)
    // {
    //     return true;
    // }

    // @Override
    // public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
    // {
    //     BlockEntity te = world.getBlockEntity(pos);

    //     if (te != null && this.isTileEntityValid(te))
    //     {
    //         LazyOptional<IItemHandler> optional = te.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH);

    //         if (optional.isPresent())
    //         {
    //             return calcRedstoneFromInventory(optional.orElse(null));
    //         }
    //     }

    //     return 0;
    // }

    // public static int calcRedstoneFromInventory(@Nullable IItemHandler inv)
    // {
    //     if (inv != null)
    //     {
    //         final int numSlots = inv.getSlots();

    //         if (numSlots > 0)
    //         {
    //             int nonEmptyStacks = 0;

    //             // Ignore the output slot, start from slot 1
    //             for (int slot = 1; slot < numSlots; ++slot)
    //             {
    //                 ItemStack stack = inv.getStackInSlot(slot);

    //                 if (stack.isEmpty() == false)
    //                 {
    //                     ++nonEmptyStacks;
    //                 }
    //             }

    //             return (nonEmptyStacks * 15) / 9;
    //         }
    //     }

    //     return 0;
    // }
}
