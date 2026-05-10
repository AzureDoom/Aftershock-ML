package mod.azure.aftershock.ai.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

import mod.azure.aftershock.util.ModTags;

public final class TargetingUtils {

    private TargetingUtils() {}

    public static Predicate<LivingEntity> validHeatTarget(Mob mob) {
        return baseValid(mob)
            .and(notAnnoyingMobs())
            .and(notExploding())
            .and(isHeatEntity());
    }

    public static Predicate<LivingEntity> validSoundTarget(Mob mob) {
        return baseValid(mob).and(notAnnoyingMobs()).and(notExploding()).and(isUnderGroundEntity());
    }

    public static Predicate<LivingEntity> validTarget(Mob mob) {
        return baseValid(mob)
            .and(notAnnoyingMobs())
            .and(notExploding())
            .and(inRangeOrVisible(mob));
    }

    public static Predicate<LivingEntity> baseValid(Mob mob) {
        return e -> e != null &&
            e.isAlive() &&
            e != mob &&
            !e.isSpectator() &&
            !(e instanceof Player p && (p.isCreative() || p.isSpectator())) &&
            e.getType() != mob.getType();
    }

    public static Predicate<LivingEntity> notAnnoyingMobs() {
        return e -> !(e instanceof Bat) &&
            !(e instanceof WaterAnimal) &&
            !e.isInWater();
    }

    public static Predicate<LivingEntity> notExploding() {
        return e -> !(e instanceof Creeper c && (c.isIgnited() || c.getSwellDir() > 0));
    }

    public static Predicate<LivingEntity> isUnderGroundEntity() {
        return e -> !e.getType().is(ModTags.UNDERGROUND_ENTITY);
    }

    public static Predicate<LivingEntity> isHeatEntity() {
        return e -> e.getType().is(ModTags.HEAT_ENTITY);
    }

    public static Predicate<LivingEntity> inRangeOrVisible(Mob mob) {
        return mob::hasLineOfSight;
    }

    public static boolean isInAttackRange(Mob mob, LivingEntity target, double reach) {
        return mob.getBoundingBox()
            .inflate(reach)
            .intersects(target.getBoundingBox());
    }

    public static boolean canBreach(Mob mob, LivingEntity target, double range) {
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (mob.distanceToSqr(target) > range * range) {
            return false;
        }

        var surfaceY = GraboidBurrowUtils.getSurfaceY(mob, mob.getX(), mob.getZ());

        return mob.getY() < surfaceY - 1.0D;
    }
}
