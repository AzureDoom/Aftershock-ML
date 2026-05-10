package mod.azure.aftershock.client.renderer;

import mod.azure.azurelib.common.render.entity.AzEntityRenderer;
import mod.azure.azurelib.common.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.client.animator.BlasterAnimator;
import mod.azure.aftershock.entity.blaster.BlasterEntity;

public class BlasterRenderer extends AzEntityRenderer<BlasterEntity> {

    private static final ResourceLocation MODEL = CommonMod.modResource(
        "geo/entity/american_blaster/american_blaster.geo.json"
    );

    private static final ResourceLocation TEXTURE = CommonMod.modResource(
        "textures/entity/american_blaster/american_blaster.png"
    );

    public BlasterRenderer(EntityRendererProvider.Context context) {
        super(
            AzEntityRendererConfig.<BlasterEntity>builder(MODEL, TEXTURE)
                .setRenderEntry(contextPipeline -> {
                    contextPipeline.animatable().updateAnimations();

                    return contextPipeline;
                })
                .setAnimatorProvider(BlasterAnimator::new)
                .setDeathMaxRotation(0F)
                .setShadowRadius(0.5F)
                .setRenderType(RenderType.entityTranslucent(TEXTURE))
                .build(),
            context
        );
    }
}
