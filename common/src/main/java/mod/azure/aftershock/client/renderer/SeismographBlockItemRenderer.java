package mod.azure.aftershock.client.renderer;

import mod.azure.azurelib.common.render.item.AzItemRenderer;
import mod.azure.azurelib.common.render.item.AzItemRendererConfig;
import net.minecraft.resources.ResourceLocation;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.client.animator.SeismographBlockItemAnimator;

public class SeismographBlockItemRenderer extends AzItemRenderer {

    private static final ResourceLocation MODEL = CommonMod.modResource("geo/block/seismograph/seismograph.geo.json");

    private static final ResourceLocation TEXTURE = CommonMod.modResource("textures/block/seismograph/seismograph.png");

    public SeismographBlockItemRenderer() {
        super(
            AzItemRendererConfig.builder(MODEL, TEXTURE)
                .setAnimatorProvider(SeismographBlockItemAnimator::new)
                .build()
        );
    }
}
