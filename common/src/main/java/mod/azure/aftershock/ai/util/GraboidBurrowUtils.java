package mod.azure.aftershock.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import mod.azure.aftershock.util.ModTags;

public final class GraboidBurrowUtils {

    private GraboidBurrowUtils() {}

    public static double getSurfaceY(Mob mob, double x, double z) {
        return mob.level()
            .getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (int) Math.floor(x),
                (int) Math.floor(z)
            );
    }

    @SuppressWarnings("deprecation")
    public static boolean isBurrowable(Mob mob, BlockPos pos) {
        var level = mob.level();
        var state = level.getBlockState(pos);

        if (state.isAir() || state.liquid()) {
            return false;
        }

        if (state.getDestroySpeed(level, pos) < 0) {
            return false;
        }

        return state.is(ModTags.GRABOID_BURROWABLE)
            || !(state.is(Blocks.AIR)
                || state.is(Blocks.LAVA));
    }

    public static boolean canOccupyBurrowPosition(Mob mob, Vec3 pos) {
        var center = BlockPos.containing(pos);
        var above = center.above();

        if (!isBurrowable(mob, center)) {
            return false;
        }

        var aboveState = mob.level().getBlockState(above);

        return aboveState.isAir() || isBurrowable(mob, above);
    }

    public static Vec3 pickUndergroundDestination(
        Mob mob,
        Vec3 home,
        double homeRadius,
        double horizRadius,
        double minDepthBelowSurface,
        double maxDepthBelowSurface,
        int attempts
    ) {
        var originX = home != null ? home.x : mob.getX();
        var originZ = home != null ? home.z : mob.getZ();
        var radius = home != null ? Math.min(horizRadius, homeRadius) : horizRadius;

        for (var i = 0; i < attempts; i++) {
            var xOff = (mob.getRandom().nextDouble() * 2.0D - 1.0D) * radius;
            var zOff = (mob.getRandom().nextDouble() * 2.0D - 1.0D) * radius;

            var x = originX + xOff;
            var z = originZ + zOff;

            var surfaceY = getSurfaceY(mob, x, z);

            var minY = surfaceY - maxDepthBelowSurface;
            var maxY = surfaceY - minDepthBelowSurface;

            var candidateY = Math.clamp(
                mob.getY() + (mob.getRandom().nextDouble() * 2.0D - 1.0D) * 1.25D,
                minY,
                maxY
            );

            var candidate = new Vec3(x, candidateY, z);

            if (canOccupyBurrowPosition(mob, candidate)) {
                return candidate;
            }
        }

        var angle = mob.getRandom().nextDouble() * Math.PI * 2.0D;
        var distance = Math.max(2.0D, horizRadius * 0.35D);

        var x = mob.getX() + Math.cos(angle) * distance;
        var z = mob.getZ() + Math.sin(angle) * distance;

        var currentSurfaceY = getSurfaceY(mob, mob.getX(), mob.getZ());
        var minY = currentSurfaceY - maxDepthBelowSurface;
        var maxY = currentSurfaceY - minDepthBelowSurface;
        var y = Math.clamp(mob.getY(), minY, maxY);

        return new Vec3(x, y, z);
    }

    public static boolean moveTowardBurrowableOnly(Mob mob, Vec3 destination, double speed) {
        var delta = destination.subtract(mob.position());
        var len = delta.length();

        if (len <= 0.1D) {
            return true;
        }

        var velocity = delta.scale(speed / len);
        var nextPos = mob.position().add(velocity.scale(1.5D));

        if (!canOccupyBurrowPosition(mob, nextPos)) {
            slowDown(mob);
            return false;
        }

        mob.setDeltaMovement(velocity);
        mob.move(MoverType.SELF, mob.getDeltaMovement());
        mob.hasImpulse = true;

        var dx = delta.x;
        var dy = delta.y;
        var dz = delta.z;
        var horiz = Math.sqrt(dx * dx + dz * dz);

        rotateToward(mob, destination, 12.0F);

        var targetPitch = (float) -(Math.atan2(dy, horiz) * (180.0D / Math.PI));
        mob.setXRot(mob.getXRot() + (targetPitch - mob.getXRot()) * 0.15F);

        return true;
    }

    public static void slowDown(Mob mob) {
        var vel = mob.getDeltaMovement();
        mob.setDeltaMovement(vel.x * 0.35D, vel.y * 0.35D, vel.z * 0.35D);
    }

    public static void playHuntSound(Mob mob, SoundEvent sound, float volume, float pitch) {
        if (sound == null) {
            return;
        }

        var level = mob.level();

        level.playSound(
            null,
            mob.blockPosition(),
            sound,
            SoundSource.HOSTILE,
            volume,
            pitch
        );
    }

    public static boolean isDeepEnoughToBurrow(Mob mob, double minDepthBelowSurface) {
        var surfaceY = getSurfaceY(mob, mob.getX(), mob.getZ());
        return mob.getY() <= surfaceY - minDepthBelowSurface;
    }

    public static Vec3 pickDiveDestination(
        Mob mob,
        double minDepthBelowSurface,
        double maxDepthBelowSurface
    ) {
        var surfaceY = getSurfaceY(mob, mob.getX(), mob.getZ());

        var depth = minDepthBelowSurface
            + mob.getRandom().nextDouble() * Math.max(0.1D, maxDepthBelowSurface - minDepthBelowSurface);

        var angle = mob.getRandom().nextDouble() * Math.PI * 2.0D;
        var sidePush = 0.75D;

        return new Vec3(
            mob.getX() + Math.cos(angle) * sidePush,
            surfaceY - depth,
            mob.getZ() + Math.sin(angle) * sidePush
        );
    }

    public static void moveTowardIgnoringBurrowCheck(Mob mob, Vec3 destination, double speed) {
        var delta = destination.subtract(mob.position());
        var len = delta.length();

        if (len <= 0.1D) {
            return;
        }

        var velocity = delta.scale(speed / len);

        mob.setDeltaMovement(velocity);
        mob.hasImpulse = true;

        var dx = delta.x;
        var dy = delta.y;
        var dz = delta.z;
        var horiz = Math.sqrt(dx * dx + dz * dz);

        if (horiz > 0.1D) {
            float yaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            mob.setYRot(yaw);
            mob.yBodyRot = yaw;
            mob.yHeadRot = yaw;
        }

        var targetPitch = (float) -(Math.atan2(dy, horiz) * (180.0D / Math.PI));
        mob.setXRot(mob.getXRot() + (targetPitch - mob.getXRot()) * 0.15F);
    }

    public static void rotateToward(Mob mob, Vec3 targetPos, float turnSpeed) {
        var delta = targetPos.subtract(mob.position());

        var dx = delta.x;
        var dz = delta.z;

        if (dx * dx + dz * dz < 0.001D) {
            return;
        }

        var targetYaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;

        var newYaw = rotateAngleToward(mob.getYRot(), targetYaw, turnSpeed);

        mob.setYRot(newYaw);
        mob.yBodyRot = newYaw;
        mob.yBodyRotO = newYaw;
        mob.yHeadRot = newYaw;
        mob.yHeadRotO = newYaw;
    }

    private static float rotateAngleToward(float current, float target, float maxTurn) {
        var delta = wrapDegrees(target - current);

        if (delta > maxTurn) {
            delta = maxTurn;
        }

        if (delta < -maxTurn) {
            delta = -maxTurn;
        }

        return current + delta;
    }

    private static float wrapDegrees(float angle) {
        angle = angle % 360.0F;

        if (angle >= 180.0F) {
            angle -= 360.0F;
        }

        if (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }
}
