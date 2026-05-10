package mod.azure.aftershock.ai.actions.blaster;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

import mod.azure.aftershock.ai.core.*;
import mod.azure.aftershock.ai.util.AiDebugUtils;
import mod.azure.aftershock.ai.util.MovementUtils;
import mod.azure.aftershock.entity.blaster.BlasterEntity;

public class EatCookedFoodAction implements Action<BlasterEntity> {

    private final double searchRange;

    private final double eatDistanceSqr;

    private final double speed;

    private final int priority;

    private final int animationTicks;

    private final Consumer<BlasterEntity> eatAnimation;

    private final int[] steerBias = { 0 };

    private ItemEntity food;

    private int eatTicks;

    private boolean eatingStarted;

    private boolean consumed;

    public EatCookedFoodAction(
        double searchRange,
        double eatDistance,
        double speed,
        int priority,
        int animationTicks,
        Consumer<BlasterEntity> eatAnimation
    ) {
        this.searchRange = searchRange;
        this.eatDistanceSqr = eatDistance * eatDistance;
        this.speed = speed;
        this.priority = priority;
        this.animationTicks = animationTicks;
        this.eatAnimation = eatAnimation;
    }

    @Override
    public void start(BlasterEntity mob, Blackboard blackboard, Cooldowns cooldowns) {
        this.food = findNearestCookedFood(mob);
        this.eatTicks = 0;
        this.eatingStarted = false;
        this.consumed = false;
    }

    @Override
    public ActionStatus tick(BlasterEntity mob, Blackboard blackboard, Cooldowns cooldowns) {
        if (!this.eatingStarted) {
            if (this.food == null || !this.food.isAlive() || this.food.getItem().isEmpty()) {
                return ActionStatus.SUCCESS;
            }

            if (mob.distanceToSqr(this.food) > this.eatDistanceSqr) {
                moveTowardFood(mob);
                return ActionStatus.RUNNING;
            }

            this.eatingStarted = true;
            this.eatTicks = 0;

            mob.setDeltaMovement(
                0.0D,
                mob.getDeltaMovement().y,
                0.0D
            );
            mob.hasImpulse = true;

            this.eatAnimation.accept(mob);
        }

        this.eatTicks++;

        mob.setDeltaMovement(
            0.0D,
            mob.getDeltaMovement().y,
            0.0D
        );
        mob.hasImpulse = true;

        if (!this.consumed && this.eatTicks >= 20) {
            if (this.food != null && this.food.isAlive() && !this.food.getItem().isEmpty()) {
                consumeOneFoodItem(mob, this.food);
                mob.playSound(SoundEvents.GENERIC_EAT);
                mob.incrementCookedFoodEaten();
            }

            this.consumed = true;
        }

        if (this.eatTicks >= animationTicks) {
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    @Override
    public void stop(BlasterEntity mob, Blackboard blackboard, ActionStatus reason) {
        mob.setDeltaMovement(
            0.0D,
            mob.getDeltaMovement().y,
            0.0D
        );
        mob.hasImpulse = true;

        this.food = null;
        this.eatTicks = 0;
        this.eatingStarted = false;
        this.consumed = false;
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    @Override
    public int priority() {
        return this.priority;
    }

    public boolean canStart(BlasterEntity mob) {
        return findNearestCookedFood(mob) != null;
    }

    private void moveTowardFood(BlasterEntity mob) {
        var destination = this.food.blockPosition();
        var destVec = Vec3.atBottomCenterOf(destination);
        var direction = destVec.subtract(mob.position());

        var horizontal = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontal.lengthSqr() < 0.0001D) {
            mob.setDeltaMovement(
                mob.getDeltaMovement().x * 0.4D,
                mob.getDeltaMovement().y,
                mob.getDeltaMovement().z * 0.4D
            );
            return;
        }

        var movement = MovementUtils.steerAwayFromDangerEntities(
            mob,
            horizontal.normalize().scale(speed)
        );

        var safe = MovementUtils.findSafeMovement(mob, movement, steerBias);

        if (safe.equals(Vec3.ZERO)) {
            mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y, 0.0D);
            mob.hasImpulse = false;
            return;
        }

        mob.setDeltaMovement(safe.x, mob.getDeltaMovement().y, safe.z);
        mob.hasImpulse = true;

        var dx = destination.getX() + 0.5D - mob.getX();
        var dz = destination.getZ() + 0.5D - mob.getZ();
        var yaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;

        mob.setYRot(yaw);
        mob.yBodyRot = yaw;
        mob.yHeadRot = yaw;

        AiDebugUtils.sendParticlePath(
            mob,
            mob.position(),
            destVec
        );
    }

    private ItemEntity findNearestCookedFood(BlasterEntity mob) {
        var level = mob.level();

        var items = level.getEntitiesOfClass(
            ItemEntity.class,
            mob.getBoundingBox().inflate(this.searchRange),
            item -> item.isAlive()
                && !item.getItem().isEmpty()
                && isCookedFood(item.getItem())
        );

        ItemEntity nearest = null;
        var nearestDistance = Double.MAX_VALUE;

        for (var item : items) {
            double distance = mob.distanceToSqr(item);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = item;
            }
        }

        return nearest;
    }

    private void consumeOneFoodItem(BlasterEntity mob, ItemEntity itemEntity) {
        if (mob.level().isClientSide()) {
            return;
        }

        var stack = itemEntity.getItem();

        stack.shrink(1);

        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }
    }

    private boolean isCookedFood(ItemStack stack) {
        return stack.is(Items.COOKED_BEEF)
            || stack.is(Items.COOKED_CHICKEN)
            || stack.is(Items.COOKED_COD)
            || stack.is(Items.COOKED_MUTTON)
            || stack.is(Items.COOKED_PORKCHOP)
            || stack.is(Items.COOKED_RABBIT)
            || stack.is(Items.COOKED_SALMON)
            || stack.is(Items.BAKED_POTATO);
    }
}
