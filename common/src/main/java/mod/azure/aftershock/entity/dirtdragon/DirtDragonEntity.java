package mod.azure.aftershock.entity.dirtdragon;

import mod.azure.azurelib.common.util.MoveAnalysis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.aftershock.ai.core.MobBrainRuntime;
import mod.azure.aftershock.ai.util.TargetingSystem;
import mod.azure.aftershock.ai.util.VibrationTargetSelector;
import mod.azure.aftershock.entity.SoundTrackingEntity;
import mod.azure.aftershock.registry.EntityRegistry;
import mod.azure.aftershock.registry.SoundRegistry;
import mod.azure.aftershock.util.MobUtils;

public class DirtDragonEntity extends SoundTrackingEntity {

    protected static final EntityDataAccessor<Float> GROWTH = SynchedEntityData.defineId(
        DirtDragonEntity.class,
        EntityDataSerializers.FLOAT
    );

    public static final EntityDataAccessor<Boolean> ALBINO = SynchedEntityData.defineId(
        DirtDragonEntity.class,
        EntityDataSerializers.BOOLEAN
    );

    private static final EntityDataAccessor<Boolean> DATA_BURROWING =
        SynchedEntityData.defineId(DirtDragonEntity.class, EntityDataSerializers.BOOLEAN);

    private final MobBrainRuntime<DirtDragonEntity> brainRuntime;

    public final DirtDragonAnimationDispatcher animationDispatcher;

    private final MoveAnalysis moveAnalysis;

    public DirtDragonEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.brainRuntime = new MobBrainRuntime<>(
            this,
            new TargetingSystem<>(
                new VibrationTargetSelector<>(64.0D),
                10
            ),
            DirtDragonTree.create()
        );
        this.animationDispatcher = new DirtDragonAnimationDispatcher(this);
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
            brainRuntime.tick();
            if (this.isAlive()) {
                grow(this, 1);
            }

            this.setNoGravity(this.isBurrowing());
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
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (source == damageSources().inWall())
            return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.DIRT_DRAGON_IDLE.get();
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

    @Override
    public float getMaxGrowth() {
        return 168000;
    }

    @Override
    public void growUp(Entity entity) {
        var world = entity.level();
        if (!world.isClientSide()) {
            var newEntity = growInto();
            if (newEntity == null)
                return;
            newEntity.setPos(entity.getX(), entity.getY(), entity.getZ());
            world.addFreshEntity(newEntity);
            if (entity.hasCustomName())
                newEntity.setCustomName(entity.getCustomName());
            if (entity instanceof LivingEntity livingEntity) {
                for (var effect : livingEntity.getActiveEffects()) {
                    newEntity.addEffect(new MobEffectInstance(effect));
                }
            }
            entity.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    public LivingEntity growInto() {
        var entity = EntityRegistry.GRABOID.get().create(level());
        if (entity == null) {
            return null;
        }
        if (hasCustomName())
            entity.setCustomName(this.getCustomName());
        entity.setGrowth(0);
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false));
        var areaEffectCloudEntity = new AreaEffectCloud(this.level(), this.getX(), this.getY() + 1, this.getZ());
        areaEffectCloudEntity.setRadius(1.0F);
        areaEffectCloudEntity.setDuration(20);
        areaEffectCloudEntity.setParticle(ParticleTypes.POOF);
        areaEffectCloudEntity.setRadiusPerTick(
            -areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration()
        );
        entity.level().addFreshEntity(areaEffectCloudEntity);
        return entity;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BURROWING, false);
        builder.define(GROWTH, 0.0F);
        builder.define(ALBINO, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("burrowing", this.isBurrowing());
        tag.putFloat("growth", this.getGrowth());
        tag.putBoolean("albino", this.isAlbino());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setBurrowing(tag.getBoolean("burrowing"));
        this.setGrowth(tag.getFloat("growth"));
        this.setAlbinoStatus(tag.getBoolean("albino"));
    }

    public boolean isBurrowing() {
        return this.entityData.get(DATA_BURROWING);
    }

    public void setBurrowing(boolean burrowing) {
        this.entityData.set(DATA_BURROWING, burrowing);
        this.noPhysics = burrowing;

        if (burrowing) {
            this.fallDistance = 0.0F;
        }
    }

    public boolean isAlbino() {
        return this.entityData.get(ALBINO);
    }

    public void setAlbinoStatus(boolean searching) {
        this.entityData.set(ALBINO, searching);
    }

    public void updateAnimations() {
        if (this.isDeadOrDying() || this.getHealth() <= 0) {
            animationDispatcher.clientDeath();
            return;
        }

        if (this.hurtTime > 0) {
            animationDispatcher.clientHurt();
            return;
        }

        if (this.getVehicle() != null) {
            animationDispatcher.clientHold();
            return;
        }

        if (moveAnalysis.isMoving()) {
            animationDispatcher.clientWalk();
            return;
        }

        animationDispatcher.clientIdle();
    }
}
