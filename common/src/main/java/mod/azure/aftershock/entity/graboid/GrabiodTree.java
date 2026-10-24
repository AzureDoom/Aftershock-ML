package mod.azure.aftershock.entity.graboid;

import net.minecraft.world.entity.LivingEntity;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.ai.actions.graboid.SurfaceBreachAttackAction;
import mod.azure.aftershock.ai.actions.graboid.UndergroundStalkTargetAction;
import mod.azure.aftershock.ai.actions.graboid.UndergroundWanderAction;
import mod.azure.aftershock.ai.core.AiKeys;
import mod.azure.aftershock.ai.core.BehaviorNode;
import mod.azure.aftershock.ai.core.BehaviorResult;
import mod.azure.aftershock.ai.util.GraboidBurrowUtils;
import mod.azure.aftershock.registry.SoundRegistry;

public class GrabiodTree {

    public static BehaviorNode<GraboidEntity> create() {
        var breach = new SurfaceBreachAttackAction<GraboidEntity>(
                3.0D,
                CommonMod.getConfig().entityConfigs.graboid_damage,
                0.0F,
                CommonMod.getConfig().entityConfigs.graboid_damage * 0.4F,
                5.0D,
                100,
                SoundRegistry.GRABOID_ATTACKING.get(),
                0.90D,
                0.15D,
                0.15D,
                graboid -> graboid.animationDispatcher.serverGrabOne(),
                graboid -> graboid.animationDispatcher.serverErupting(),
                graboid -> graboid.animationDispatcher.serverBite()
        );

        var stalk = new UndergroundStalkTargetAction<GraboidEntity>(
                0.22D,
                5.0D,
                5.0D,
                0.5D,
                1.5D,
                10,
                60,
                null,
                30,
                80
        );

        var wander = new UndergroundWanderAction<GraboidEntity>(
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

            if (hasTarget && shouldBreach(mob, target)) {
                return BehaviorResult.run(breach, 100);
            }

            if (hasTarget) {
                return BehaviorResult.run(stalk, 60);
            }

            return BehaviorResult.run(wander, 10);
        };
    }

    private static boolean shouldBreach(GraboidEntity mob, LivingEntity target) {
        if (!mob.isBurrowing()) {
            return false;
        }

        var horizontalDistanceSqr = horizontalDistanceSqr(
                mob.getX(),
                mob.getZ(),
                target.getX(),
                target.getZ()
        );

        if (horizontalDistanceSqr > 0.5D * 0.5D) {
            return false;
        }

        var surfaceY = GraboidBurrowUtils.getSurfaceY(mob, mob.getX(), mob.getZ());
        var depth = surfaceY - mob.getY();

        if (depth < 1.25D) {
            return false;
        }

        return !(depth > 7D);
    }

    private static double horizontalDistanceSqr(double x1, double z1, double x2, double z2) {
        var dx = x1 - x2;
        var dz = z1 - z2;
        return dx * dx + dz * dz;
    }
}