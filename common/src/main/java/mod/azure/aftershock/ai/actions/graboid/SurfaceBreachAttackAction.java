package mod.azure.aftershock.ai.actions.graboid;

import com.mojang.logging.LogUtils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.function.Consumer;

import mod.azure.aftershock.ai.core.*;
import mod.azure.aftershock.ai.util.GraboidBurrowUtils;
import mod.azure.aftershock.entity.graboid.GraboidEntity;
import mod.azure.aftershock.entity.graboid.GraboidHitboxes;
import org.slf4j.Logger;

public final class SurfaceBreachAttackAction<E extends Mob> implements Action<E> {

    private enum BreachType {
        DRAG_DOWN(3.6D, 5.8D, 35),
        ERUPT(2.5D, 7.0D, 30),
        BITE(1.35D, 6.4D, 24);

        final double forward;

        final double up;

        final int calibrationTick;

        BreachType(double forward, double up, int calibrationTick) {
            this.forward = forward;
            this.up = up;
            this.calibrationTick = calibrationTick;
        }
    }

    private static final boolean DEBUG_STRIKE_OFFSETS = false;

    private static final Logger LOGGER = LogUtils.getLogger();

    private final double triggerRange;

    private final int priority;

    private final float biteDamage;

    private final float dragDamage;

    private final float tentacleDamage;

    private final double dragDownDepth;

    private final SoundEvent breachSound;

    private final double dragDownWeight;

    private final double eruptWeight;

    private final double biteWeight;

    private final Consumer<E> dragDownAnimationTrigger;

    private final Consumer<E> eruptAnimationTrigger;

    private final Consumer<E> biteAnimationTrigger;

    private static final int STRIKE_WINDOW = 5;

    private static final int DRAG_DOWN_DURATION = 80;

    private static final int DRAG_GRAB_START_TICK = 26;

    private static final int DRAG_PULL_START_TICK = 44;

    private static final int BITE_DURATION = 60;

    private static final int BITE_JAW_CLOSE_TICK = 21;

    private static final int ERUPT_DURATION = 100;

    private static final int[][] ERUPT_TENTACLE_SNAPS = {
            { 14, 21, 28, 36, 45, 54, 63 },
            { 12, 19, 27, 34, 43, 50, 58, 69 },
            { 18, 24, 32, 39, 48, 55, 65 }
    };

    private static final int ERUPT_JAW_CLOSE_TICK = 88;

    private BreachType breachType;

    private LivingEntity grabbedTarget;

    private int age;

    private boolean hasHit;

    private boolean hasStartedPulling;

    private double grabbedTargetEndY;

    private double mobPullStartY;

    private double mobPullEndY;

    private final int[] lastTentacleSnapHit = new int[ERUPT_TENTACLE_SNAPS.length];

    private static final double GRAB_MOUTH_FORWARD = 3.67D;

    private static final double GRAB_MOUTH_UP = 5.75D;

    private static final double REST_MOUTH_FORWARD = 4.0D;

    private static final double REST_MOUTH_UP = 0.81D;

    public SurfaceBreachAttackAction(
            double triggerRange,
            float biteDamage,
            float dragDamage,
            float tentacleDamage,
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
        this.tentacleDamage = tentacleDamage;
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
        Arrays.fill(this.lastTentacleSnapHit, -1);
        this.breachType = chooseBreachType(mob);

        mob.getNavigation().stop();
        mob.setAggressive(true);
        mob.setNoGravity(false);

        var target = blackboard.get(AiKeys.TARGET, LivingEntity.class);

        moveToBreachPosition(mob, target);

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

        if (DEBUG_STRIKE_OFFSETS && age == breachType.calibrationTick) {
            logStrikeOffsets(mob);
        }

        return switch (breachType) {
            case DRAG_DOWN -> tickDragDown(mob, target);
            case ERUPT -> tickErupt(mob, target);
            case BITE -> tickBite(mob, target);
        };
    }

    private ActionStatus tickDragDown(E mob, LivingEntity target) {
        if (grabbedTarget == null && (target == null || !target.isAlive())) {
            return ActionStatus.INTERRUPTED;
        }

        if (grabbedTarget == null && age >= DRAG_GRAB_START_TICK && age <= DRAG_PULL_START_TICK
                && canGrab(mob, target)) {
            grabbedTarget = target;

            mob.getNavigation().stop();
            mob.setNoGravity(true);

            grabbedTarget.setNoGravity(true);
            grabbedTarget.hurtMarked = true;

            GraboidBurrowUtils.slowDown(mob);
        }

        if (grabbedTarget != null && !grabbedTarget.isAlive()) {
            return ActionStatus.INTERRUPTED;
        }

        if (grabbedTarget != null && !hasStartedPulling) {
            holdGrabbedTarget(mob, grabbedTarget);
        }

        if (age >= DRAG_PULL_START_TICK && grabbedTarget != null) {
            if (!hasStartedPulling) {
                hasStartedPulling = true;

                var surfaceY = GraboidBurrowUtils.getSurfaceY(
                        mob,
                        grabbedTarget.getX(),
                        grabbedTarget.getZ()
                );

                this.grabbedTargetEndY = surfaceY - dragDownDepth;
                this.mobPullStartY = mob.getY();
                this.mobPullEndY = grabbedTargetEndY + grabbedTarget.getBbHeight() * 0.5D - REST_MOUTH_UP;

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

    private boolean canGrab(E mob, LivingEntity target) {
        if (!GraboidHitboxes.hasLivePose(mob)) {
            return isWithinHorizontalRange(mob, target, triggerRange);
        }

        return GraboidHitboxes.isAnyTentacleTouching(mob, target) || GraboidHitboxes.isInJaws(mob, target);
    }

    private void holdGrabbedTarget(E mob, LivingEntity target) {
        mob.setDeltaMovement(0.0D, 0.0D, 0.0D);
        mob.hasImpulse = true;

        glueToMouth(mob, target, 0.0D, 0.0D);
    }

    private void glueToMouth(E mob, LivingEntity target, double pullProgress, double verticalVelocity) {
        var mouth = GraboidHitboxes.center(mob, GraboidHitboxes.MOUTH);

        if (mouth == null) {
            mouth = offsetFromOrigin(
                    mob,
                    Mth.lerp(pullProgress, GRAB_MOUTH_FORWARD, REST_MOUTH_FORWARD),
                    Mth.lerp(pullProgress, GRAB_MOUTH_UP, REST_MOUTH_UP)
            );
        }

        target.setPos(mouth.x, mouth.y - target.getBbHeight() * 0.5D, mouth.z);
        target.setDeltaMovement(0.0D, verticalVelocity, 0.0D);
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    private ActionStatus tickErupt(E mob, LivingEntity target) {
        if (target != null && target.isAlive() && GraboidHitboxes.hasLivePose(mob)) {
            tickTentacleStrikes(mob, target);

            if (!hasHit && isInWindow(age, ERUPT_JAW_CLOSE_TICK) && GraboidHitboxes.isInJaws(mob, target)) {
                target.hurt(mob.damageSources().mobAttack(mob), biteDamage);
                target.hurtMarked = true;
                hasHit = true;
            }
        }

        if (age >= ERUPT_DURATION) {
            setBurrowing(mob, true);
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    private void tickTentacleStrikes(E mob, LivingEntity target) {
        for (int i = 0; i < ERUPT_TENTACLE_SNAPS.length; i++) {
            var snap = activeSnap(ERUPT_TENTACLE_SNAPS[i], age);

            if (snap < 0 || lastTentacleSnapHit[i] == snap) {
                continue;
            }

            if (GraboidHitboxes.isTentacleTouching(mob, i, target)) {
                target.hurt(mob.damageSources().mobAttack(mob), tentacleDamage);
                target.hurtMarked = true;
                lastTentacleSnapHit[i] = snap;
            }
        }
    }

    private ActionStatus tickBite(E mob, LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return ActionStatus.INTERRUPTED;
        }

        if (!hasHit && isInWindow(age, BITE_JAW_CLOSE_TICK) && isInJaws(mob, target)) {
            target.hurt(mob.damageSources().mobAttack(mob), biteDamage);
            target.hurtMarked = true;
            hasHit = true;
        }

        if (age >= BITE_DURATION) {
            setBurrowing(mob, true);
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    private boolean isInJaws(E mob, LivingEntity target) {
        if (!GraboidHitboxes.hasLivePose(mob)) {
            return isWithinHorizontalRange(mob, target, triggerRange);
        }

        return GraboidHitboxes.isInJaws(mob, target);
    }

    private static boolean isInWindow(int age, int keyframeTick) {
        return age >= keyframeTick && age < keyframeTick + STRIKE_WINDOW;
    }

    private static int activeSnap(int[] snaps, int age) {
        for (var snap : snaps) {
            if (isInWindow(age, snap)) {
                return snap;
            }
        }
        return -1;
    }

    private void pullTargetDown(E mob, LivingEntity target) {
        var pullTicks = Math.max(1, DRAG_DOWN_DURATION - DRAG_PULL_START_TICK);
        var currentPullTick = Math.min(pullTicks, age - DRAG_PULL_START_TICK);

        var progress = currentPullTick / (double) pullTicks;

        mob.setPos(
                mob.getX(),
                Mth.lerp(progress, mobPullStartY, mobPullEndY),
                mob.getZ()
        );

        mob.setDeltaMovement(0.0D, -0.25D, 0.0D);
        mob.hasImpulse = true;

        glueToMouth(mob, target, progress, -0.25D);
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

    private void moveToBreachPosition(E mob, LivingEntity target) {
        var yaw = mob.getYRot();

        mob.setYRot(yaw);
        mob.yRotO = yaw;
        mob.setYBodyRot(yaw);
        mob.yBodyRotO = yaw;
        mob.setYHeadRot(yaw);
        mob.yHeadRotO = yaw;

        if (target == null || !target.isAlive()) {
            var surfaceY = GraboidBurrowUtils.getSurfaceY(mob, mob.getX(), mob.getZ());
            mob.setPos(mob.getX(), surfaceY - 2.5D, mob.getZ());
        } else {
            var forward = forward(mob);
            var aimY = target.getY() + target.getBbHeight() * 0.5D;

            mob.setPos(
                    target.getX() - forward.x * breachType.forward,
                    aimY - breachType.up,
                    target.getZ() - forward.z * breachType.forward
            );
        }

        mob.setDeltaMovement(0.0D, 0.0D, 0.0D);
        mob.hasImpulse = true;
    }

    private static Vec3 forward(Mob mob) {
        var yawRad = Math.toRadians(mob.getYRot());
        return new Vec3(-Math.sin(yawRad), 0.0D, Math.cos(yawRad));
    }

    private static Vec3 offsetFromOrigin(Mob mob, double forwardBlocks, double upBlocks) {
        var forward = forward(mob);
        return new Vec3(
                mob.getX() + forward.x * forwardBlocks,
                mob.getY() + upBlocks,
                mob.getZ() + forward.z * forwardBlocks
        );
    }

    private void logStrikeOffsets(E mob) {
        if (!GraboidHitboxes.hasLivePose(mob)) {
            LOGGER.info("[graboid] {} tick {}: no live pose, stand closer", breachType, age);
            return;
        }

        logOffset(mob, GraboidHitboxes.MOUTH);
        for (var tentacle : GraboidHitboxes.TENTACLES) {
            logOffset(mob, tentacle);
        }
    }

    private void logOffset(E mob, String name) {
        var center = GraboidHitboxes.center(mob, name);
        if (center == null) {
            LOGGER.info("[graboid] {} tick {} {}: disabled", breachType, age, name);
            return;
        }

        var forward = forward(mob);
        var delta = center.subtract(mob.position());

        LOGGER.info(
                "[graboid] {} tick {} {}: forward {} up {}",
                breachType,
                age,
                name,
                String.format("%.2f", delta.x * forward.x + delta.z * forward.z),
                String.format("%.2f", delta.y)
        );
    }
}