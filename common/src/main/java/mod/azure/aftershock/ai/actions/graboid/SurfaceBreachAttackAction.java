package mod.azure.aftershock.ai.actions.graboid;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

import mod.azure.aftershock.ai.core.*;
import mod.azure.aftershock.ai.util.GraboidBurrowUtils;
import mod.azure.aftershock.entity.graboid.GraboidEntity;

public final class SurfaceBreachAttackAction<E extends Mob> implements Action<E> {

    private enum BreachType {
        DRAG_DOWN,
        ERUPT,
        BITE
    }

    private final double triggerRange;

    private final int priority;

    private final float biteDamage;

    private final float dragDamage;

    private final double dragDownDepth;

    private final SoundEvent breachSound;

    private final double dragDownWeight;

    private final double eruptWeight;

    private final double biteWeight;

    private final Consumer<E> dragDownAnimationTrigger;

    private final Consumer<E> eruptAnimationTrigger;

    private final Consumer<E> biteAnimationTrigger;

    private static final int DRAG_DOWN_DURATION = 80;

    private static final int DRAG_PULL_START_TICK = 44;

    private static final int ERUPT_DURATION = 100;

    private static final int BITE_DURATION = 60;

    private static final int BITE_ATTACK_TICK = 18;

    private BreachType breachType;

    private LivingEntity grabbedTarget;

    private int age;

    private boolean hasHit;

    private boolean hasStartedPulling;

    private double grabbedTargetStartY;

    private double grabbedTargetEndY;

    private static final double MOUTH_FORWARD_OFFSET = 2.5D;

    private static final double MOUTH_SIDE_OFFSET = 0.0D;

    private static final double MOUTH_UP_OFFSET = 1.15D;

    private static final double TARGET_FEET_BELOW_MOUTH = 0.95D;

    public SurfaceBreachAttackAction(
        double triggerRange,
        float biteDamage,
        float dragDamage,
        double dragDownDepth,
        int priority,
        SoundEvent breachSound,
        double dragDownWeight,
        double eruptWeight,
        double biteWeight,
        Consumer<E> dragDownAnimationTrigger,
        Consumer<E> eruptAnimationTrigger,
        Consumer<E> biteAnimationTrigger
    ) {
        this.triggerRange = triggerRange;
        this.biteDamage = biteDamage;
        this.dragDamage = dragDamage;
        this.dragDownDepth = dragDownDepth;
        this.priority = priority;
        this.breachSound = breachSound;
        this.dragDownWeight = dragDownWeight;
        this.eruptWeight = eruptWeight;
        this.biteWeight = biteWeight;
        this.dragDownAnimationTrigger = dragDownAnimationTrigger;
        this.eruptAnimationTrigger = eruptAnimationTrigger;
        this.biteAnimationTrigger = biteAnimationTrigger;
    }

    @Override
    public void start(E mob, Blackboard blackboard, Cooldowns cooldowns) {
        this.age = 0;
        this.hasHit = false;
        this.hasStartedPulling = false;
        this.grabbedTarget = null;
        this.breachType = chooseBreachType(mob);

        mob.getNavigation().stop();
        mob.setAggressive(true);
        mob.setNoGravity(false);

        var target = blackboard.get(AiKeys.TARGET, LivingEntity.class);

        moveToBreachSurfacePosition(mob, target);

        setBurrowing(mob, false);

        triggerAnimation(mob);

        GraboidBurrowUtils.playHuntSound(
            mob,
            breachSound,
            0.5F,
            1.0F
        );
    }

    @Override
    public ActionStatus tick(E mob, Blackboard blackboard, Cooldowns cooldowns) {
        var target = blackboard.get(AiKeys.TARGET, LivingEntity.class);

        if (mob.getHealth() <= 0.0F) {
            return ActionStatus.INTERRUPTED;
        }

        age++;

        return switch (breachType) {
            case DRAG_DOWN -> tickDragDown(mob, target);
            case ERUPT -> tickErupt(mob);
            case BITE -> tickBite(mob, target);
        };
    }

    private ActionStatus tickDragDown(E mob, LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return ActionStatus.INTERRUPTED;
        }

        if (grabbedTarget == null && isWithinHorizontalRange(mob, target, triggerRange)) {
            grabbedTarget = target;

            mob.getNavigation().stop();
            mob.setNoGravity(true);

            grabbedTarget.setNoGravity(true);
            grabbedTarget.hurtMarked = true;

            this.grabbedTargetStartY = grabbedTarget.getY();

            GraboidBurrowUtils.slowDown(mob);
        }

        if (grabbedTarget != null && grabbedTarget.isAlive() && !hasStartedPulling) {
            holdGrabbedTarget(mob, grabbedTarget);
        }

        if (age >= DRAG_PULL_START_TICK && grabbedTarget != null && grabbedTarget.isAlive()) {
            if (!hasStartedPulling) {
                hasStartedPulling = true;

                var surfaceY = GraboidBurrowUtils.getSurfaceY(
                    mob,
                    grabbedTarget.getX(),
                    grabbedTarget.getZ()
                );

                this.grabbedTargetEndY = surfaceY - dragDownDepth;

                setBurrowing(mob, true);
            }

            pullTargetDown(mob, grabbedTarget);
        }

        if (age >= DRAG_DOWN_DURATION) {
            if (grabbedTarget != null && grabbedTarget.isAlive()) {
                grabbedTarget.hurt(mob.damageSources().mobAttack(mob), dragDamage);
                grabbedTarget.hurtMarked = true;
            }

            clearGrabbedTarget(mob);
            setBurrowing(mob, true);
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    private void holdGrabbedTarget(E mob, LivingEntity target) {
        GraboidBurrowUtils.rotateToward(mob, target.position(), 20.0F);

        Vec3 mouth = getMouthPosition(mob);

        target.setPos(
            mouth.x,
            mouth.y - TARGET_FEET_BELOW_MOUTH,
            mouth.z
        );

        mob.setDeltaMovement(0.0D, 0.0D, 0.0D);
        target.setDeltaMovement(0.0D, 0.0D, 0.0D);

        mob.hasImpulse = true;
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    private ActionStatus tickErupt(E mob) {
        if (age >= ERUPT_DURATION) {
            setBurrowing(mob, true);
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    private ActionStatus tickBite(E mob, LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return ActionStatus.INTERRUPTED;
        }

        if (!hasHit && age >= BITE_ATTACK_TICK) {
            if (isWithinHorizontalRange(mob, target, triggerRange)) {
                target.hurt(mob.damageSources().mobAttack(mob), biteDamage);
                target.hurtMarked = true;
                hasHit = true;
            }
        }

        if (age >= BITE_DURATION) {
            setBurrowing(mob, true);
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    private void pullTargetDown(E mob, LivingEntity target) {
        var pullTicks = Math.max(1, DRAG_DOWN_DURATION - DRAG_PULL_START_TICK);
        var currentPullTick = Math.min(pullTicks, age - DRAG_PULL_START_TICK);

        var progress = currentPullTick / (double) pullTicks;

        var targetY = grabbedTargetStartY + (grabbedTargetEndY - grabbedTargetStartY) * progress;

        mob.setPos(
            mob.getX(),
            targetY - MOUTH_UP_OFFSET,
            mob.getZ()
        );

        var mouth = getMouthPosition(mob);

        target.setPos(
            mouth.x,
            mouth.y - TARGET_FEET_BELOW_MOUTH,
            mouth.z
        );

        mob.setDeltaMovement(0.0D, -0.25D, 0.0D);
        target.setDeltaMovement(0.0D, -0.25D, 0.0D);

        mob.hasImpulse = true;
        target.hasImpulse = true;
        target.hurtMarked = true;

        GraboidBurrowUtils.rotateToward(mob, target.position(), 20.0F);
    }

    private static boolean isWithinHorizontalRange(Mob mob, LivingEntity target, double range) {
        double dx = mob.getX() - target.getX();
        double dz = mob.getZ() - target.getZ();
        return dx * dx + dz * dz <= range * range;
    }

    private BreachType chooseBreachType(E mob) {
        double totalWeight = Math.max(0.0D, dragDownWeight)
            + Math.max(0.0D, eruptWeight)
            + Math.max(0.0D, biteWeight);

        if (totalWeight <= 0.0D) {
            return BreachType.BITE;
        }

        double roll = mob.getRandom().nextDouble() * totalWeight;

        if (roll < dragDownWeight) {
            return BreachType.DRAG_DOWN;
        }

        roll -= dragDownWeight;

        if (roll < eruptWeight) {
            return BreachType.ERUPT;
        }

        return BreachType.BITE;
    }

    private void triggerAnimation(E mob) {
        switch (breachType) {
            case DRAG_DOWN -> {
                if (dragDownAnimationTrigger != null) {
                    dragDownAnimationTrigger.accept(mob);
                }
            }
            case ERUPT -> {
                if (eruptAnimationTrigger != null) {
                    eruptAnimationTrigger.accept(mob);
                }
            }
            case BITE -> {
                if (biteAnimationTrigger != null) {
                    biteAnimationTrigger.accept(mob);
                }
            }
        }
    }

    private void clearGrabbedTarget(E mob) {
        mob.setNoGravity(false);

        if (grabbedTarget != null) {
            if (grabbedTarget.isPassenger() && grabbedTarget.getVehicle() == mob) {
                grabbedTarget.stopRiding();
            }

            grabbedTarget.setNoGravity(false);
            grabbedTarget.hurtMarked = true;
            grabbedTarget = null;
        }
    }

    @Override
    public void stop(E mob, Blackboard blackboard, ActionStatus reason) {
        if (mob.isPassenger()) {
            mob.stopRiding();
        }

        clearGrabbedTarget(mob);

        mob.setNoGravity(false);
        mob.setAggressive(false);
        mob.setXRot(0.0F);

        GraboidBurrowUtils.slowDown(mob);

        if (mob.isAlive() && mob.getHealth() > 0.0F) {
            setBurrowing(mob, true);
        }
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    @Override
    public int priority() {
        return priority;
    }

    private static void setBurrowing(Mob mob, boolean burrowing) {
        if (mob instanceof GraboidEntity graboid) {
            graboid.setBurrowing(burrowing);
        }
    }

    private void moveToBreachSurfacePosition(E mob, LivingEntity target) {
        double x = target != null && target.isAlive() ? target.getX() : mob.getX();
        double z = target != null && target.isAlive() ? target.getZ() : mob.getZ();

        double surfaceY = GraboidBurrowUtils.getSurfaceY(mob, x, z);

        mob.setPos(
            x,
            surfaceY - 2.5D,
            z
        );

        mob.setDeltaMovement(0.0D, 0.0D, 0.0D);
        mob.hasImpulse = true;
    }

    private Vec3 getMouthPosition(E mob) {
        var yawRad = Math.toRadians(mob.getYRot());
        var forwardX = -Math.sin(yawRad);
        var forwardZ = Math.cos(yawRad);
        var rightX = Math.cos(yawRad);
        var rightZ = Math.sin(yawRad);

        return new Vec3(
            mob.getX() + forwardX * MOUTH_FORWARD_OFFSET + rightX * MOUTH_SIDE_OFFSET,
            mob.getY() + MOUTH_UP_OFFSET,
            mob.getZ() + forwardZ * MOUTH_FORWARD_OFFSET + rightZ * MOUTH_SIDE_OFFSET
        );
    }
}
