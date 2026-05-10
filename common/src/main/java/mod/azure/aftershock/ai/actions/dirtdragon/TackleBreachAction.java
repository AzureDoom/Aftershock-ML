package mod.azure.aftershock.ai.actions.dirtdragon;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

import mod.azure.aftershock.ai.core.*;
import mod.azure.aftershock.ai.util.AiDebugUtils;
import mod.azure.aftershock.ai.util.GraboidBurrowUtils;
import mod.azure.aftershock.entity.dirtdragon.DirtDragonEntity;

public final class TackleBreachAction<E extends Mob> implements Action<E> {

    private enum State {
        APPROACH,
        RIDING,
        DRAGGING_UNDERGROUND,
        DISMOUNTING
    }

    private final double approachSpeed;

    private final double mountHorizontalRange;

    private final double approachDepth;

    private final double launchHorizontalForce;

    private final float attackDamage;

    private final int attackCooldownTicks;

    private final String cooldownKey;

    private final int priority;

    private final double dragUndergroundChance;

    private final int dragUndergroundTicks;

    private final double dragUndergroundDepth;

    private final float dragDamage;

    private final Consumer<E> dragAnimationTrigger;

    private State state;

    private LivingEntity ridingTarget;

    private LivingEntity draggedTarget;

    private int rideTicks;

    private int dragTicks;

    private int biteCooldown;

    private int bitesDone;

    private int bitesNeeded;

    private Vec3 fleeTarget;

    private static final int FIRST_BITE_DELAY = 10;

    public TackleBreachAction(
        double approachSpeed,
        double mountHorizontalRange,
        double approachDepth,
        double launchHorizontalForce,
        float attackDamage,
        int attackCooldownTicks,
        String cooldownKey,
        int priority,
        double dragUndergroundChance,
        int dragUndergroundTicks,
        double dragUndergroundDepth,
        float dragDamage,
        Consumer<E> dragAnimationTrigger
    ) {
        this.approachSpeed = approachSpeed;
        this.mountHorizontalRange = mountHorizontalRange;
        this.approachDepth = approachDepth;
        this.launchHorizontalForce = launchHorizontalForce;
        this.attackDamage = attackDamage;
        this.attackCooldownTicks = attackCooldownTicks;
        this.cooldownKey = cooldownKey;
        this.priority = priority;
        this.dragUndergroundChance = dragUndergroundChance;
        this.dragUndergroundTicks = dragUndergroundTicks;
        this.dragUndergroundDepth = dragUndergroundDepth;
        this.dragDamage = dragDamage;
        this.dragAnimationTrigger = dragAnimationTrigger;
    }

    @Override
    public void start(E mob, Blackboard blackboard, Cooldowns cooldowns) {
        this.state = State.APPROACH;
        this.ridingTarget = null;
        this.draggedTarget = null;
        this.rideTicks = 0;
        this.dragTicks = 0;
        this.biteCooldown = FIRST_BITE_DELAY;
        this.bitesDone = 0;
        this.bitesNeeded = 2 + mob.getRandom().nextInt(2);
        this.fleeTarget = null;

        mob.getNavigation().stop();
        mob.setAggressive(true);
        setBurrowing(mob, true);
    }

    @Override
    public ActionStatus tick(E mob, Blackboard blackboard, Cooldowns cooldowns) {
        var target = blackboard.get(AiKeys.TARGET, LivingEntity.class);

        if (target == null || !target.isAlive() || mob.getHealth() <= 0) {
            return ActionStatus.INTERRUPTED;
        }

        return switch (state) {
            case APPROACH -> tickApproach(mob, target);
            case RIDING -> tickRiding(mob, target);
            case DRAGGING_UNDERGROUND -> tickDraggingUnderground(mob, target, cooldowns);
            case DISMOUNTING -> tickDismounting(mob, cooldowns);
        };
    }

    private ActionStatus tickApproach(E mob, LivingEntity target) {
        setBurrowing(mob, true);

        var horizontalDistanceSqr = horizontalDistanceSqr(mob, target);

        if (horizontalDistanceSqr <= mountHorizontalRange * mountHorizontalRange) {
            if (mob.getRandom().nextDouble() < dragUndergroundChance) {
                startDraggingUnderground(mob, target);
            } else {
                startRidingTarget(mob, target);
            }

            return ActionStatus.RUNNING;
        }

        var surfaceY = GraboidBurrowUtils.getSurfaceY(
            mob,
            target.getX(),
            target.getZ()
        );

        var underFeetTarget = new Vec3(
            target.getX(),
            Math.min(target.getY() - 0.25D, surfaceY - approachDepth),
            target.getZ()
        );

        GraboidBurrowUtils.moveTowardIgnoringBurrowCheck(
            mob,
            underFeetTarget,
            approachSpeed
        );

        GraboidBurrowUtils.rotateToward(mob, underFeetTarget, 20.0F);
        AiDebugUtils.sendParticlePath(mob, mob.position(), underFeetTarget);

        return ActionStatus.RUNNING;
    }

    private void startRidingTarget(E mob, LivingEntity target) {
        setBurrowing(mob, false);
        mob.setNoGravity(false);
        GraboidBurrowUtils.slowDown(mob);

        this.ridingTarget = target;
        this.state = State.RIDING;
        this.rideTicks = 0;
        this.biteCooldown = FIRST_BITE_DELAY;

        mob.startRiding(target, true);
    }

    private void startDraggingUnderground(E mob, LivingEntity target) {
        this.state = State.DRAGGING_UNDERGROUND;
        this.draggedTarget = target;
        this.dragTicks = 0;

        mob.stopRiding();
        mob.getNavigation().stop();
        mob.setNoGravity(true);
        target.setNoGravity(true);

        setBurrowing(mob, true);
        GraboidBurrowUtils.slowDown(mob);

        var surfaceY = GraboidBurrowUtils.getSurfaceY(
            mob,
            target.getX(),
            target.getZ()
        );

        spawnSurfaceDirtParticles(mob, target, surfaceY);

        if (dragAnimationTrigger != null) {
            dragAnimationTrigger.accept(mob);
        }
    }

    private void spawnSurfaceDirtParticles(E mob, LivingEntity target, double surfaceY) {
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        var particleY = surfaceY + 0.08D;

        var groundPos = net.minecraft.core.BlockPos.containing(
            target.getX(),
            surfaceY - 1.0D,
            target.getZ()
        );

        var groundState = serverLevel.getBlockState(groundPos);

        if (groundState.isAir()) {
            groundPos = net.minecraft.core.BlockPos.containing(
                target.getX(),
                surfaceY,
                target.getZ()
            );
            groundState = serverLevel.getBlockState(groundPos);
        }

        serverLevel.sendParticles(
            new BlockParticleOption(ParticleTypes.BLOCK, groundState),
            target.getX(),
            particleY,
            target.getZ(),
            35,
            0.45D,
            0.08D,
            0.45D,
            0.08D
        );
    }

    private ActionStatus tickDraggingUnderground(E mob, LivingEntity target, Cooldowns cooldowns) {
        dragTicks++;

        if (draggedTarget == null || !draggedTarget.isAlive()) {
            finishDraggingUnderground(mob, target, cooldowns);
            return ActionStatus.SUCCESS;
        }

        setBurrowing(mob, true);

        var surfaceY = GraboidBurrowUtils.getSurfaceY(
            mob,
            draggedTarget.getX(),
            draggedTarget.getZ()
        );

        if (dragTicks % 3 == 0) {
            spawnSurfaceDirtParticles(mob, draggedTarget, surfaceY);
        }

        double progress = Math.min(1.0D, dragTicks / (double) dragUndergroundTicks);
        double desiredY = surfaceY - dragUndergroundDepth * progress;

        var dragPosition = new Vec3(
            draggedTarget.getX(),
            desiredY,
            draggedTarget.getZ()
        );

        mob.setPos(dragPosition.x, dragPosition.y, dragPosition.z);
        draggedTarget.setPos(dragPosition.x, dragPosition.y + 0.15D, dragPosition.z);

        mob.setDeltaMovement(0.0D, -0.08D, 0.0D);
        draggedTarget.setDeltaMovement(0.0D, -0.08D, 0.0D);

        mob.hasImpulse = true;
        draggedTarget.hasImpulse = true;
        draggedTarget.hurtMarked = true;

        GraboidBurrowUtils.rotateToward(mob, draggedTarget.position(), 20.0F);

        if (dragTicks >= dragUndergroundTicks) {
            draggedTarget.hurt(mob.damageSources().mobAttack(mob), dragDamage);
            finishDraggingUnderground(mob, draggedTarget, cooldowns);
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    private void finishDraggingUnderground(E mob, LivingEntity target, Cooldowns cooldowns) {
        mob.setNoGravity(false);

        if (draggedTarget != null) {
            draggedTarget.setNoGravity(false);
            draggedTarget.hurtMarked = true;
        }

        draggedTarget = null;

        setBurrowing(mob, true);
        finishAttack(mob, cooldowns);
    }

    private ActionStatus tickRiding(E mob, LivingEntity target) {
        rideTicks++;

        if (ridingTarget == null || !ridingTarget.isAlive()) {
            startDismounting(mob, target);
            return ActionStatus.RUNNING;
        }

        if (!mob.isPassenger() || mob.getVehicle() != ridingTarget) {
            mob.startRiding(ridingTarget, true);
        }

        biteCooldown--;

        if (biteCooldown <= 0) {
            applyBite(mob, ridingTarget);
            bitesDone++;
            biteCooldown = 12;
        }

        if (bitesDone >= bitesNeeded || rideTicks >= 100) {
            startDismounting(mob, ridingTarget);
            return ActionStatus.RUNNING;
        }

        return ActionStatus.RUNNING;
    }

    private void startDismounting(E mob, LivingEntity target) {
        mob.stopRiding();

        this.state = State.DISMOUNTING;
        this.rideTicks = 0;

        setBurrowing(mob, true);

        var away = mob.position().subtract(target.position());
        var hLen = Math.sqrt(away.x * away.x + away.z * away.z);

        var normX = hLen > 0.001D ? away.x / hLen : mob.getRandom().nextDouble() - 0.5D;
        var normZ = hLen > 0.001D ? away.z / hLen : mob.getRandom().nextDouble() - 0.5D;

        var surfaceY = GraboidBurrowUtils.getSurfaceY(mob, mob.getX(), mob.getZ());

        this.fleeTarget = new Vec3(
            mob.getX() + normX * 8.0D,
            surfaceY - approachDepth,
            mob.getZ() + normZ * 8.0D
        );

        mob.setDeltaMovement(
            normX * launchHorizontalForce,
            0.15D,
            normZ * launchHorizontalForce
        );
        mob.hasImpulse = true;
    }

    private ActionStatus tickDismounting(E mob, Cooldowns cooldowns) {
        rideTicks++;

        setBurrowing(mob, true);

        if (fleeTarget != null) {
            GraboidBurrowUtils.moveTowardIgnoringBurrowCheck(
                mob,
                fleeTarget,
                approachSpeed
            );

            GraboidBurrowUtils.rotateToward(mob, fleeTarget, 20.0F);
            AiDebugUtils.sendParticlePath(mob, mob.position(), fleeTarget);
        }

        if (rideTicks >= 25) {
            finishAttack(mob, cooldowns);
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    @Override
    public void stop(E mob, Blackboard blackboard, ActionStatus reason) {
        if (mob.isPassenger()) {
            mob.stopRiding();
        }

        if (draggedTarget != null) {
            draggedTarget.setNoGravity(false);
            draggedTarget.hurtMarked = true;
            draggedTarget = null;
        }

        mob.setNoGravity(false);
        mob.setAggressive(false);
        mob.setXRot(0.0F);

        GraboidBurrowUtils.slowDown(mob);

        if (reason != ActionStatus.SUCCESS) {
            setBurrowing(mob, false);
        }
    }

    @Override
    public boolean isInterruptible() {
        return state == State.APPROACH;
    }

    @Override
    public int priority() {
        return priority;
    }

    private void finishAttack(E mob, Cooldowns cooldowns) {
        if (cooldownKey != null && attackCooldownTicks > 0) {
            cooldowns.set(cooldownKey, attackCooldownTicks);
        }

        setBurrowing(mob, true);
    }

    private void applyBite(E mob, LivingEntity target) {
        target.hurt(mob.damageSources().mobAttack(mob), attackDamage);
        target.hurtMarked = true;
    }

    private static void setBurrowing(Mob mob, boolean burrowing) {
        if (mob instanceof DirtDragonEntity dirtDragon) {
            dirtDragon.setBurrowing(burrowing);
        }
    }

    private static double horizontalDistanceSqr(Mob mob, LivingEntity target) {
        var dx = mob.getX() - target.getX();
        var dz = mob.getZ() - target.getZ();
        return dx * dx + dz * dz;
    }
}
