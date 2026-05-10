package mod.azure.aftershock.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface Growable {

    float getGrowth();

    void setGrowth(float growth);

    float getMaxGrowth();

    default void grow(Entity entity, float amount) {
        setGrowth(Math.min(getGrowth() + amount, getMaxGrowth()));
        if (getGrowth() >= getMaxGrowth())
            growUp(entity);
    }

    void growUp(Entity entity);

    LivingEntity growInto();

    default float getGrowthNeededUntilGrowUp() {
        return getMaxGrowth() - getGrowth();
    }

    default float getGrowthMultiplier() {
        return 1.0f;
    }
}
