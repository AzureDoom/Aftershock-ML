package mod.azure.aftershock.ai.actions.shrieker;

import mod.azure.aftershock.ai.core.*;
import mod.azure.aftershock.entity.shrieker.ShriekerEntity;

public class StatisAction implements Action<ShriekerEntity> {

    private static final int STASIS_START_TICKS = 40; // adjust to match statis_start clip length

    private final float growthThreshold;

    private int age;

    private boolean idleStarted;

    public StatisAction(float growthThreshold) {
        this.growthThreshold = growthThreshold;
    }

    public boolean shouldActivate(ShriekerEntity mob) {
        return mob.getGrowth() >= growthThreshold;
    }

    @Override
    public void start(ShriekerEntity mob, Blackboard blackboard, Cooldowns cooldowns) {
        this.age = 0;
        this.idleStarted = false;
        mob.setAggressive(false);

        mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y, 0.0D);
        mob.hasImpulse = true;

        mob.animationDispatcher.statisStart();
    }

    @Override
    public ActionStatus tick(ShriekerEntity mob, Blackboard blackboard, Cooldowns cooldowns) {
        age++;

        mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y, 0.0D);
        mob.hasImpulse = true;

        if (!idleStarted && age >= STASIS_START_TICKS) {
            mob.animationDispatcher.statisIdle();
            idleStarted = true;
        }

        if (idleStarted && age % 20 == 0) {
            mob.animationDispatcher.statisIdle();
        }

        return ActionStatus.RUNNING;
    }

    @Override
    public void stop(ShriekerEntity mob, Blackboard blackboard, ActionStatus reason) {}

    @Override
    public boolean isInterruptible() {
        return false;
    }

    @Override
    public int priority() {
        return 1;
    }
}
