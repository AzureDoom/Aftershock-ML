package mod.azure.aftershock.client.animator;

import mod.azure.azurelib.common.animation.AzAnimatorConfig;
import mod.azure.azurelib.common.animation.controller.AzAnimationController;
import mod.azure.azurelib.common.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.common.animation.impl.AzBlockAnimator;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.blocks.be.SeismographBlockEntity;

public class SeismographBlockAnimator extends AzBlockAnimator<SeismographBlockEntity> {

    private static final ResourceLocation ANIMATIONS = CommonMod.modResource(
        "animations/block/seismograph/seismograph.animation.json"
    );

    public SeismographBlockAnimator() {
        super(AzAnimatorConfig.defaultConfig());
    }

    @Override
    public void registerControllers(
        AzAnimationControllerContainer<SeismographBlockEntity> animationControllerContainer
    ) {
        animationControllerContainer.add(
            AzAnimationController.builder(this, "base_controller")
                .build()
        );
    }

    @Override
    public @NotNull ResourceLocation getAnimationLocation(SeismographBlockEntity animatable) {
        return ANIMATIONS;
    }
}
