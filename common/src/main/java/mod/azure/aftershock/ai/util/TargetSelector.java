package mod.azure.aftershock.ai.util;

import net.minecraft.world.entity.LivingEntity;

import mod.azure.aftershock.ai.core.Blackboard;

@FunctionalInterface
public interface TargetSelector<E> {

    LivingEntity findTarget(E mob, Blackboard blackboard);
}
