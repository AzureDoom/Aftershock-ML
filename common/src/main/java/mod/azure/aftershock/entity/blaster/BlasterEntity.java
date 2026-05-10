package mod.azure.aftershock.entity.blaster;

import mod.azure.azurelib.common.util.MoveAnalysis;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.aftershock.ai.core.AiKeys;
import mod.azure.aftershock.ai.core.MobBrainRuntime;
import mod.azure.aftershock.ai.util.NearestHeatTargetSelector;
import mod.azure.aftershock.ai.util.TargetingSystem;
import mod.azure.aftershock.registry.SoundRegistry;
import mod.azure.aftershock.util.Growable;
import mod.azure.aftershock.util.MobUtils;

public class BlasterEntity extends PathfinderMob implements Growable {

    protected static final EntityDataAccessor<Float> GROWTH = SynchedEntityData.defineId(
        BlasterEntity.class,
        EntityDataSerializers.FLOAT
    );

    protected static final EntityDataAccessor<Integer> COOKED_FOOD_EATEN =
        SynchedEntityData.defineId(BlasterEntity.class, EntityDataSerializers.INT);

    private final MobBrainRuntime<BlasterEntity> brainRuntime;

    public final BlasterAnimationDispatcher animationDispatcher;

    private final MoveAnalysis moveAnalysis;

    public BlasterEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.brainRuntime = new MobBrainRuntime<>(
            this,
            new TargetingSystem<>(
                new NearestHeatTargetSelector<>(64.0D),
                10
            ),
            BlasterTree.create()
        );
        this.animationDispatcher = new BlasterAnimationDispatcher(this);
        this.moveAnalysis = new MoveAnalysis(this);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
            .add(Attributes.FOLLOW_RANGE, 16.0F)
            .add(Attributes.ATTACK_DAMAGE, 5F)
            .add(Attributes.MAX_HEALTH, 40)
            .add(Attributes.MOVEMENT_SPEED, 0.12F);
    }

    @Override
    public float maxUpStep() {
        return 3.0F;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 40 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.isWakingUp()) {
                this.animationDispatcher.serverWake();
                this.getNavigation().stop();
                this.setDeltaMovement(Vec3.ZERO);
                this.setNoGravity(false);
            } else {
                brainRuntime.tick();

                var inFlight = Boolean.TRUE.equals(
                    brainRuntime.getBlackboard().get(AiKeys.BLASTER_IN_FLIGHT, Boolean.class)
                );
                this.setNoGravity(inFlight);
            }

            if (this.isOnFire() && this.tickCount % 2 == 0) {
                MobUtils.spawnFireParticles(this, (ServerLevel) this.level());
            }
        }

        moveAnalysis.update();
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
        @NotNull ServerLevelAccessor level,
        @NotNull DifficultyInstance difficulty,
        @NotNull MobSpawnType spawnType,
        @Nullable SpawnGroupData spawnGroupData
    ) {
        if (spawnType == MobSpawnType.SPAWN_EGG || spawnType == MobSpawnType.COMMAND)
            setGrowth(1200);
        brainRuntime.getCooldowns().set(AiKeys.MELEE_COOLDOWN, 200);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.SHRIEKER_SEARCH.get();
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockIn) {
        this.playSound(SoundRegistry.GRABOID_MOVING.get(), 0.15F, 1.0F);
    }

    @Override
    public float getGrowth() {
        return entityData.get(GROWTH);
    }

    @Override
    public void setGrowth(float growth) {
        entityData.set(GROWTH, growth);
    }

    public boolean isWakingUp() {
        return this.getGrowth() <= 100.0F;
    }

    public int getCookedFoodEaten() {
        return this.entityData.get(COOKED_FOOD_EATEN);
    }

    public void setCookedFoodEaten(int amount) {
        this.entityData.set(COOKED_FOOD_EATEN, amount);
    }

    public void incrementCookedFoodEaten() {
        this.setCookedFoodEaten(this.getCookedFoodEaten() + 1);
    }

    public boolean isReadyToLayEgg() {
        return this.getCookedFoodEaten() >= 10;
    }

    public void resetCookedFoodEaten() {
        this.setCookedFoodEaten(0);
    }

    @Override
    public float getMaxGrowth() {
        return 1200;
    }

    @Override
    public LivingEntity growInto() {
        return null;
    }

    @Override
    public void growUp(Entity entity) {}

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GROWTH, 0.0F);
        builder.define(COOKED_FOOD_EATEN, 0);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("growth", entityData.get(GROWTH));
        tag.putBoolean(
            "inFlight",
            Boolean.TRUE.equals(brainRuntime.getBlackboard().get(AiKeys.BLASTER_IN_FLIGHT, Boolean.class))
        );
        tag.putBoolean(
            "takingOff",
            Boolean.TRUE.equals(brainRuntime.getBlackboard().get(AiKeys.BLASTER_TAKING_OFF, Boolean.class))
        );
        tag.putInt("cookedFoodEaten", this.getCookedFoodEaten());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setGrowth(tag.getFloat("growth"));
        this.setCookedFoodEaten(tag.getInt("cookedFoodEaten"));
        brainRuntime.getBlackboard().set(AiKeys.BLASTER_TAKING_OFF, false);
        brainRuntime.getBlackboard().set(AiKeys.BLASTER_IN_FLIGHT, tag.getBoolean("inFlight"));
    }

    public void updateAnimations() {
        var isMovingHorizontally = moveAnalysis.isMovingHorizontally();
        var isMovingOnGround = isMovingHorizontally && onGround();
        var isMovingInAir = isMovingHorizontally && !onGround();

        if (this.isDeadOrDying() || this.getHealth() <= 0) {
            animationDispatcher.clientDeath();
            return;
        }

        if (this.isWakingUp()) {
            animationDispatcher.serverWake();
            return;
        }

        if (isMovingOnGround) {
            if (this.isAggressive() && !this.swinging) {
                animationDispatcher.clientRun();
            } else {
                animationDispatcher.clientWalk();
            }
            return;
        }

        if (isMovingInAir) {
            animationDispatcher.serverGliding();
            return;
        }

        // if (this.getRandom().nextDouble() >= 0.5) {
        // animationDispatcher.clientLook();
        // } else {
        // animationDispatcher.clientIdle();
        // }
        animationDispatcher.clientIdle();
    }
}
