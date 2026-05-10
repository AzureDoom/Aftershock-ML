package mod.azure.aftershock;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.azure.azurelib.common.animation.cache.AzIdentityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import mod.azure.aftershock.entity.blaster.BlasterEntity;
import mod.azure.aftershock.entity.dirtdragon.DirtDragonEntity;
import mod.azure.aftershock.entity.graboid.GraboidEntity;
import mod.azure.aftershock.entity.shrieker.ShriekerEntity;
import mod.azure.aftershock.registry.BlockRegistry;
import mod.azure.aftershock.registry.EntityRegistry;
import mod.azure.aftershock.registry.ItemRegistry;
import mod.azure.aftershock.util.SpawnUtils;

@Mod(CommonMod.MOD_ID)
public final class NeoForgeMod {

    public static DeferredRegister<EntityType<?>> entityTypeDeferredRegister = DeferredRegister.create(
        BuiltInRegistries.ENTITY_TYPE,
        CommonMod.MOD_ID
    );

    public static DeferredRegister<Item> itemDeferredRegister = DeferredRegister.create(
        BuiltInRegistries.ITEM,
        CommonMod.MOD_ID
    );

    public static DeferredRegister<Block> blockDeferredRegister = DeferredRegister.create(
        BuiltInRegistries.BLOCK,
        CommonMod.MOD_ID
    );

    public static DeferredRegister<BlockEntityType<?>> blockEntityTypeDeferredRegister = DeferredRegister.create(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        CommonMod.MOD_ID
    );

    public static DeferredRegister<SoundEvent> soundEventDeferredRegister = DeferredRegister.create(
        BuiltInRegistries.SOUND_EVENT,
        CommonMod.MOD_ID
    );

    public NeoForgeMod(IEventBus modEventBus) {
        CommonMod.initRegistries();
        entityTypeDeferredRegister.register(modEventBus);
        itemDeferredRegister.register(modEventBus);
        blockDeferredRegister.register(modEventBus);
        blockEntityTypeDeferredRegister.register(modEventBus);
        soundEventDeferredRegister.register(modEventBus);
        modEventBus.addListener(this::init);
        modEventBus.addListener(this::createEntityAttributes);
        modEventBus.addListener(this::addCreativeTabs);
        ModEntitySpawn.SERIALIZER.register(modEventBus);
    }

    public void init(final FMLCommonSetupEvent event) {
        ;
        AzIdentityRegistry.register(BlockRegistry.SEIMOGRAPH_ITEM.get(), ItemRegistry.EIGHT_GAUGE.get());
    }

    public void createEntityAttributes(final EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.DIRT_DRAGON.get(), DirtDragonEntity.createMobAttributes().build());
        event.put(EntityRegistry.GRABOID.get(), GraboidEntity.createMobAttributes().build());
        event.put(EntityRegistry.SHRIEKER.get(), ShriekerEntity.createMobAttributes().build());
        event.put(EntityRegistry.BLASTER.get(), BlasterEntity.createMobAttributes().build());
    }

    public void addSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
            EntityRegistry.DIRT_DRAGON.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> SpawnUtils.isDesertSpawn(world, pos),
            RegisterSpawnPlacementsEvent.Operation.AND
        );
        event.register(
            EntityRegistry.GRABOID.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> SpawnUtils.isDesertSpawn(world, pos),
            RegisterSpawnPlacementsEvent.Operation.AND
        );
        event.register(
            EntityRegistry.SHRIEKER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> SpawnUtils.isDesertSpawn(world, pos),
            RegisterSpawnPlacementsEvent.Operation.AND
        );
        event.register(
            EntityRegistry.BLASTER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> SpawnUtils.isDesertSpawn(world, pos),
            RegisterSpawnPlacementsEvent.Operation.AND
        );
    }

    public void addCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ItemRegistry.DIRT_DRAGON_SPAWN_EGG.get());
            event.accept(ItemRegistry.GRABOID_SPAWN_EGG.get());
            event.accept(ItemRegistry.SHRIEKER_SPAWN_EGG.get());
            event.accept(ItemRegistry.BLASTER_SPAWN_EGG.get());
            event.accept(BlockRegistry.GRABOID_EGG_ITEM.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ItemRegistry.PORTABLE_SEIMOGRAPH_ITEM.get());
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ItemRegistry.SHOTGUN_SHELL.get());
            event.accept(ItemRegistry.EIGHT_GAUGE.get());
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(BlockRegistry.SEIMOGRAPH_ITEM.get());
        }
    }

    record ModEntitySpawn(
        HolderSet<Biome> biomes,
        MobSpawnSettings.SpawnerData spawn
    ) implements BiomeModifier {

        public static DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZER = DeferredRegister.create(
            NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,
            CommonMod.MOD_ID
        );

        static Supplier<MapCodec<ModEntitySpawn>> SPAWN_CODEC = SERIALIZER.register(
            "mobspawns",
            () -> RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(ModEntitySpawn::biomes),
                    MobSpawnSettings.SpawnerData.CODEC.fieldOf("spawn")
                        .forGetter(
                            ModEntitySpawn::spawn
                        )
                ).apply(builder, ModEntitySpawn::new)
            )
        );

        @Override
        public void modify(
            @NotNull Holder<Biome> biome,
            @NotNull Phase phase,
            ModifiableBiomeInfo.BiomeInfo.@NotNull Builder builder
        ) {
            if (phase == Phase.ADD && this.biomes.contains(biome)) {
                builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, this.spawn);
            }
        }

        @Override
        public @NotNull MapCodec<? extends BiomeModifier> codec() {
            return SPAWN_CODEC.get();
        }
    }
}
