package mod.azure.aftershock.mixins;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import mod.azure.aftershock.items.ShotgunItem;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void swing(@NotNull InteractionHand hand) {
        if (!(this.getUseItem().getItem() instanceof ShotgunItem))
            super.swing(hand);
    }
}
