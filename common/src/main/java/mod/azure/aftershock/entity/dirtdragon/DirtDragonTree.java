package mod.azure.aftershock.entity.dirtdragon;

import net.minecraft.world.entity.LivingEntity;

import mod.azure.aftershock.ai.actions.dirtdragon.TackleBreachAction;
import mod.azure.aftershock.ai.actions.graboid.UndergroundStalkTargetAction;
import mod.azure.aftershock.ai.actions.graboid.UndergroundWanderAction;
import mod.azure.aftershock.ai.core.AiKeys;
import mod.azure.aftershock.ai.core.BehaviorNode;
import mod.azure.aftershock.ai.core.BehaviorResult;

public class DirtDragonTree {

    public static BehaviorNode<DirtDragonEntity> create() {
        var tackle = new TackleBreachAction<DirtDragonEntity>(
            0.45D,
            0.05D,
            0.2D,
            1.25D,
            6.0F,
            100,
            AiKeys.TACKLE_COOLDOWN,
            80,
            0.15D,
            35,
            3.0D,
            8.0F,
            mob -> mob.animationDispatcher.serverDragging()
        );

        var stalk = new UndergroundStalkTargetAction<DirtDragonEntity>(
            0.22D,
            5.0D,
            5.0D,
            1.0D,
            1.5D,
            10,
            60,
            null,
            30,
            80
        );

        var wander = new UndergroundWanderAction<DirtDragonEntity>(
            0.15D,
            18.0D,
            5.0D,
            6.0D,
            80,
            180,
            10,
            null,
            90,
            180
        );

        return (mob, blackboard, cooldowns) -> {
            var target = blackboard.get(AiKeys.TARGET, LivingEntity.class);
            var hasTarget = target != null && target.isAlive();

            if (hasTarget && cooldowns.ready(AiKeys.TACKLE_COOLDOWN)) {
                return BehaviorResult.run(tackle, 80);
            }

            if (hasTarget) {
                return BehaviorResult.run(stalk, 60);
            }

            return BehaviorResult.run(wander, 10);
        };
    }
}
