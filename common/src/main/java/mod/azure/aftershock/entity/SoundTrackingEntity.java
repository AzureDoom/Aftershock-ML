package mod.azure.aftershock.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

import mod.azure.aftershock.util.Growable;

public abstract class SoundTrackingEntity extends PathfinderMob implements Growable {

    public SoundTrackingEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }
}
