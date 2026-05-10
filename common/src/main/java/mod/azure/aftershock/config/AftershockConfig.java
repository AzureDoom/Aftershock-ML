package mod.azure.aftershock.config;

import mod.azure.azurelib.common.config.Config;
import mod.azure.azurelib.common.config.Configurable;

import mod.azure.aftershock.CommonMod;

@Config(id = CommonMod.MOD_ID)
public class AftershockConfig {

    @Configurable
    public boolean debugPathingParticlesEnabled = false;

    @Configurable
    @Configurable.Synchronized
    public EntityConfigs entityConfigs = new EntityConfigs();

    public static class EntityConfigs {

        @Configurable
        @Configurable.Synchronized
        public float dirt_dragon_health = 28F;

        @Configurable
        @Configurable.Synchronized
        public int dirt_dragon_exp = 6;

        @Configurable
        @Configurable.Synchronized
        public float dirt_dragon_damage = 2F;

        @Configurable
        @Configurable.Synchronized
        public float graboid_health = 80F;

        @Configurable
        @Configurable.Synchronized
        public int graboid_exp = 25;

        @Configurable
        @Configurable.Synchronized
        public float graboid_damage = 10F;

        @Configurable
        @Configurable.Synchronized
        public float graboid_armor = 6F;

        @Configurable
        @Configurable.Synchronized
        public float shrieker_health = 20F;

        @Configurable
        @Configurable.Synchronized
        public int shrieker_exp = 8;

        @Configurable
        @Configurable.Synchronized
        public float shrieker_damage = 4F;

        @Configurable
        @Configurable.Synchronized
        public float blaster_health = 5F;

        @Configurable
        @Configurable.Synchronized
        public int blaster_exp = 10;

        @Configurable
        @Configurable.Synchronized
        public float blaster_damage = 5F;
    }

    @Configurable
    @Configurable.Synchronized
    public ItemConfigs itemConfigs = new ItemConfigs();

    public static class ItemConfigs {

        @Configurable
        @Configurable.Synchronized
        public float shotgun_damage = 5F;
    }
}
