package mod.azure.aftershock.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.aftershock.items.ShotgunItem;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    Minecraft minecraft;

    @Inject(
        method = "renderItemInHand", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
            shift = At.Shift.BEFORE
        )
    )
    private void aftershock$bobViewGun(Camera camera, float partialTick, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (
            this.minecraft.player.getUseItem().getItem() instanceof ShotgunItem && !this.minecraft.player
                .isSprinting()
        ) {
            aftershock$gunBobView(projectionMatrix, partialTick);
        }
    }

    @WrapWithCondition(
        method = "renderItemInHand", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"
        )
    )
    private boolean aftershock$vanillaBobView(GameRenderer instance, PoseStack matrices, float tickDelta) {
        return !(this.minecraft.player.getUseItem().getItem() instanceof ShotgunItem);
    }

    @Unique
    private void aftershock$gunBobView(Matrix4f projectionMatrix, float tickDelta) {
        if (!(this.minecraft.gameRenderer.getMainCamera().getEntity() instanceof Player playerEntity)) {
            return;
        }
        var f = (playerEntity.walkDist - playerEntity.walkDistO);
        var g = -(playerEntity.walkDist + f * tickDelta);
        var h = Mth.lerp(tickDelta, playerEntity.oBob, playerEntity.bob) * 0.25f;

        projectionMatrix.translate(
            Mth.sin(g * (float) Math.PI) * h * 0.5f,
            -Math.abs(Mth.cos(g * (float) Math.PI) * h),
            0
        );

        projectionMatrix.rotate(Axis.ZP.rotationDegrees(Mth.sin(g * (float) Math.PI) * h * 3.0f));
        projectionMatrix.rotate(Axis.XP.rotationDegrees(Math.abs(Mth.cos(g * (float) Math.PI - 0.2f) * h) * 5.0f));
    }
}
