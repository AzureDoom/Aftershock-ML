package mod.azure.aftershock.ai.actions.graboid;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import mod.azure.aftershock.ai.core.*;
import mod.azure.aftershock.ai.util.AiDebugUtils;
import mod.azure.aftershock.ai.util.GraboidBurrowUtils;
import mod.azure.aftershock.entity.dirtdragon.DirtDragonEntity;
import mod.azure.aftershock.entity.graboid.GraboidEntity;

public final class UndergroundStalkTargetAction<E extends Mob> implements Action<E> {

    private final double stalkSpeed;

    private final double minDepthBelowTarget;

    private final double maxDepthBelowTarget;

    private final double desiredHorizontalDistance;

    private final double repathDistance;

    private final int repathInterval;

    private final int priority;

    private final SoundEvent huntingSound;

    private final int minSoundInterval;

    private final int maxSoundInterval;

    private Vec3 destination;

    private int ticksUntilRepath;

    private int soundCooldown;

    public UndergroundStalkTargetAction(
        double stalkSpeed,
        double minDepthBelowTarget,
        double maxDepthBelowTarget,
        double desiredHorizontalDistance,
        double repathDistance,
        int repathInterval,
        int priority,
        SoundEvent huntingSound,
        int minSoundInterval,
        int maxSoundInterval
    ) {
        this.stalkSpeed = stalkSpeed;
        this.minDepthBelowTarget = minDepthBelowTarget;
        this.maxDepthBelowTarget = maxDepthBelowTarget;
        this.desiredHorizontalDistance = desiredHorizontalDistance;
        this.repathDistance = repathDistance;
        this.repathInterval = repathInterval;
        this.priority = priority;
        this.huntingSound = huntingSound;
        this.minSoundInterval = minSoundInterval;
        this.maxSoundInterval = maxSoundInterval;
    }

    @Override
    public void start(E mob, Blackboard blackboard, Cooldowns cooldowns) {
        this.destination = null;
        this.ticksUntilRepath = 0;
        this.soundCooldown = nextSoundCooldown(mob);

        mob.getNavigation().stop();
        mob.setAggressive(true);

        if (mob instanceof GraboidEntity graboidEntity) {
            graboidEntity.setBurrowing(true);
            graboidEntity.animationDispatcher.serverBurrow();
        }

        if (mob instanceof DirtDragonEntity dirtDragonEntity) {
            dirtDragonEntity.setBurrowing(true);
        }
    }

    @Override
    public ActionStatus tick(E mob, Blackboard blackboard, Cooldowns cooldowns) {
        LivingEntity target = blackboard.get(AiKeys.TARGET, LivingEntity.class);

        if (target == null || !target.isAlive()) {
            return ActionStatus.INTERRUPTED;
        }

        if (mob.getHealth() <= 0) {
            return ActionStatus.INTERRUPTED;
        }

        if (mob instanceof GraboidEntity graboid) {
            graboid.setBurrowing(true);
        }

        if (mob instanceof DirtDragonEntity dirtDragonEntity) {
            dirtDragonEntity.setBurrowing(true);
        }

        if (!GraboidBurrowUtils.isDeepEnoughToBurrow(mob, minDepthBelowTarget)) {
            destination = GraboidBurrowUtils.pickDiveDestination(
                mob,
                minDepthBelowTarget,
                maxDepthBelowTarget
            );

            GraboidBurrowUtils.moveTowardIgnoringBurrowCheck(
                mob,
                destination,
                Math.max(stalkSpeed, 0.3D)
            );

            AiDebugUtils.sendParticlePath(mob, mob.position(), destination);
            return ActionStatus.RUNNING;
        }

        ticksUntilRepath--;

        if (
            destination == null || ticksUntilRepath <= 0 || mob.position().distanceToSqr(destination) < repathDistance
                * repathDistance
        ) {
            destination = pickStalkDestination(mob, target);
            ticksUntilRepath = repathInterval;
        }

        if (--soundCooldown <= 0 && huntingSound != null) {
            GraboidBurrowUtils.playHuntSound(
                mob,
                huntingSound,
                1.0F,
                1.0F
            );

            soundCooldown = nextSoundCooldown(mob);
        }

        if (!GraboidBurrowUtils.canOccupyBurrowPosition(mob, destination)) {
            destination = pickStalkDestination(mob, target);
        }

        if (!GraboidBurrowUtils.moveTowardBurrowableOnly(mob, destination, stalkSpeed)) {
            GraboidBurrowUtils.rotateToward(mob, target.position(), 14.0F);
            destination = pickStalkDestination(mob, target);
        }

        AiDebugUtils.sendParticlePath(mob, mob.position(), destination);

        return ActionStatus.RUNNING;
    }

    @Override
    public void stop(E mob, Blackboard blackboard, ActionStatus reason) {
        GraboidBurrowUtils.slowDown(mob);
        mob.setAggressive(false);
        mob.setXRot(0.0F);

        if (mob instanceof DirtDragonEntity dirtDragon) {
            dirtDragon.setBurrowing(false);
        }

        if (mob instanceof GraboidEntity graboidEntity) {
            graboidEntity.setBurrowing(false);
        }
    }

    @Override
    public boolean isInterruptible() {
        return true;
    }

    @Override
    public int priority() {
        return priority;
    }

    private Vec3 pickStalkDestination(E mob, LivingEntity target) {
        var targetSurfaceY = GraboidBurrowUtils.getSurfaceY(
            mob,
            target.getX(),
            target.getZ()
        );

        var depth = minDepthBelowTarget
            + mob.getRandom().nextDouble() * Math.max(0.1D, maxDepthBelowTarget - minDepthBelowTarget);

        var underTarget = new Vec3(
            target.getX(),
            targetSurfaceY - depth,
            target.getZ()
        );

        if (GraboidBurrowUtils.canOccupyBurrowPosition(mob, underTarget)) {
            return underTarget;
        }

        return new Vec3(
            target.getX(),
            mob.getY(),
            target.getZ()
        );
    }

    private int nextSoundCooldown(E mob) {
        return minSoundInterval + mob.getRandom().nextInt(Math.max(1, maxSoundInterval - minSoundInterval));
    }
}
