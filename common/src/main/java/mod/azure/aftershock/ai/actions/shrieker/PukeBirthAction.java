package mod.azure.aftershock.ai.actions.shrieker;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Consumer;

import mod.azure.aftershock.ai.core.*;
import mod.azure.aftershock.entity.shrieker.ShriekerEntity;
import mod.azure.aftershock.registry.EntityRegistry;

public class PukeBirthAction implements Action<ShriekerEntity> {

    private static final int SPAWN_TICK = 20;

    private final int totalTicks;

    private final int priority;

    private final Consumer<ShriekerEntity> pukeAnimation;

    private int age;

    private boolean spawned;

    public PukeBirthAction(int totalTicks, int priority, Consumer<ShriekerEntity> pukeAnimation) {
        this.totalTicks = totalTicks;
        this.priority = priority;
        this.pukeAnimation = pukeAnimation;
    }

    @Override
    public void start(ShriekerEntity mob, Blackboard blackboard, Cooldowns cooldowns) {
        this.age = 0;
        this.spawned = false;
        mob.setAggressive(false);

        mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y, 0.0D);
        mob.hasImpulse = true;

        pukeAnimation.accept(mob);
    }

    @Override
    public ActionStatus tick(ShriekerEntity mob, Blackboard blackboard, Cooldowns cooldowns) {
        age++;

        mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y, 0.0D);
        mob.hasImpulse = true;

        if (!spawned && age >= SPAWN_TICK) {
            spawnChild(mob);
            mob.resetCookedFoodEaten();
            spawned = true;
        }

        return age >= totalTicks ? ActionStatus.SUCCESS : ActionStatus.RUNNING;
    }

    @Override
    public void stop(ShriekerEntity mob, Blackboard blackboard, ActionStatus reason) {
        mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y, 0.0D);
        mob.hasImpulse = true;
        if (!spawned) {
            mob.resetCookedFoodEaten();
        }
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    @Override
    public int priority() {
        return this.priority;
    }

    private void spawnChild(ShriekerEntity mob) {
        if (mob.level().isClientSide())
            return;
        var child = EntityRegistry.SHRIEKER.get().create(mob.level());
        if (child == null)
            return;
        child.setPos(mob.getX(), mob.getY(), mob.getZ());
        if (mob.level() instanceof ServerLevel serverLevel)
            serverLevel.addAlwaysVisibleParticle(ParticleTypes.SMOKE, mob.getX(), mob.getY(), mob.getZ(), 0, 0, 0);
        mob.level().addFreshEntity(child);
    }
}
