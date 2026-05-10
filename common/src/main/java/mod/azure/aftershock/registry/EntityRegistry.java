package mod.azure.aftershock.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

import mod.azure.aftershock.blocks.be.SeismographBlockEntity;
import mod.azure.aftershock.entity.blaster.BlasterEntity;
import mod.azure.aftershock.entity.dirtdragon.DirtDragonEntity;
import mod.azure.aftershock.entity.graboid.GraboidEntity;
import mod.azure.aftershock.entity.shrieker.ShriekerEntity;
import mod.azure.aftershock.services.AftershockServices;
import mod.azure.aftershock.util.SilencedEntityTypeBuilder;

public class EntityRegistry {

    private EntityRegistry() {}

    public static final Supplier<BlockEntityType<SeismographBlockEntity>> SEIMOGRAPH = registerBlockEntity(
        "seismograph",
        () -> BlockEntityType.Builder.of(
            SeismographBlockEntity::new,
            BlockRegistry.SEIMOGRAPH.get()
        ).build(null)
    );

    public static final Supplier<EntityType<GraboidEntity>> GRABOID = registerEntity(
        "graboid",
        GraboidEntity::new,
        2.25F,
        2.0F
    );

    public static final Supplier<EntityType<DirtDragonEntity>> DIRT_DRAGON = registerEntity(
        "dirt_dragon",
        DirtDragonEntity::new,
        0.9F,
        0.3F
    );

    public static final Supplier<EntityType<ShriekerEntity>> SHRIEKER = registerEntity(
        "shrieker",
        ShriekerEntity::new,
        0.9F,
        0.9F
    );

    public static final Supplier<EntityType<BlasterEntity>> BLASTER = registerEntity(
        "blaster",
        BlasterEntity::new,
        1.3F,
        1.15F
    );

    static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
        String blockEntityName,
        Supplier<BlockEntityType<T>> blockEntity
    ) {
        return AftershockServices.COMMON_REGISTRY.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            blockEntityName,
            blockEntity
        );
    }

    static <T extends Entity> Supplier<EntityType<T>> registerEntity(
        String entityName,
        EntityType.EntityFactory<T> entity,
        float width,
        float height
    ) {
        return AftershockServices.COMMON_REGISTRY.register(
            BuiltInRegistries.ENTITY_TYPE,
            entityName,
            () -> create(entity, width, height).buildWithoutDataFixerCheck()
        );
    }

    static <T extends Entity> SilencedEntityTypeBuilder create(
        EntityType.EntityFactory<T> entity,
        float width,
        float height
    ) {
        return (SilencedEntityTypeBuilder) EntityType.Builder.of(entity, MobCategory.MONSTER).sized(width, height);
    }

    public static void initialize() {}
}
