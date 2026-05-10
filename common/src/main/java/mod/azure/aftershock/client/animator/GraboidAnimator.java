package mod.azure.aftershock.client.animator;

import mod.azure.azurelib.common.animation.AzAnimatorConfig;
import mod.azure.azurelib.common.animation.controller.AzAnimationController;
import mod.azure.azurelib.common.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.common.animation.controller.keyframe.AzKeyframeCallbacks;
import mod.azure.azurelib.common.animation.impl.AzEntityAnimator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.entity.graboid.GraboidEntity;

public class GraboidAnimator extends AzEntityAnimator<GraboidEntity> {

    private static final ResourceLocation ANIMATIONS = CommonMod.modResource(
        "animations/entity/american_graboid/american_graboid.animation.json"
    );

    public GraboidAnimator() {
        super(AzAnimatorConfig.defaultConfig());
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<GraboidEntity> animationControllerContainer) {
        animationControllerContainer.add(
            AzAnimationController.builder(this, "base_controller")
                .setKeyframeCallbacks(
                    AzKeyframeCallbacks.<GraboidEntity>builder()
                        .setSoundKeyframeHandler(
                            event -> {
                                if (event.getKeyframeData().getSound().equals("digging")) {
                                    event.getAnimatable()
                                        .level()
                                        .playLocalSound(
                                            event.getAnimatable().getX(),
                                            event.getAnimatable().getY(),
                                            event.getAnimatable().getZ(),
                                            SoundEvents.RESPAWN_ANCHOR_SET_SPAWN,
                                            SoundSource.HOSTILE,
                                            0.75F,
                                            0.1F,
                                            true
                                        );
                                }
                            }
                        )
                        .build()
                )
                .build()
        );
    }

    @Override
    public @NotNull ResourceLocation getAnimationLocation(GraboidEntity brainBug) {
        return ANIMATIONS;
    }
}
