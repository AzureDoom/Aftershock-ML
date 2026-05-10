package mod.azure.aftershock.ai.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mod.azure.aftershock.ai.core.AiKeys;
import mod.azure.aftershock.ai.core.Blackboard;

public final class VibrationTargetSelector<E extends Mob> implements TargetSelector<E> {

    private final double range;

    private final Map<UUID, Vec3> lastPositions = new HashMap<>();

    public VibrationTargetSelector(double range) {
        this.range = range;
    }

    @Override
    public LivingEntity findTarget(E mob, Blackboard blackboard) {
        var current = blackboard.get(AiKeys.TARGET, LivingEntity.class);

        if (isValidMovingTarget(mob, current)) {
            return current;
        }

        var closest = mob.level()
            .getEntitiesOfClass(
                LivingEntity.class,
                mob.getBoundingBox().inflate(range),
                entity -> isValidMovingTarget(mob, entity)
            )
            .stream()
            .min(Comparator.comparingDouble(mob::distanceToSqr))
            .orElse(null);

        cleanupOldPositions(mob);

        return closest;
    }

    private boolean isValidMovingTarget(E mob, LivingEntity entity) {
        return TargetingUtils.validSoundTarget(mob).test(entity)
            && mob.distanceToSqr(entity) <= range * range
            && isMovingByTrackedPosition(entity);
    }

    private boolean isMovingByTrackedPosition(LivingEntity entity) {
        if (entity.isSteppingCarefully()) {
            return false;
        }

        var id = entity.getUUID();
        var currentPos = entity.position();
        var previousPos = lastPositions.put(id, currentPos);

        if (previousPos == null) {
            return false;
        }

        var dx = currentPos.x - previousPos.x;
        var dz = currentPos.z - previousPos.z;
        var movementSqr = dx * dx + dz * dz;

        return movementSqr >= 0.0004D;
    }

    private void cleanupOldPositions(E mob) {
        lastPositions.entrySet().removeIf(entry -> {
            var entity = mob.level().getPlayerByUUID(entry.getKey());

            if (entity != null) {
                return mob.distanceToSqr(entity) > range * range * 4.0D;
            }

            return false;
        });
    }
}
