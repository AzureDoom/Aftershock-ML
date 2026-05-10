package mod.azure.aftershock.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.azure.azurelib.common.render.AzLayerRenderer;
import mod.azure.azurelib.common.render.AzRendererPipeline;
import mod.azure.azurelib.common.render.entity.AzEntityModelRenderer;
import mod.azure.azurelib.common.render.entity.AzEntityRendererPipeline;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

import mod.azure.aftershock.entity.graboid.GraboidEntity;

public class GraboidModelRenderer<T extends LivingEntity> extends AzEntityModelRenderer<T> {

    public GraboidModelRenderer(
        AzRendererPipeline<UUID, T> entityRendererPipeline,
        AzLayerRenderer<UUID, T> layerRenderer
    ) {
        super((AzEntityRendererPipeline<T>) entityRendererPipeline, layerRenderer);
    }

    public GraboidModelRenderer(
        AzEntityRendererPipeline<T> entityRendererPipeline,
        AzLayerRenderer<UUID, T> layerRenderer
    ) {
        super(entityRendererPipeline, layerRenderer);
    }

    @Override
    protected void applyRotations(
        T animatable,
        PoseStack poseStack,
        float ageInTicks,
        float rotationYaw,
        float partialTick,
        float nativeScale
    ) {
        if (
            animatable instanceof GraboidEntity graboidEntity
                && graboidEntity.isBurrowing() && !graboidEntity.isNoAi()
        ) {
            applyMovingRotations(animatable, graboidEntity, poseStack, partialTick);
            return;
        }

        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
    }

    private static void applyMovingRotations(
        LivingEntity entity,
        GraboidEntity graboidEntity,
        PoseStack poseStack,
        float partialTick
    ) {
        var velocity = graboidEntity.getDeltaMovement();

        var horizontalSpeed = Math.sqrt(
            velocity.x * velocity.x + velocity.z * velocity.z
        );

        var currentYaw = Mth.rotLerp(
            partialTick,
            graboidEntity.yBodyRotO,
            graboidEntity.yBodyRot
        );

        var currentPitch = Mth.lerp(
            partialTick,
            graboidEntity.xRotO,
            graboidEntity.getXRot()
        );

        var targetYaw = currentYaw;
        var targetPitch = currentPitch;

        if (horizontalSpeed > 0.01D) {
            targetYaw = (float) (Math.atan2(velocity.z, velocity.x) * (180.0D / Math.PI)) - 90.0F;
        }

        if (horizontalSpeed > 0.01D || Math.abs(velocity.y) > 0.01D) {
            targetPitch = (float) (Math.atan2(velocity.y, horizontalSpeed) * (180.0D / Math.PI));
        }

        var smoothedYaw = Mth.rotLerp(0.12F, currentYaw, targetYaw);
        var smoothedPitch = Mth.lerp(-0.10F, currentPitch, targetPitch);

        smoothedPitch *= 1.05F;
        smoothedPitch = Mth.clamp(smoothedPitch, -180.0F, 180.0F);

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - smoothedYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-smoothedPitch));
    }
}
