package mod.azure.aftershock.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GraboidEggBlockItem extends BlockItem {

    public GraboidEggBlockItem(Block block) {
        super(block, new Item.Properties());
    }

    @Override
    public void appendHoverText(
        @NotNull ItemStack stack,
        @NotNull TooltipContext context,
        List<Component> tooltipComponents,
        @NotNull TooltipFlag tooltipFlag
    ) {
        tooltipComponents.add(
            Component.translatable("aftershock.egg.text")
                .withStyle(ChatFormatting.YELLOW)
                .withStyle(ChatFormatting.ITALIC)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

}
