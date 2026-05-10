package mod.azure.aftershock.entity.shrieker;

import net.minecraft.core.BlockPos;

import mod.azure.aftershock.ai.actions.*;
import mod.azure.aftershock.ai.actions.shrieker.EatCookedFoodShriekerAction;
import mod.azure.aftershock.ai.actions.shrieker.PukeBirthAction;
import mod.azure.aftershock.ai.actions.shrieker.StatisAction;
import mod.azure.aftershock.ai.core.AiKeys;
import mod.azure.aftershock.ai.core.BehaviorNode;
import mod.azure.aftershock.ai.core.BehaviorResult;

public class ShriekerTree {

    public static BehaviorNode<ShriekerEntity> create() {
        var statisAction = new StatisAction(47000f);

        var pukeAction = new PukeBirthAction(
            37,
            2,
            mob -> mob.animationDispatcher.serverPuke()
        );

        var attackAction = new TimedAttackAction<ShriekerEntity>(
            AiKeys.BITE_COOLDOWN,
            20,
            20,
            5,
            3,
            mob -> mob.animationDispatcher.serverBite()
        );

        var moveToTarget = new MoveToTargetAction<ShriekerEntity>(
            1.0,
            0.39,
            4
        );

        var eatAction = new EatCookedFoodShriekerAction(
            16.0,
            2.0,
            0.28,
            5,
            28,
            mob -> mob.animationDispatcher.serverEating()
        );

        var breakHeatBlockAction = new BreakHeatBlockAction<ShriekerEntity>(
            12.0D,
            2.0D,
            0.28D,
            5,
            20,
            5,
            15,
            mob -> mob.animationDispatcher.serverBite()
        );

        var wanderAction = new MoveToDestinationAction<ShriekerEntity>(
            1.5D,
            0.25D,
            6,
            5.0D,
            1.0D,
            0.55D,
            0.45D
        );

        var idleAction = new IdleAction<ShriekerEntity>(
            40,
            100,
            6
        );

        return (mob, blackboard, cooldowns) -> {
            var destination = blackboard.get(AiKeys.DESTINATION, BlockPos.class);
            if (statisAction.shouldActivate(mob)) {
                return BehaviorResult.run(statisAction, 1);
            }

            if (mob.isReadyToLayEgg()) {
                return BehaviorResult.run(pukeAction, 2);
            }

            var target = blackboard.get(AiKeys.TARGET, net.minecraft.world.entity.LivingEntity.class);
            if (target != null && target.isAlive()) {
                var distSqr = mob.distanceToSqr(target);
                if (distSqr <= 6.25 && !cooldowns.isOnCooldown(AiKeys.BITE_COOLDOWN)) {
                    return BehaviorResult.run(attackAction, 3);
                }
                return BehaviorResult.run(moveToTarget, 4);
            }

            if (eatAction.canStart(mob)) {
                return BehaviorResult.run(eatAction, 5);
            }

            if (breakHeatBlockAction.canStart(mob)) {
                return BehaviorResult.run(breakHeatBlockAction, 5);
            }

            if (destination != null) {
                return BehaviorResult.run(wanderAction, 6);
            }

            return BehaviorResult.run(idleAction, 6);
        };
    }
}
