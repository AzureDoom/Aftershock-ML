package mod.azure.aftershock.client.renderer;

import mod.azure.azurelib.common.render.block.AzBlockEntityRenderer;
import mod.azure.azurelib.common.render.block.AzBlockEntityRendererConfig;
import net.minecraft.resources.ResourceLocation;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.blocks.be.SeismographBlockEntity;
import mod.azure.aftershock.client.animator.SeismographBlockAnimator;

public class SeismographBlockRenderer extends AzBlockEntityRenderer<SeismographBlockEntity> {

    private static final ResourceLocation MODEL = CommonMod.modResource("geo/block/seismograph/seismograph.geo.json");

    private static final ResourceLocation TEXTURE = CommonMod.modResource("textures/block/seismograph/seismograph.png");

    public SeismographBlockRenderer() {
        super(
            AzBlockEntityRendererConfig.<SeismographBlockEntity>builder(MODEL, TEXTURE)
                .setAnimatorProvider(SeismographBlockAnimator::new)
                .build()
        );
    }
}
