package mod.azure.aftershock.entity.blaster;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

import mod.azure.aftershock.ai.actions.*;
import mod.azure.aftershock.ai.actions.blaster.*;
import mod.azure.aftershock.ai.core.*;

public class BlasterTree {

    public static BehaviorNode<BlasterEntity> create() {
        var idle = new IdleAction<BlasterEntity>(30, 80, 5);

        var eatCookedFood = new EatCookedFoodAction(
            10.0D,
            1.5D,
            0.15D,
            95,
            86,
            mob -> mob.animationDispatcher.serverEating()
        );

        var breakHeatBlock = new BreakHeatBlockAction<BlasterEntity>(
            12.0D,
            1.75D,
            0.15D,
            85,
            18,
            8,
            15,
            mob -> mob.animationDispatcher.serverBite()
        );

        var layEgg = new LayBlasterEggAction(
            100,
            mob -> mob.animationDispatcher.serverLayEgg(),
            mob -> mob.animationDispatcher.serverStandUp()
        );

        var takeOff = new TakeOffAction<BlasterEntity>(
            60,
            36,
            0.72D,
            0.55D,
            AiKeys.TAKEOFF_COOLDOWN,
            100,
            100,
            mob -> mob.animationDispatcher.serverTakeOff()
        );

        var moveToTarget = new MoveToTargetAction<BlasterEntity>(
            0.1D,
            0.28D,
            40
        );

        var moveToDestination = new MoveToDestinationAction<BlasterEntity>(
            1.5D,
            0.25D,
            30,
            5.0D,
            1.0D,
            0.55D,
            0.45D
        );

        var meleeAttack = new TimedAttackAction<BlasterEntity>(
            AiKeys.MELEE_COOLDOWN,
            30,
            18,
            8,
            80,
            mob -> mob.animationDispatcher.serverBite()
        );

        var flyToTarget = new FlyToTargetAction<BlasterEntity>(
            8.0D,
            0.38D,
            7.0D,
            50
        );

        var aerialReposition = new AerialRepositionAction<BlasterEntity>(
            0.32D,
            7.0D,
            0.35D,
            45
        );

        var aerialSwoop = new AerialSwoopAction<BlasterEntity>(
            45,
            24,
            0.75D,
            0.45D,
            80,
            90,
            mob -> mob.animationDispatcher.serverGliding(),
            (mob, target) -> false
        );

        var aerialWander = new AerialWanderAction<BlasterEntity>(
            0.25D,
            18.0D,
            5.0D,
            10.0D,
            40,
            100,
            10
        );

        return (mob, blackboard, cooldowns) -> {

            Action<BlasterEntity> next = selectNextAction(
                mob,
                blackboard,
                cooldowns,
                idle,
                takeOff,
                moveToTarget,
                moveToDestination,
                meleeAttack,
                flyToTarget,
                aerialReposition,
                aerialSwoop,
                aerialWander,
                eatCookedFood,
                breakHeatBlock,
                layEgg
            );

            if (next == null) {
                return BehaviorResult.none();
            }

            return BehaviorResult.run(next, next.priority());
        };
    }

    private static Action<BlasterEntity> selectNextAction(
        BlasterEntity mob,
        Blackboard blackboard,
        Cooldowns cooldowns,
        Action<BlasterEntity> idle,
        Action<BlasterEntity> takeOff,
        Action<BlasterEntity> moveToTarget,
        Action<BlasterEntity> moveToDestination,
        Action<BlasterEntity> meleeAttack,
        Action<BlasterEntity> flyToTarget,
        Action<BlasterEntity> aerialReposition,
        Action<BlasterEntity> aerialSwoop,
        Action<BlasterEntity> aerialWander,
        EatCookedFoodAction eatCookedFood,
        BreakHeatBlockAction<BlasterEntity> breakHeatBlock,
        Action<BlasterEntity> layEgg
    ) {
        var target = blackboard.get(AiKeys.TARGET, LivingEntity.class);
        var destination = blackboard.get(AiKeys.DESTINATION, BlockPos.class);

        var hasTarget = target != null && target.isAlive();
        var takingOff = blackboard.get(AiKeys.BLASTER_TAKING_OFF, Boolean.class);
        var inFlight = blackboard.get(AiKeys.BLASTER_IN_FLIGHT, Boolean.class);

        if (Boolean.TRUE.equals(takingOff)) {
            return takeOff;
        }

        if (mob.isReadyToLayEgg()) {
            return layEgg;
        }

        if (mob.onGround() && eatCookedFood.canStart(mob)) {
            return eatCookedFood;
        }

        if (mob.onGround() && breakHeatBlock.canStart(mob)) {
            return breakHeatBlock;
        }

        if (mob.onGround()) {
            mob.setNoGravity(false);

            if (hasTarget) {
                double distanceSqr = mob.distanceToSqr(target);

                if (distanceSqr <= 1.44D && cooldowns.ready(AiKeys.MELEE_COOLDOWN)) {
                    return meleeAttack;
                }

                if (cooldowns.ready(AiKeys.TAKEOFF_COOLDOWN) && distanceSqr > 144.0D) {
                    return takeOff;
                }

                return moveToTarget;
            }

            if (cooldowns.ready(AiKeys.TAKEOFF_COOLDOWN) && mob.getRandom().nextInt(200) == 0) {
                return takeOff;
            }

            if (destination != null) {
                return moveToDestination;
            }

            return idle;
        }

        if (!Boolean.TRUE.equals(inFlight)) {
            mob.setNoGravity(false);

            if (hasTarget) {
                return moveToTarget;
            }

            return idle;
        }

        mob.setNoGravity(true);

        if (hasTarget) {
            double distanceSqr = mob.distanceToSqr(target);

            if (
                cooldowns.ready(AiKeys.SWOOP_COOLDOWN)
                    && distanceSqr <= 400.0D
                    && mob.getY() > target.getY() + 3.0D
            ) {
                return aerialSwoop;
            }

            if (distanceSqr > 100.0D) {
                return flyToTarget;
            }

            return aerialReposition;
        }

        if (cooldowns.ready(AiKeys.LAND_COOLDOWN) && mob.getRandom().nextInt(300) == 0) {
            blackboard.set(AiKeys.BLASTER_IN_FLIGHT, false);
            mob.setNoGravity(false);
            cooldowns.set(AiKeys.LAND_COOLDOWN, 200);
            cooldowns.set(AiKeys.TAKEOFF_COOLDOWN, 200);
            return idle;
        }

        return aerialWander;
    }
}
