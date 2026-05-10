package mod.azure.aftershock.client.animator;

import mod.azure.azurelib.common.animation.AzAnimatorConfig;
import mod.azure.azurelib.common.animation.controller.AzAnimationController;
import mod.azure.azurelib.common.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.common.animation.controller.keyframe.AzKeyframeCallbacks;
import mod.azure.azurelib.common.animation.impl.AzEntityAnimator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.entity.dirtdragon.DirtDragonEntity;
import mod.azure.aftershock.registry.SoundRegistry;

public class DirtDragonAnimator extends AzEntityAnimator<DirtDragonEntity> {

    private static final ResourceLocation ANIMATIONS = CommonMod.modResource(
        "animations/entity/american_dirt_dragon/american_dirt_dragon.animation.json"
    );

    public DirtDragonAnimator() {
        super(AzAnimatorConfig.defaultConfig());
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<DirtDragonEntity> animationControllerContainer) {
        animationControllerContainer.add(
            AzAnimationController.builder(this, "base_controller")
                .setKeyframeCallbacks(
                    AzKeyframeCallbacks.<DirtDragonEntity>builder()
                        .setSoundKeyframeHandler(
                            event -> {
                                if (event.getKeyframeData().getSound().equals("attacking")) {
                                    event.getAnimatable()
                                        .level()
                                        .playLocalSound(
                                            event.getAnimatable().getX(),
                                            event.getAnimatable().getY(),
                                            event.getAnimatable().getZ(),
                                            SoundRegistry.DIRT_DRAGON_ATTACK.get(),
                                            SoundSource.HOSTILE,
                                            1F,
                                            1F,
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
    }

    @Override
    public @NotNull ResourceLocation getAnimationLocation(DirtDragonEntity dirtDragonEntity) {
        return ANIMATIONS;
    }
}
