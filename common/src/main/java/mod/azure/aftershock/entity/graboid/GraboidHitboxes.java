package mod.azure.aftershock.entity.graboid;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.hitbox.AzHitboxSet;
import mod.azure.azurelib.common.hitbox.AzHitboxes;

public final class GraboidHitboxes {

    public static final String MOUTH = "mouth";

    public static final String HEAD_TAG = "head";

    public static final String TENTACLE_TAG = "tentacle";

    public static final String[] TENTACLES = { "tentacle_1", "tentacle_2", "tentacle_3" };

    public static final AzHitboxSet SET = AzHitboxSet.builder()
            .add("body00")
            .add("body02")
            .add("body03")
            .add("body04")
            .add("body05")
            .add("body06")
            .add("body07")
            .add("neck", h -> h.tag(HEAD_TAG))
            .add("topJaw1", h -> h.tag(HEAD_TAG))
            .add("topjaw2", h -> h.tag(HEAD_TAG))
            .add("topJaw3", h -> h.tag(HEAD_TAG))
            .add("left_jaw2", h -> h.tag(HEAD_TAG))
            .add("right_jaw2", h -> h.tag(HEAD_TAG))
            .add("midjaw1", h -> h.tag(HEAD_TAG))
            .add(TENTACLES[0], "lTentaclehead1", h -> h.inflate(1).tag(TENTACLE_TAG))
            .add(TENTACLES[1], "lTentaclehead2", h -> h.inflate(1).tag(TENTACLE_TAG))
            .add(TENTACLES[2], "lTentaclehead3", h -> h.inflate(1).tag(TENTACLE_TAG))
            .add(MOUTH, "neck", h -> h.anchor().anchorAtPivot().size(22, 20, 26).offset(0, -1, -26))
            .maxReach(12)
            .build();

    private GraboidHitboxes() {}

    public static boolean hasLivePose(Mob mob) {
        return AzHitboxes.hasLivePose(mob);
    }

    public static @Nullable Vec3 center(Mob mob, String name) {
        if (!hasLivePose(mob)) {
            return null;
        }
        var box = AzHitboxes.getWorldBox(mob, name);
        return box == null ? null : box.bounds().getCenter();
    }

    public static boolean isInJaws(Mob mob, LivingEntity target) {
        var mouth = AzHitboxes.getWorldBox(mob, MOUTH);
        return mouth != null && mouth.inflate(0.25D).intersects(target.getBoundingBox());
    }

    public static boolean isTentacleTouching(Mob mob, int tentacle, LivingEntity target) {
        var box = AzHitboxes.getWorldBox(mob, TENTACLES[tentacle]);
        return box != null && box.inflate(0.35D).intersects(target.getBoundingBox());
    }

    public static boolean isAnyTentacleTouching(Mob mob, LivingEntity target) {
        for (int i = 0; i < TENTACLES.length; i++) {
            if (isTentacleTouching(mob, i, target)) {
                return true;
            }
        }
        return false;
    }
}