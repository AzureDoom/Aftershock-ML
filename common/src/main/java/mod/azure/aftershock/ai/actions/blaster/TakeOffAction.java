package mod.azure.aftershock.ai.actions.blaster;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

import mod.azure.aftershock.ai.core.*;

public final class TakeOffAction<E extends Mob> implements Action<E> {

    private final int totalTicks;

    private final int flightStartTick;

    private final double liftPower;

    private final double forwardPower;

    private final int cooldownTicks;

    private final String cooldownKey;

    private final int priority;

    private final Consumer<E> animationTrigger;

    private int age;

    private boolean flightStarted;

    public TakeOffAction(
        int totalTicks,
        int flightStartTick,
        double liftPower,
        double forwardPower,
        String cooldownKey,
        int cooldownTicks,
        int priority,
        Consumer<E> animationTrigger
    ) {
        this.totalTicks = totalTicks;
        this.flightStartTick = flightStartTick;
        this.liftPower = liftPower;
        this.forwardPower = forwardPower;
        this.cooldownKey = cooldownKey;
        this.cooldownTicks = cooldownTicks;
        this.priority = priority;
        this.animationTrigger = animationTrigger;
    }

    @Override
    public void start(E mob, Blackboard blackboard, Cooldowns cooldowns) {
        var alreadyTakingOff = blackboard.get(AiKeys.BLASTER_TAKING_OFF, Boolean.class);

        if (Boolean.TRUE.equals(alreadyTakingOff)) {
            return;
        }

        this.age = 0;
        this.flightStarted = false;

        blackboard.set(AiKeys.BLASTER_TAKING_OFF, true);
        blackboard.set(AiKeys.BLASTER_IN_FLIGHT, false);

        mob.setNoGravity(false);
        mob.getNavigation().stop();
        mob.setAggressive(true);
        mob.setDeltaMovement(0.0D, 0.0D, 0.0D);
        mob.hasImpulse = true;

        animationTrigger.accept(mob);
    }

    @Override
    public ActionStatus tick(E mob, Blackboard blackboard, Cooldowns cooldowns) {
        age++;

        if (mob.getHealth() <= 0) {
            mob.setAggressive(false);
            return ActionStatus.INTERRUPTED;
        }

        var target = blackboard.get(AiKeys.TARGET, LivingEntity.class);

        if (target != null && target.isAlive()) {
            faceTarget(mob, target);
        }

        if (age < flightStartTick) {
            mob.setNoGravity(false);

            mob.setDeltaMovement(
                0.0D,
                0.0D,
                0.0D
            );

            mob.hasImpulse = true;
            return ActionStatus.RUNNING;
        }

        if (!flightStarted) {
            flightStarted = true;

            blackboard.set(AiKeys.BLASTER_IN_FLIGHT, true);

            mob.setNoGravity(true);

            var forward = getForwardVector(mob);

            mob.setDeltaMovement(
                forward.x * forwardPower,
                liftPower,
                forward.z * forwardPower
            );

            mob.hasImpulse = true;
        } else {
            var current = mob.getDeltaMovement();

            mob.setDeltaMovement(
                current.x * 0.96D,
                Math.max(current.y * 0.92D, 0.08D),
                current.z * 0.96D
            );

            mob.hasImpulse = true;
        }

        spawnFireTrail(mob);

        if (age >= totalTicks) {
            cooldowns.set(cooldownKey, cooldownTicks);
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    @Override
    public void stop(E mob, Blackboard blackboard, ActionStatus reason) {
        blackboard.set(AiKeys.BLASTER_TAKING_OFF, false);

        if (reason == ActionStatus.SUCCESS) {
            blackboard.set(AiKeys.BLASTER_IN_FLIGHT, true);
            mob.setNoGravity(true);
        } else {
            blackboard.set(AiKeys.BLASTER_IN_FLIGHT, false);
            mob.setNoGravity(false);
        }

        mob.setXRot(0.0F);
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    @Override
    public int priority() {
        return priority;
    }

    private void spawnFireTrail(E mob) {
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        var forward = getForwardVector(mob);

        var trailPos = mob.position()
            .add(0.0D, mob.getBbHeight() * 0.45D, 0.0D)
            .subtract(forward.scale(mob.getBbWidth() * 1.2D));

        serverLevel.sendParticles(
            ParticleTypes.FLAME,
            trailPos.x,
            trailPos.y,
            trailPos.z,
            8,
            0.18D,
            0.18D,
            0.18D,
            0.015D
        );

        serverLevel.sendParticles(
            ParticleTypes.SMOKE,
            trailPos.x,
            trailPos.y,
            trailPos.z,
            2,
            0.12D,
            0.12D,
            0.12D,
            0.005D
        );
    }

    private Vec3 getForwardVector(E mob) {
        var yawRad = mob.getYRot() * ((float) Math.PI / 180.0F);

        return new Vec3(
            -Math.sin(yawRad),
            0.0D,
            Math.cos(yawRad)
        ).normalize();
    }

    private void faceTarget(E mob, LivingEntity target) {
        var dx = target.getX() - mob.getX();
        var dy = target.getEyeY() - mob.getEyeY();
        var dz = target.getZ() - mob.getZ();

        var horiz = Math.sqrt(dx * dx + dz * dz);

        var yaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        var pitch = (float) -(Math.atan2(dy, horiz) * (180.0D / Math.PI));

        mob.setYRot(yaw);
        mob.yBodyRot = yaw;
        mob.yHeadRot = yaw;
        mob.setXRot(pitch * 0.35F);

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }
}
