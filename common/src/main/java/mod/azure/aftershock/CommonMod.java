package mod.azure.aftershock;

import com.mojang.serialization.Codec;
import mod.azure.azurelib.common.platform.Services;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

import mod.azure.aftershock.registry.BlockRegistry;
import mod.azure.aftershock.registry.EntityRegistry;
import mod.azure.aftershock.registry.ItemRegistry;
import mod.azure.aftershock.registry.SoundRegistry;

public class CommonMod {

    public static final Supplier<DataComponentType<Integer>> AMMO_COUNT = Services.PLATFORM
        .registerDataComponent(
            "ammo_count",
            builder -> builder.persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.INT)
        );

    public static final String MOD_ID = "aftershock";

    public static ResourceLocation modResource(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static void initRegistries() {
        BlockRegistry.initialize();
        EntityRegistry.initialize();
        SoundRegistry.initialize();
        ItemRegistry.initialize();
    }
}
