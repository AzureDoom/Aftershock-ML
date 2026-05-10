package mod.azure.aftershock;

import mod.azure.azurelib.common.render.item.AzItemRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import mod.azure.aftershock.client.renderer.*;
import mod.azure.aftershock.registry.BlockRegistry;
import mod.azure.aftershock.registry.EntityRegistry;
import mod.azure.aftershock.registry.ItemRegistry;

@EventBusSubscriber(modid = CommonMod.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientMod {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        AzItemRendererRegistry.register(BlockRegistry.SEIMOGRAPH_ITEM.get(), SeismographBlockItemRenderer::new);
        AzItemRendererRegistry.register(ItemRegistry.EIGHT_GAUGE.get(), EightGaugeRenderer::new);
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegistry.DIRT_DRAGON.get(), DirtDragonRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GRABOID.get(), GraboidRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SHRIEKER.get(), ShriekerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.BLASTER.get(), BlasterRenderer::new);
        event.registerBlockEntityRenderer(
            EntityRegistry.SEIMOGRAPH.get(),
            (BlockEntityRendererProvider.Context rendererDispatcherIn) -> new SeismographBlockRenderer()
        );
    }
}
