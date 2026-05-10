package mod.azure.aftershock.client.renderer;

import mod.azure.azurelib.common.render.entity.AzEntityRenderer;
import mod.azure.azurelib.common.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.client.animator.GraboidAnimator;
import mod.azure.aftershock.client.model.GraboidModelRenderer;
import mod.azure.aftershock.entity.graboid.GraboidEntity;

public class GraboidRenderer extends AzEntityRenderer<GraboidEntity> {

    private static final ResourceLocation MODEL = CommonMod.modResource(
        "geo/entity/american_graboid/american_graboid.geo.json"
    );

    private static final ResourceLocation TEXTURE = CommonMod.modResource(
        "textures/entity/american_graboid/american_graboid.png"
    );

    private static final ResourceLocation ALBINO = CommonMod.modResource(
        "textures/entity/american_graboid/american_graboid_albino.png"
    );

    public GraboidRenderer(EntityRendererProvider.Context context) {
        super(
            AzEntityRendererConfig.<GraboidEntity>builder($ -> MODEL, entity -> entity.isAlbino() ? ALBINO : TEXTURE)
                .setRenderEntry(contextPipeline -> {
                    contextPipeline.animatable().updateAnimations();

                    return contextPipeline;
                })
                .setModelRenderer(GraboidModelRenderer::new)
                .setAnimatorProvider(GraboidAnimator::new)
                .setScale(graboidEntity -> {
                    var scaleFactor = 0.3f + ((graboidEntity.getGrowth() / 1200) / 2.0f);
                    return graboidEntity.getGrowth() > 1200 ? 1.1F : scaleFactor;
                })
                .setDeathMaxRotation(0F)
                .setShadowRadius(0.5F)
                .build(),
            context
        );
    }
}
