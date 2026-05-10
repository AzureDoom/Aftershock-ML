package mod.azure.aftershock.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.services.AftershockServices;

public class SoundRegistry {

    public static Supplier<SoundEvent> SHOTGUN = registerSound("aftershock.shotgun");

    public static Supplier<SoundEvent> SHOTGUN_RELOAD = registerSound("aftershock.shotgunreload");

    public static Supplier<SoundEvent> DIRT_DRAGON_ATTACK = registerSound("aftershock.dirtdragon_attack");

    public static Supplier<SoundEvent> DIRT_DRAGON_IDLE = registerSound("aftershock.dirtdragon_idle");

    public static Supplier<SoundEvent> GRABOID_ATTACKING = registerSound("aftershock.graboid_attacking");

    public static Supplier<SoundEvent> GRABOID_DYING = registerSound("aftershock.graboid_dying");

    public static Supplier<SoundEvent> GRABOID_IDLE = registerSound("aftershock.graboid_idle");

    public static Supplier<SoundEvent> GRABOID_MOVING = registerSound("aftershock.graboid_moving");

    public static Supplier<SoundEvent> SHRIEKER_HURT = registerSound("aftershock.shrieker_hurt");

    public static Supplier<SoundEvent> SHRIEKER_SEARCH = registerSound("aftershock.shrieker_search");

    static Supplier<SoundEvent> registerSound(String soundName) {
        return AftershockServices.COMMON_REGISTRY.register(
            BuiltInRegistries.SOUND_EVENT,
            soundName,
            () -> SoundEvent.createVariableRangeEvent(CommonMod.modResource(soundName))
        );
    }

    public static void initialize() {}
}
