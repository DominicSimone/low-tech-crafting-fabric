package name.lowtechcrafting;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class AutoCraftingItem extends BlockItem {

    public AutoCraftingItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public Text getName() {
        return Text.translatable("block.minecraft.crafting_table");
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("Autocrafting").formatted(Formatting.BLUE));
    }

    @Override
    public String getTranslationKey() {
        return "block.minecraft.crafting_table";
    }
}
