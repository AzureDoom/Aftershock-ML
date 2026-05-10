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
import mod.azure.aftershock.entity.shrieker.ShriekerEntity;
import mod.azure.aftershock.registry.SoundRegistry;

public class ShriekerAnimator extends AzEntityAnimator<ShriekerEntity> {

    private static final ResourceLocation ANIMATIONS = CommonMod.modResource(
        "animations/entity/american_shrieker/american_shrieker.animation.json"
    );

    public ShriekerAnimator() {
        super(AzAnimatorConfig.defaultConfig());
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<ShriekerEntity> animationControllerContainer) {
        animationControllerContainer.add(
            AzAnimationController.builder(this, "base_controller")
                .setKeyframeCallbacks(
                    AzKeyframeCallbacks.<ShriekerEntity>builder()
                        .setSoundKeyframeHandler(
                            event -> {
                                if (event.getKeyframeData().getSound().equals("puking")) {
                                    event.getAnimatable()
                                        .level()
                                        .playLocalSound(
                                            event.getAnimatable().getX(),
                                            event.getAnimatable().getY(),
                                            event.getAnimatable().getZ(),
                                            SoundEvents.ALLAY_HURT,
                                            SoundSource.HOSTILE,
                                            0.75F,
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
                                            SoundRegistry.SHRIEKER_SEARCH.get(),
                                            SoundSource.HOSTILE,
                                            1.00F,
                                            1.0F,
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
                                            SoundRegistry.SHRIEKER_HURT.get(),
                                            SoundSource.HOSTILE,
                                            1.00F,
                                            1.0F,
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
                                            1.25F,
                                            0.3F,
                                            true
                                        );
                                }
                            }
                        )
                        .build()
                )
                .build()
        );
        animationControllerContainer.add(
            AzAnimationController.builder(this, "eyes_controller").build()
        );
    }

    @Override
    public @NotNull ResourceLocation getAnimationLocation(ShriekerEntity shriekerEntity) {
        return ANIMATIONS;
    }
}
