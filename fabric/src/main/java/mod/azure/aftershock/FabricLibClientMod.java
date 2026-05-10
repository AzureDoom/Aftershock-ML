package mod.azure.aftershock;

import mod.azure.azurelib.common.render.item.AzItemRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

import mod.azure.aftershock.client.renderer.*;
import mod.azure.aftershock.registry.BlockRegistry;
import mod.azure.aftershock.registry.EntityRegistry;
import mod.azure.aftershock.registry.ItemRegistry;

public class FabricLibClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(EntityRegistry.DIRT_DRAGON.get(), DirtDragonRenderer::new);
        EntityRendererRegistry.register(EntityRegistry.GRABOID.get(), GraboidRenderer::new);
        EntityRendererRegistry.register(EntityRegistry.SHRIEKER.get(), ShriekerRenderer::new);
        EntityRendererRegistry.register(EntityRegistry.BLASTER.get(), BlasterRenderer::new);
        BlockEntityRenderers.register(
            EntityRegistry.SEIMOGRAPH.get(),
            (BlockEntityRendererProvider.Context rendererDispatcherIn) -> new SeismographBlockRenderer()
        );
        AzItemRendererRegistry.register(BlockRegistry.SEIMOGRAPH_ITEM.get(), SeismographBlockItemRenderer::new);
        AzItemRendererRegistry.register(ItemRegistry.EIGHT_GAUGE.get(), EightGaugeRenderer::new);
    }
}
