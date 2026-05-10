package mod.azure.aftershock.ai.actions.blaster;

import java.util.function.Consumer;

import mod.azure.aftershock.ai.core.Action;
import mod.azure.aftershock.ai.core.ActionStatus;
import mod.azure.aftershock.ai.core.Blackboard;
import mod.azure.aftershock.ai.core.Cooldowns;
import mod.azure.aftershock.entity.blaster.BlasterEntity;
import mod.azure.aftershock.registry.BlockRegistry;

public class LayBlasterEggAction implements Action<BlasterEntity> {

    private static final int START_ANIMATION_TICKS = 30;

    private static final int END_ANIMATION_TICKS = 40;

    private static final int TOTAL_TICKS = START_ANIMATION_TICKS + END_ANIMATION_TICKS;

    private final int priority;

    private final Consumer<BlasterEntity> startAnimation;

    private final Consumer<BlasterEntity> endAnimation;

    private int ticks;

    private boolean eggPlaced;

    private boolean endAnimationPlayed;

    public LayBlasterEggAction(
        int priority,
        Consumer<BlasterEntity> startAnimation,
        Consumer<BlasterEntity> endAnimation
    ) {
        this.priority = priority;
        this.startAnimation = startAnimation;
        this.endAnimation = endAnimation;
    }

    @Override
    public void start(BlasterEntity mob, Blackboard blackboard, Cooldowns cooldowns) {
        this.ticks = 0;
        this.eggPlaced = false;
        this.endAnimationPlayed = false;

        mob.getNavigation().stop();
        this.startAnimation.accept(mob);
    }

    @Override
    public ActionStatus tick(BlasterEntity mob, Blackboard blackboard, Cooldowns cooldowns) {
        mob.getNavigation().stop();
        this.ticks++;

        if (!this.eggPlaced && this.ticks >= START_ANIMATION_TICKS) {
            placeEgg(mob);
            mob.resetCookedFoodEaten();

            this.eggPlaced = true;
        }

        if (this.eggPlaced && !this.endAnimationPlayed) {
            this.endAnimation.accept(mob);
            this.endAnimationPlayed = true;
        }

        if (this.ticks >= TOTAL_TICKS) {
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    @Override
    public void stop(BlasterEntity mob, Blackboard blackboard, ActionStatus reason) {
        mob.getNavigation().stop();

        this.ticks = 0;
        this.eggPlaced = false;
        this.endAnimationPlayed = false;
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    @Override
    public int priority() {
        return this.priority;
    }

    private void placeEgg(BlasterEntity mob) {
        if (mob.level().isClientSide()) {
            return;
        }

        var basePos = mob.blockPosition();

        var eggPos = basePos;
        if (!mob.level().getBlockState(eggPos).canBeReplaced()) {
            eggPos = basePos.above();
        }

        if (mob.level().getBlockState(eggPos).canBeReplaced()) {
            mob.level().setBlockAndUpdate(eggPos, BlockRegistry.GRABOID_EGG.get().defaultBlockState());
        }
    }
}
