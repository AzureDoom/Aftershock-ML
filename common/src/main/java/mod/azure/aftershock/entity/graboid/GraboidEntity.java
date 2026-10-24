package mod.azure.aftershock.entity.graboid;

import mod.azure.azurelib.common.util.MoveAnalysis;
import mod.azure.azurelib.common.hitbox.AzHitboxHolder;
import mod.azure.azurelib.common.hitbox.AzHitboxSet;
import mod.azure.azurelib.common.hitbox.AzHitboxes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.aftershock.CommonMod;
import mod.azure.aftershock.ai.core.MobBrainRuntime;
import mod.azure.aftershock.ai.util.GraboidBurrowUtils;
import mod.azure.aftershock.ai.util.TargetingSystem;
import mod.azure.aftershock.ai.util.VibrationTargetSelector;
import mod.azure.aftershock.entity.SoundTrackingEntity;
import mod.azure.aftershock.registry.EntityRegistry;
import mod.azure.aftershock.registry.SoundRegistry;
import mod.azure.aftershock.util.MobUtils;

public class GraboidEntity extends SoundTrackingEntity implements AzHitboxHolder {

    protected static final EntityDataAccessor<Float> GROWTH = SynchedEntityData.defineId(
            GraboidEntity.class,
            EntityDataSerializers.FLOAT
    );

    private static final EntityDataAccessor<Boolean> DATA_BURROWING =
            SynchedEntityData.defineId(GraboidEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> DATA_BEACHED =
            SynchedEntityData.defineId(GraboidEntity.class, EntityDataSerializers.BOOLEAN);

    public static final EntityDataAccessor<Boolean> ALBINO = SynchedEntityData.defineId(
            GraboidEntity.class,
            EntityDataSerializers.BOOLEAN
    );

    private final MobBrainRuntime<GraboidEntity> brainRuntime;

    public final GraboidAnimationDispatcher animationDispatcher;

    private final MoveAnalysis moveAnalysis;

    private static final float BEACH_GROWTH = 336000.0F;

    public GraboidEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.brainRuntime = new MobBrainRuntime<>(
                this,
                new TargetingSystem<>(
                        new VibrationTargetSelector<>(64.0D),
                        10
                ),
                GrabiodTree.create()
        );
        this.animationDispatcher = new GraboidAnimationDispatcher(this);
        this.moveAnalysis = new MoveAnalysis(this);
    }

    @Override
    protected int getBaseExperienceReward() {
        return CommonMod.getConfig().entityConfigs.graboid_exp;
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 16.0F)
                .add(Attributes.ATTACK_DAMAGE, CommonMod.getConfig().entityConfigs.graboid_damage)
                .add(Attributes.MAX_HEALTH, CommonMod.getConfig().entityConfigs.graboid_health)
                .add(Attributes.ARMOR, CommonMod.getConfig().entityConfigs.graboid_armor)
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
    public @NotNull AABB makeBoundingBox() {
        var pos = this.position();
        var height = this.getDimensions(Pose.STANDING).height();
        var halfX = (6.4) / 2.0;
        var halfZ = (4.6) / 2.0;

        return new AABB(
                pos.x - halfX,
                pos.y,
                pos.z - halfZ,
                pos.x + halfX,
                pos.y + height,
                pos.z + halfZ
        );
    }

    @Override
    public AzHitboxSet getHitboxSet() {
        return GraboidHitboxes.SET;
    }

    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return AzHitboxes.getCullingBox(this, super.getBoundingBoxForCulling());
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.isAlive()) {
                grow(this, 1);
            }
            if (this.getGrowth() >= BEACH_GROWTH) {
                this.setBeached(true);
            }

            if (this.isBeached()) {
                tickBeached();
            } else {
                brainRuntime.tick();
                this.setNoGravity(this.isBurrowing());
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
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BURROWING, false);
        builder.define(GROWTH, 0.0F);
        builder.define(ALBINO, false);
        builder.define(DATA_BEACHED, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("burrowing", this.isBurrowing());
        tag.putFloat("growth", this.getGrowth());
        tag.putBoolean("albino", this.isAlbino());
        tag.putBoolean("beached", this.isBeached());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setBurrowing(tag.getBoolean("burrowing"));
        this.setGrowth(tag.getFloat("growth"));
        this.setAlbinoStatus(tag.getBoolean("albino"));
        this.setBeached(tag.getBoolean("beached"));
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
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

    public boolean isBeached() {
        return this.entityData.get(DATA_BEACHED);
    }

    public void setBeached(boolean beached) {
        this.entityData.set(DATA_BEACHED, beached);

        if (beached) {
            this.setBurrowing(false);
            this.setNoGravity(false);
            this.noPhysics = false;
            this.fallDistance = 0.0F;
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        }
    }

    private void tickBeached() {
        this.getNavigation().stop();
        this.setAggressive(false);
        this.setBurrowing(false);
        this.setNoGravity(false);
        this.noPhysics = false;
        this.fallDistance = 0.0F;

        double surfaceY = GraboidBurrowUtils.getSurfaceY(this, this.getX(), this.getZ());

        if (this.getY() < surfaceY) {
            double nextY = Math.min(surfaceY, this.getY() + 0.25D);

            this.setPos(
                    this.getX(),
                    nextY,
                    this.getZ()
            );

            this.setDeltaMovement(0.0D, 0.0D, 0.0D);
            this.hasImpulse = true;
            return;
        }

        this.setPos(
                this.getX(),
                surfaceY,
                this.getZ()
        );

        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        this.hasImpulse = true;
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

        if (moveAnalysis.isMoving()) {
            animationDispatcher.clientWalk();
            return;
        }

        animationDispatcher.clientIdle();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.GRABOID_IDLE.get();
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundRegistry.GRABOID_DYING.get();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockIn) {}

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
        return 342000;
    }

    @Override
    public void growUp(Entity entity) {
        var world = entity.level();
        if (!world.isClientSide() && !this.isAlbino()) {
            var newEntity = growInto();
            var newEntity1 = growInto();
            var newEntity2 = growInto();
            if (newEntity == null || newEntity1 == null || newEntity2 == null)
                return;
            newEntity.moveTo(entity.blockPosition().east(), entity.getYRot(), entity.getXRot());
            newEntity1.moveTo(entity.blockPosition().west(), entity.getYRot(), entity.getXRot());
            newEntity2.moveTo(entity.blockPosition(), entity.getYRot(), entity.getXRot());
            world.addFreshEntity(newEntity);
            world.addFreshEntity(newEntity1);
            world.addFreshEntity(newEntity2);
            entity.kill();
        }
    }

    @Override
    public LivingEntity growInto() {
        var entity = EntityRegistry.SHRIEKER.get().create(level());
        if (entity == null) {
            return null;
        }
        if (hasCustomName())
            entity.setCustomName(this.getCustomName());
        entity.setNewBornStatus(true);
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
        return this.isAlbino() ? null : entity;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (source == damageSources().inWall())
            return false;
        return super.hurt(source, amount);
    }

    public boolean isAlbino() {
        return this.entityData.get(ALBINO);
    }

    public void setAlbinoStatus(boolean searching) {
        this.entityData.set(ALBINO, searching);
    }
}