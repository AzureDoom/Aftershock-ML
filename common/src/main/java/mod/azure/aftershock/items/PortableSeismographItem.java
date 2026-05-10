package mod.azure.aftershock.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import mod.azure.aftershock.entity.SoundTrackingEntity;

public class PortableSeismographItem extends Item {

    protected int soundcounter;

    public PortableSeismographItem() {
        super(new Item.Properties().durability(600));
    }

    @Override
    public void appendHoverText(
        @NotNull ItemStack stack,
        @NotNull TooltipContext context,
        List<Component> tooltipComponents,
        @NotNull TooltipFlag tooltipFlag
    ) {
        tooltipComponents.add(
            Component.translatable("aftershock.portable.text")
                .withStyle(ChatFormatting.YELLOW)
                .withStyle(ChatFormatting.ITALIC)
        );
        tooltipComponents.add(
            Component.translatable("aftershock.portable.text2")
                .withStyle(ChatFormatting.WHITE)
                .withStyle(ChatFormatting.ITALIC)
        );
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack itemStack, ItemStack itemStack2) {
        return itemStack2.is(Items.IRON_INGOT);
    }

    @Override
    public void inventoryTick(
        @NotNull ItemStack itemStack,
        @NotNull Level level,
        @NotNull Entity entity,
        int slot,
        boolean selected
    ) {
        if (entity instanceof Player player)
            if (
                (player.getOffhandItem().is(this) || selected) && itemStack.getDamageValue() < itemStack.getMaxDamage()
                    - 1
            )
                entity.level()
                    .getEntitiesOfClass(
                        SoundTrackingEntity.class,
                        new AABB(entity.blockPosition()).inflate(15D, 15D, 15D)
                    )
                    .forEach(e -> {
                        if (e.walkAnimation.speed() >= 0.15F) {
                            this.soundcounter++;
                            if (this.soundcounter >= 5) {
                                if (entity.level().isClientSide)
                                    entity.level()
                                        .playSound(
                                            entity,
                                            entity.blockPosition(),
                                            SoundEvents.NOTE_BLOCK_BELL.value(),
                                            SoundSource.BLOCKS,
                                            1.0f,
                                            3.3f
                                        );
                                this.soundcounter = 0;
                                itemStack.setDamageValue(itemStack.getDamageValue() + 1);
                            }
                        } else
                            this.soundcounter = 0;
                    });
            else
                this.soundcounter = 0;
        super.inventoryTick(itemStack, level, entity, slot, selected);
    }
}
