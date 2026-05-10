package mod.azure.aftershock.client.renderer;

import mod.azure.azurelib.common.render.item.AzItemRenderer;
import mod.azure.azurelib.common.render.item.AzItemRendererConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.client.animator.EightGaugeAnimator;

public class EightGaugeRenderer extends AzItemRenderer {

    private static final ResourceLocation MODEL = CommonMod.modResource("geo/item/eightgauge/eightgauge.geo.json");

    private static final ResourceLocation TEXTURE = CommonMod.modResource("textures/item/eightgauge/eightgauge.png");

    public EightGaugeRenderer() {
        super(
            AzItemRendererConfig.builder(MODEL, TEXTURE)
                .setAnimatorProvider(EightGaugeAnimator::new)
                .disableAnimationInContexts(ItemDisplayContext.GUI)
                .build()
        );
    }
}
