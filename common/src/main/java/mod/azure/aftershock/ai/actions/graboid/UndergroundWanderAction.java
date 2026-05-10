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

public final class UndergroundWanderAction<E extends Mob> implements Action<E> {

    private static final int DESTINATION_ATTEMPTS = 12;

    private final double burrowSpeed;

    private final double horizRadius;

    private final double minDepthBelowSurface;

    private final double maxDepthBelowSurface;

    private final int minDuration;

    private final int maxDuration;

    private final int priority;

    private final SoundEvent idleBurrowSound;

    private final int minSoundInterval;

    private final int maxSoundInterval;

    private Vec3 home;

    private double homeRadius = 20.0D;

    private Vec3 destination;

    private int age;

    private int duration;

    private int soundCooldown;

    public UndergroundWanderAction(
        double burrowSpeed,
        double horizRadius,
        double minDepthBelowSurface,
        double maxDepthBelowSurface,
        int minDuration,
        int maxDuration,
        int priority,
        SoundEvent idleBurrowSound,
        int minSoundInterval,
        int maxSoundInterval
    ) {
        this.burrowSpeed = burrowSpeed;
        this.horizRadius = horizRadius;
        this.minDepthBelowSurface = minDepthBelowSurface;
        this.maxDepthBelowSurface = maxDepthBelowSurface;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.priority = priority;
        this.idleBurrowSound = idleBurrowSound;
        this.minSoundInterval = minSoundInterval;
        this.maxSoundInterval = maxSoundInterval;
    }

    public void setHome(Vec3 home, double homeRadius) {
        this.home = home;
        this.homeRadius = homeRadius;
    }

    @Override
    public void start(E mob, Blackboard blackboard, Cooldowns cooldowns) {
        this.age = 0;
        this.duration = minDuration + mob.getRandom().nextInt(Math.max(1, maxDuration - minDuration));
        this.destination = pickDestination(mob);
        this.soundCooldown = nextSoundCooldown(mob);

        mob.getNavigation().stop();
        mob.setAggressive(false);

        if (mob instanceof GraboidEntity graboid) {
            graboid.setBurrowing(true);
        }

        if (mob instanceof DirtDragonEntity dirtDragonEntity) {
            dirtDragonEntity.setBurrowing(true);
        }
    }

    @Override
    public ActionStatus tick(E mob, Blackboard blackboard, Cooldowns cooldowns) {
        if (mob.getHealth() <= 0) {
            return ActionStatus.INTERRUPTED;
        }

        var target = blackboard.get(AiKeys.TARGET, LivingEntity.class);
        if (target != null && target.isAlive()) {
            return ActionStatus.INTERRUPTED;
        }

        if (mob instanceof GraboidEntity graboid) {
            graboid.setBurrowing(true);
        }

        if (mob instanceof DirtDragonEntity dirtDragonEntity) {
            dirtDragonEntity.setBurrowing(true);
        }

        if (!GraboidBurrowUtils.isDeepEnoughToBurrow(mob, minDepthBelowSurface)) {
            destination = GraboidBurrowUtils.pickDiveDestination(
                mob,
                minDepthBelowSurface,
                maxDepthBelowSurface
            );

            GraboidBurrowUtils.moveTowardIgnoringBurrowCheck(
                mob,
                destination,
                Math.max(burrowSpeed, 0.25D)
            );

            AiDebugUtils.sendParticlePath(mob, mob.position(), destination);
            return ActionStatus.RUNNING;
        }

        age++;

        if (destination == null) {
            destination = pickDestination(mob);
        }

        if (--soundCooldown <= 0 && idleBurrowSound != null) {
            GraboidBurrowUtils.playHuntSound(
                mob,
                idleBurrowSound,
                0.7F,
                0.8F + mob.getRandom().nextFloat() * 0.3F
            );
            soundCooldown = nextSoundCooldown(mob);
        }

        if (mob.position().distanceToSqr(destination) < 1.5D || age >= duration) {
            GraboidBurrowUtils.slowDown(mob);
            return ActionStatus.SUCCESS;
        }

        if (!GraboidBurrowUtils.canOccupyBurrowPosition(mob, destination)) {
            destination = pickDestination(mob);
        }

        if (!GraboidBurrowUtils.moveTowardBurrowableOnly(mob, destination, burrowSpeed)) {
            destination = pickDestination(mob);
            GraboidBurrowUtils.rotateToward(mob, destination, 10.0F);
        }

        AiDebugUtils.sendParticlePath(mob, mob.position(), destination);

        return ActionStatus.RUNNING;
    }

    @Override
    public void stop(E mob, Blackboard blackboard, ActionStatus reason) {
        GraboidBurrowUtils.slowDown(mob);
        mob.setXRot(0.0F);

        if (mob instanceof DirtDragonEntity dirtDragon) {
            dirtDragon.setBurrowing(false);
        }

        if (mob instanceof GraboidEntity graboidEntity) {
            graboidEntity.setBurrowing(false);
            graboidEntity.animationDispatcher.serverBurrow();
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

    private Vec3 pickDestination(E mob) {
        return GraboidBurrowUtils.pickUndergroundDestination(
            mob,
            home,
            homeRadius,
            horizRadius,
            minDepthBelowSurface,
            maxDepthBelowSurface,
            DESTINATION_ATTEMPTS
        );
    }

    private int nextSoundCooldown(E mob) {
        return minSoundInterval + mob.getRandom().nextInt(Math.max(1, maxSoundInterval - minSoundInterval));
    }
}
