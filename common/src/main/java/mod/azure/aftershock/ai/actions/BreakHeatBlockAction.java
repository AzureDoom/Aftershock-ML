package mod.azure.aftershock.ai.actions;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

import mod.azure.aftershock.ai.core.*;
import mod.azure.aftershock.ai.util.AiDebugUtils;
import mod.azure.aftershock.ai.util.MovementUtils;
import mod.azure.aftershock.util.ModTags;

public class BreakHeatBlockAction<T extends Mob> implements Action<T> {

    private final double searchRange;

    private final double breakDistanceSqr;

    private final double speed;

    private final int priority;

    private final int hitIntervalTicks;

    private final int hitApplyTick;

    private final Consumer<T> biteAnimation;

    private final int[] steerBias = { 0 };

    private BlockPos targetBlock;

    private int requiredHits;

    private int hitsDone;

    private int hitTicks;

    private final int hitCooldownTicks;

    private int cooldownTicks;

    private boolean swingHitApplied;

    public BreakHeatBlockAction(
        double searchRange,
        double breakDistance,
        double speed,
        int priority,
        int hitIntervalTicks,
        int hitApplyTick,
        int hitCooldownTicks,
        Consumer<T> biteAnimation
    ) {
        this.searchRange = searchRange;
        this.breakDistanceSqr = breakDistance * breakDistance;
        this.speed = speed;
        this.priority = priority;
        this.hitIntervalTicks = hitIntervalTicks;
        this.hitApplyTick = hitApplyTick;
        this.biteAnimation = biteAnimation;
        this.hitCooldownTicks = hitCooldownTicks;
    }

    @Override
    public void start(T mob, Blackboard blackboard, Cooldowns cooldowns) {
        this.targetBlock = findNearestHeatBlock(mob);
        this.requiredHits = 2 + mob.getRandom().nextInt(4);
        this.hitsDone = 0;
        this.hitTicks = 0;
        this.cooldownTicks = 0;
        this.swingHitApplied = false;
    }

    @Override
    public ActionStatus tick(T mob, Blackboard blackboard, Cooldowns cooldowns) {
        if (mob.isDeadOrDying() || mob.getHealth() <= 0) {
            return ActionStatus.FAILURE;
        }
        if (this.targetBlock == null || !isHeatBlock(mob, this.targetBlock)) {
            return ActionStatus.SUCCESS;
        }

        var targetVec = Vec3.atCenterOf(this.targetBlock);

        if (mob.position().distanceToSqr(targetVec) > this.breakDistanceSqr) {
            moveTowardBlock(mob, this.targetBlock);
            return ActionStatus.RUNNING;
        }

        faceBlock(mob, this.targetBlock);
        stopHorizontalMovement(mob);

        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            return ActionStatus.RUNNING;
        }

        if (this.hitTicks == 0) {
            this.swingHitApplied = false;
            this.biteAnimation.accept(mob);
        }

        if (!this.swingHitApplied && this.hitTicks == this.hitApplyTick) {
            this.swingHitApplied = true;

            applyHit(mob);

            if (this.hitsDone >= this.requiredHits) {
                breakBlock(mob);
                return ActionStatus.SUCCESS;
            }

            this.cooldownTicks = this.hitCooldownTicks;
        }

        this.hitTicks++;

        if (this.hitTicks >= this.hitIntervalTicks) {
            this.hitTicks = 0;
        }

        return ActionStatus.RUNNING;
    }

    @Override
    public void stop(T mob, Blackboard blackboard, ActionStatus reason) {
        stopHorizontalMovement(mob);
        this.targetBlock = null;
        this.requiredHits = 0;
        this.hitsDone = 0;
        this.hitTicks = 0;
        this.cooldownTicks = 0;
        this.swingHitApplied = false;
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    @Override
    public int priority() {
        return this.priority;
    }

    public boolean canStart(T mob) {
        return findNearestHeatBlock(mob) != null;
    }

    private void applyHit(T mob) {
        if (this.targetBlock == null)
            return;
        if (mob.level().isClientSide())
            return;
        if (!isHeatBlock(mob, this.targetBlock))
            return;

        this.hitsDone++;

        var state = mob.level().getBlockState(this.targetBlock);
        mob.level().levelEvent(2001, this.targetBlock, Block.getId(state));
        mob.playSound(SoundEvents.STONE_HIT);
    }

    private void breakBlock(T mob) {
        if (this.targetBlock == null)
            return;
        if (mob.level().isClientSide())
            return;
        if (!isHeatBlock(mob, this.targetBlock))
            return;

        mob.level().destroyBlock(this.targetBlock, false, mob);
        mob.playSound(SoundEvents.STONE_BREAK);
    }

    private BlockPos findNearestHeatBlock(T mob) {
        var origin = mob.blockPosition();
        var range = (int) Math.ceil(this.searchRange);

        BlockPos nearest = null;
        var nearestDist = Double.MAX_VALUE;

        for (
            var pos : BlockPos.betweenClosed(
                origin.offset(-range, -2, -range),
                origin.offset(range, 2, range)
            )
        ) {
            if (!isHeatBlock(mob, pos)) {
                continue;
            }

            double dist = mob.position().distanceToSqr(Vec3.atCenterOf(pos));

            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = pos.immutable();
            }
        }

        return nearest;
    }

    private boolean isHeatBlock(T mob, BlockPos pos) {
        return mob.level().getBlockState(pos).is(ModTags.HEAT_BLOCKS);
    }

    private void moveTowardBlock(T mob, BlockPos blockPos) {
        var destVec = Vec3.atCenterOf(blockPos);
        var horizontal = new Vec3(
            destVec.x - mob.getX(),
            0.0D,
            destVec.z - mob.getZ()
        );

        if (horizontal.lengthSqr() < 0.0001D) {
            stopHorizontalMovement(mob);
            return;
        }

        var movement = MovementUtils.steerAwayFromDangerEntities(
            mob,
            horizontal.normalize().scale(this.speed)
        );

        var safe = MovementUtils.findSafeMovement(mob, movement, this.steerBias);

        if (safe.equals(Vec3.ZERO)) {
            stopHorizontalMovement(mob);
            return;
        }

        mob.setDeltaMovement(safe.x, mob.getDeltaMovement().y, safe.z);
        mob.hasImpulse = true;

        faceBlock(mob, blockPos);

        AiDebugUtils.sendParticlePath(mob, mob.position(), destVec);
    }

    private void faceBlock(T mob, BlockPos blockPos) {
        var dx = blockPos.getX() + 0.5D - mob.getX();
        var dz = blockPos.getZ() + 0.5D - mob.getZ();
        var yaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;

        mob.setYRot(yaw);
        mob.yBodyRot = yaw;
        mob.yHeadRot = yaw;
    }

    private void stopHorizontalMovement(T mob) {
        mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y, 0.0D);
        mob.hasImpulse = true;
    }
}
