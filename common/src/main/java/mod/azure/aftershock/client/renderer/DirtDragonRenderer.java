package mod.azure.aftershock.client.renderer;

import mod.azure.azurelib.common.render.entity.AzEntityRenderer;
import mod.azure.azurelib.common.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.client.animator.DirtDragonAnimator;
import mod.azure.aftershock.entity.dirtdragon.DirtDragonEntity;

public class DirtDragonRenderer extends AzEntityRenderer<DirtDragonEntity> {

    private static final ResourceLocation MODEL = CommonMod.modResource(
        "geo/entity/american_dirt_dragon/american_dirt_dragon.geo.json"
    );

    private static final ResourceLocation TEXTURE = CommonMod.modResource(
        "textures/entity/american_dirt_dragon/american_dirt_dragon.png"
    );

    private static final ResourceLocation ALBINO = CommonMod.modResource(
        "textures/entity/american_dirt_dragon/american_dirt_dragon_albino.png"
    );

    public DirtDragonRenderer(EntityRendererProvider.Context context) {
        super(
            AzEntityRendererConfig.<DirtDragonEntity>builder($ -> MODEL, entity -> entity.isAlbino() ? ALBINO : TEXTURE)
                .setRenderEntry(contextPipeline -> {
                    contextPipeline.animatable().updateAnimations();

                    return contextPipeline;
                })
                .setAnimatorProvider(DirtDragonAnimator::new)
                .setScale(DirtDragonRenderer::getDirtDragonScale)
                .setDeathMaxRotation(0F)
                .setShadowRadius(dirtDragonEntity -> 0.5F * getDirtDragonScale(dirtDragonEntity))
                .build(),
            context
        );
    }

    private static float getDirtDragonScale(DirtDragonEntity entity) {
        var scaleFactor = 0.3F + ((entity.getGrowth() / 1200.0F) / 2.0F);
        return entity.getGrowth() >= 1200.0F ? 1.0F : scaleFactor;
    }
}
