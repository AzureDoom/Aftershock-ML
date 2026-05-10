package mod.azure.aftershock.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

import mod.azure.aftershock.blocks.GraboidEggBlock;
import mod.azure.aftershock.blocks.SeismographBlock;
import mod.azure.aftershock.items.GraboidEggBlockItem;
import mod.azure.aftershock.items.SeismographBlockItem;
import mod.azure.aftershock.services.AftershockServices;

public class BlockRegistry {

    private BlockRegistry() {}

    public static final Supplier<SeismographBlock> SEIMOGRAPH = registerBlock(
        "seismograph",
        () -> new SeismographBlock(
            BlockBehaviour.Properties.of()
        )
    );

    public static final Supplier<BlockItem> SEIMOGRAPH_ITEM = ItemRegistry.registerItem(
        "seismograph",
        () -> new SeismographBlockItem(SEIMOGRAPH.get())
    );

    public static final Supplier<GraboidEggBlock> GRABOID_EGG = registerBlock(
        "graboid_egg",
        GraboidEggBlock::new
    );

    public static final Supplier<BlockItem> GRABOID_EGG_ITEM = ItemRegistry.registerItem(
        "graboid_egg",
        () -> new GraboidEggBlockItem(GRABOID_EGG.get())
    );

    static <T extends Block> Supplier<T> registerBlock(String blockName, Supplier<T> block) {
        return AftershockServices.COMMON_REGISTRY.register(BuiltInRegistries.BLOCK, blockName, block);
    }

    public static void initialize() {}
}
