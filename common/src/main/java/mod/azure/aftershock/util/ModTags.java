package mod.azure.aftershock.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import mod.azure.aftershock.CommonMod;

public class ModTags {

    public static final TagKey<Block> GRABOID_BURROWABLE = TagKey.create(
        Registries.BLOCK,
        CommonMod.modResource("graboid_burrowable")
    );

    public static final TagKey<Block> HEAT_BLOCKS = TagKey.create(
        Registries.BLOCK,
        CommonMod.modResource("heat_blocks")
    );

    public static final TagKey<EntityType<?>> HEAT_ENTITY = TagKey.create(
        Registries.ENTITY_TYPE,
        CommonMod.modResource("heat_entity")
    );

    public static final TagKey<EntityType<?>> UNDERGROUND_ENTITY = TagKey.create(
        Registries.ENTITY_TYPE,
        CommonMod.modResource("underground_entity")
    );

    public static final TagKey<EntityType<?>> DANGER_ENTITIES = TagKey.create(
        Registries.ENTITY_TYPE,
        CommonMod.modResource("danger_entities")
    );

    public static final TagKey<Biome> SPAWN_UNDERGROUND_MOBS = TagKey.create(
        Registries.BIOME,
        CommonMod.modResource("spawns_underground_mobs")
    );
}
