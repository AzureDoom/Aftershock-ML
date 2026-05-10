package mod.azure.aftershock.client.renderer;

import mod.azure.azurelib.common.render.entity.AzEntityRenderer;
import mod.azure.azurelib.common.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.client.animator.ShriekerAnimator;
import mod.azure.aftershock.entity.shrieker.ShriekerEntity;

public class ShriekerRenderer extends AzEntityRenderer<ShriekerEntity> {

    private static final ResourceLocation MODEL = CommonMod.modResource(
        "geo/entity/american_shrieker/american_shrieker.geo.json"
    );

    private static final ResourceLocation TEXTURE = CommonMod.modResource(
        "textures/entity/american_shrieker/american_shrieker.png"
    );

    private static final ResourceLocation MOLT = CommonMod.modResource(
        "textures/entity/american_shrieker/american_shrieker_molt.png"
    );

    public ShriekerRenderer(EntityRendererProvider.Context context) {
        super(
            AzEntityRendererConfig.builder($ -> MODEL, ShriekerRenderer::getTextures)
                .setRenderEntry(contextPipeline -> {
                    contextPipeline.animatable().updateAnimations();

                    return contextPipeline;
                })
                .setAnimatorProvider(ShriekerAnimator::new)
                .setScale(ShriekerRenderer::getShriekerScale)
                .setDeathMaxRotation(0F)
                .setShadowRadius(dirtDragonEntity -> 0.5F * getShriekerScale(dirtDragonEntity))
                .build(),
            context
        );
    }

    private static ResourceLocation getTextures(ShriekerEntity entity) {
        if (entity.getGrowth() > 47000f)
            return MOLT;
        return TEXTURE;
    }

    private static float getShriekerScale(ShriekerEntity entity) {
        var scaleFactor = 0.5F + ((entity.getGrowth() / 1200.0F) / 2.0F);
        return entity.getGrowth() >= 1200.0F ? 1.35F : scaleFactor;
    }
}
