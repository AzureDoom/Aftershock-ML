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
import mod.azure.aftershock.entity.blaster.BlasterEntity;

public class BlasterAnimator extends AzEntityAnimator<BlasterEntity> {

    private static final ResourceLocation ANIMATIONS = CommonMod.modResource(
        "animations/entity/american_blaster/american_blaster.animation.json"
    );

    public BlasterAnimator() {
        super(AzAnimatorConfig.defaultConfig());
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<BlasterEntity> animationControllerContainer) {
        animationControllerContainer.add(
            AzAnimationController.builder(this, "base_controller")
                .setKeyframeCallbacks(
                    AzKeyframeCallbacks.<BlasterEntity>builder()
                        .setSoundKeyframeHandler(
                            event -> {
                                if (event.getKeyframeData().getSound().equals("takeoff")) {
                                    event.getAnimatable()
                                        .level()
                                        .playLocalSound(
                                            event.getAnimatable().getX(),
                                            event.getAnimatable().getY(),
                                            event.getAnimatable().getZ(),
                                            SoundEvents.CAMPFIRE_CRACKLE,
                                            SoundSource.HOSTILE,
                                            1F,
                                            1F,
                                            true
                                        );
                                }
                                if (event.getKeyframeData().getSound().equals("screaming")) {
                                    event.getAnimatable()
                                        .level()
                                        .playLocalSound(
                                            event.getAnimatable().getX(),
                                            event.getAnimatable().getY(),
                                            event.getAnimatable().getZ(),
                                            SoundEvents.HUSK_HURT,
                                            SoundSource.HOSTILE,
                                            1.00F,
                                            0.5F,
                                            true
                                        );
                                }
                                if (event.getKeyframeData().getSound().equals("dying")) {
                                    event.getAnimatable()
                                        .level()
                                        .playLocalSound(
                                            event.getAnimatable().getX(),
                                            event.getAnimatable().getY(),
                                            event.getAnimatable().getZ(),
                                            SoundEvents.LLAMA_DEATH,
                                            SoundSource.HOSTILE,
                                            1.00F,
                                            0.1F,
                                            true
                                        );
                                }
                                if (event.getKeyframeData().getSound().equals("looking")) {
                                    event.getAnimatable()
                                        .level()
                                        .playLocalSound(
                                            event.getAnimatable().getX(),
                                            event.getAnimatable().getY(),
                                            event.getAnimatable().getZ(),
                                            SoundEvents.CHICKEN_AMBIENT,
                                            SoundSource.HOSTILE,
                                            1.0F,
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
    public @NotNull ResourceLocation getAnimationLocation(BlasterEntity blasterEntity) {
        return ANIMATIONS;
    }
}
