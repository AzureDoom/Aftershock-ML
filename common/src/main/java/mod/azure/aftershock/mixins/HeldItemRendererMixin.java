package mod.azure.aftershock.mixins;

import mod.azure.aftershock.items.PortableSeismographItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.aftershock.items.ShotgunItem;

@Mixin(value = ItemInHandRenderer.class)
public class HeldItemRendererMixin {

    @Mutable
    @Shadow
    @Final
    private final Minecraft minecraft;

    @Shadow
    private float mainHandHeight;

    @Shadow
    private float offHandHeight;

    @Shadow
    private ItemStack mainHandItem;

    @Shadow
    private ItemStack offHandItem;

    protected HeldItemRendererMixin(Minecraft client) {
        this.minecraft = client;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void aftershock$cancelAnimation(CallbackInfo ci) {
        final var clientPlayerEntity = minecraft.player;
        assert clientPlayerEntity != null;
        final var itemStack = clientPlayerEntity.getMainHandItem();
        final var itemStack2 = clientPlayerEntity.getOffhandItem();
        if ((mainHandItem.getItem() instanceof ShotgunItem || mainHandItem.getItem() instanceof PortableSeismographItem) && ItemStack.isSameItem(mainHandItem, itemStack)) {
            mainHandHeight = 1;
            mainHandItem = itemStack;
        }
        if ((offHandItem.getItem() instanceof ShotgunItem || offHandItem.getItem() instanceof PortableSeismographItem) && ItemStack.isSameItem(offHandItem, itemStack2)) {
            offHandHeight = 1;
            offHandItem = itemStack2;
        }
    }
}
