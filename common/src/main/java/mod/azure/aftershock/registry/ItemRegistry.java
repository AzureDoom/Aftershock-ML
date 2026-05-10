package mod.azure.aftershock.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Supplier;

import mod.azure.aftershock.items.PortableSeismographItem;
import mod.azure.aftershock.items.ShotgunItem;
import mod.azure.aftershock.services.AftershockServices;

public class ItemRegistry {

    public static final Supplier<SpawnEggItem> GRABOID_SPAWN_EGG = registerItem(
        "graboid_spawn_egg",
        AftershockServices.COMMON_REGISTRY.makeSpawnEggFor(
            EntityRegistry.GRABOID,
            0x4d4d4d,
            0x7e6d50,
            new Item.Properties()
        )
    );

    public static final Supplier<SpawnEggItem> DIRT_DRAGON_SPAWN_EGG = registerItem(
        "dirt_dragon_spawn_egg",
        AftershockServices.COMMON_REGISTRY.makeSpawnEggFor(
            EntityRegistry.DIRT_DRAGON,
            0xf2e7b0,
            0x9e6025,
            new Item.Properties()
        )
    );

    public static final Supplier<SpawnEggItem> SHRIEKER_SPAWN_EGG = registerItem(
        "shrieker_spawn_egg",
        AftershockServices.COMMON_REGISTRY.makeSpawnEggFor(
            EntityRegistry.SHRIEKER,
            0x7c5c51,
            0xe7ded4,
            new Item.Properties()
        )
    );

    public static final Supplier<SpawnEggItem> BLASTER_SPAWN_EGG = registerItem(
        "blaster_spawn_egg",
        AftershockServices.COMMON_REGISTRY.makeSpawnEggFor(
            EntityRegistry.BLASTER,
            0x927452,
            0x213744,
            new Item.Properties()
        )
    );

    public static final Supplier<Item> PORTABLE_SEIMOGRAPH_ITEM = registerItem(
        "portable_seismograph",
        PortableSeismographItem::new
    );

    public static final Supplier<Item> EIGHT_GAUGE = registerItem(
        "eightgauge",
        ShotgunItem::new
    );

    public static final Supplier<Item> SHOTGUN_SHELL = registerItem(
        "shotgun_shell",
        () -> new Item(new Item.Properties().stacksTo(64))
    );

    static <T extends Item> Supplier<T> registerItem(String itemName, Supplier<T> item) {
        return AftershockServices.COMMON_REGISTRY.register(BuiltInRegistries.ITEM, itemName, item);
    }

    public static void initialize() {}
}
