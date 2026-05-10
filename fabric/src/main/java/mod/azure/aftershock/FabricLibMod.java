package mod.azure.aftershock;

import mod.azure.azurelib.common.animation.cache.AzIdentityRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;

import mod.azure.aftershock.entity.blaster.BlasterEntity;
import mod.azure.aftershock.entity.dirtdragon.DirtDragonEntity;
import mod.azure.aftershock.entity.graboid.GraboidEntity;
import mod.azure.aftershock.entity.shrieker.ShriekerEntity;
import mod.azure.aftershock.registry.BlockRegistry;
import mod.azure.aftershock.registry.EntityRegistry;
import mod.azure.aftershock.registry.ItemRegistry;
import mod.azure.aftershock.util.ModTags;
import mod.azure.aftershock.util.SpawnUtils;

public final class FabricLibMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonMod.initRegistries();
        AzIdentityRegistry.register(BlockRegistry.SEIMOGRAPH_ITEM.get(), ItemRegistry.EIGHT_GAUGE.get());
        FabricDefaultAttributeRegistry.register(
            EntityRegistry.DIRT_DRAGON.get(),
            DirtDragonEntity.createMobAttributes()
        );
        FabricDefaultAttributeRegistry.register(
            EntityRegistry.GRABOID.get(),
            GraboidEntity.createMobAttributes()
        );
        FabricDefaultAttributeRegistry.register(
            EntityRegistry.SHRIEKER.get(),
            ShriekerEntity.createMobAttributes()
        );
        FabricDefaultAttributeRegistry.register(
            EntityRegistry.BLASTER.get(),
            BlasterEntity.createMobAttributes()
        );
        SpawnPlacements.register(
            EntityRegistry.DIRT_DRAGON.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> SpawnUtils.isDesertSpawn(world, pos)
        );
        SpawnPlacements.register(
            EntityRegistry.GRABOID.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> SpawnUtils.isDesertSpawn(world, pos)
        );
        SpawnPlacements.register(
            EntityRegistry.SHRIEKER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> SpawnUtils.isDesertSpawn(world, pos)
        );
        SpawnPlacements.register(
            EntityRegistry.BLASTER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> SpawnUtils.isDesertSpawn(world, pos)
        );
        BiomeModifications.addSpawn(
            BiomeSelectors.tag(ModTags.SPAWN_UNDERGROUND_MOBS),
            MobCategory.MONSTER,
            EntityRegistry.DIRT_DRAGON.get(),
            20,
            1,
            4
        );
        BiomeModifications.addSpawn(
            BiomeSelectors.tag(ModTags.SPAWN_UNDERGROUND_MOBS),
            MobCategory.MONSTER,
            EntityRegistry.GRABOID.get(),
            5,
            1,
            1
        );
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            entries.accept(ItemRegistry.DIRT_DRAGON_SPAWN_EGG.get());
            entries.accept(ItemRegistry.GRABOID_SPAWN_EGG.get());
            entries.accept(ItemRegistry.SHRIEKER_SPAWN_EGG.get());
            entries.accept(ItemRegistry.BLASTER_SPAWN_EGG.get());
            entries.accept(BlockRegistry.GRABOID_EGG_ITEM.get());
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .register(entries -> entries.accept(BlockRegistry.SEIMOGRAPH_ITEM.get()));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.accept(ItemRegistry.SHOTGUN_SHELL.get());
            entries.accept(ItemRegistry.EIGHT_GAUGE.get());
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .register(entries -> entries.accept(ItemRegistry.PORTABLE_SEIMOGRAPH_ITEM.get()));
    }
}
