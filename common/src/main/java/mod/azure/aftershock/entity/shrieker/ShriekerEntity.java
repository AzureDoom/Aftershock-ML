package mod.azure.aftershock.entity.shrieker;

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
import mod.azure.aftershock.ai.util.NearestHeatTargetSelector;
import mod.azure.aftershock.ai.util.TargetingSystem;
import mod.azure.aftershock.registry.EntityRegistry;
import mod.azure.aftershock.registry.SoundRegistry;
import mod.azure.aftershock.util.Growable;
import mod.azure.aftershock.util.MobUtils;

public class ShriekerEntity extends PathfinderMob implements Growable {

    protected static final EntityDataAccessor<Float> GROWTH = SynchedEntityData.defineId(
        ShriekerEntity.class,
        EntityDataSerializers.FLOAT
    );

    public static final EntityDataAccessor<Boolean> BIRTH = SynchedEntityData.defineId(
        ShriekerEntity.class,
        EntityDataSerializers.BOOLEAN
    );

    protected static final EntityDataAccessor<Integer> COOKED_FOOD_EATEN =
        SynchedEntityData.defineId(ShriekerEntity.class, EntityDataSerializers.INT);

    private final MobBrainRuntime<ShriekerEntity> brainRuntime;

    public final ShriekerAnimationDispatcher animationDispatcher;

    private final MoveAnalysis moveAnalysis;

    private ClientAnimState currentClientAnim = null;

    private int lookCooldown = 0;

    private int lookTicks = 0;

    private int lastAnimationTick = -1;

    public ShriekerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.brainRuntime = new MobBrainRuntime<>(
            this,
            new TargetingSystem<>(
                new NearestHeatTargetSelector<>(64.0D),
                10
            ),
            ShriekerTree.create()
        );
        this.animationDispatcher = new ShriekerAnimationDispatcher(this);
        this.moveAnalysis = new MoveAnalysis(this);
    }

    @Override
    public float maxUpStep() {
        return 3.0F;
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

            if (this.isOnFire() && this.tickCount % 2 == 0) {
                MobUtils.spawnFireParticles(this, (ServerLevel) this.level());
            }
        }

        moveAnalysis.update();
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
    protected @Nullable SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundRegistry.SHRIEKER_HURT.get();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockIn) {
        this.playSound(SoundEvents.PIG_STEP, 0.15F, 1.0F);
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
    public float getGrowth() {
        return entityData.get(GROWTH);
    }

    @Override
    public void setGrowth(float growth) {
        entityData.set(GROWTH, growth);
    }

    public boolean isNewBorn() {
        return this.entityData.get(BIRTH);
    }

    public void setNewBornStatus(boolean birthing) {
        this.entityData.set(BIRTH, birthing);
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
        return 48000;
    }

    @Override
    public LivingEntity growInto() {
        var entity = EntityRegistry.BLASTER.get().create(level());
        if (entity == null) {
            return null;
        }
        if (hasCustomName())
            entity.setCustomName(this.getCustomName());
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
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GROWTH, 0.0F);
        builder.define(BIRTH, false);
        builder.define(COOKED_FOOD_EATEN, 0);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("growth", getGrowth());
        tag.putBoolean("newBorn", isNewBorn());
        tag.putInt("cookedFoodEaten", this.getCookedFoodEaten());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setGrowth(tag.getFloat("growth"));
        this.setNewBornStatus(tag.getBoolean("newBorn"));
        this.setCookedFoodEaten(tag.getInt("cookedFoodEaten"));
    }

    private enum ClientAnimState {
        DEATH,
        HURT,
        WALK,
        RUN,
        IDLE,
        LOOK
    }

    private void playAnimation(ClientAnimState next) {
        if (currentClientAnim == next) {
            return;
        }

        currentClientAnim = next;

        switch (next) {
            case DEATH -> animationDispatcher.clientDeath();
            case HURT -> animationDispatcher.clientHurt();
            case WALK -> animationDispatcher.clientWalk();
            case RUN -> animationDispatcher.clientRun();
            case IDLE -> animationDispatcher.clientIdle();
            case LOOK -> animationDispatcher.clientLook();
        }
    }

    public void updateAnimations() {
        if (this.isAggressive()) {
            animationDispatcher.clientAgressive();
        } else {
            animationDispatcher.clientIdleMode();
        }
        boolean newEntityTick = this.tickCount != lastAnimationTick;
        if (newEntityTick) {
            lastAnimationTick = this.tickCount;
        }

        if (this.isDeadOrDying() || this.getHealth() <= 0) {
            playAnimation(ClientAnimState.DEATH);
            return;
        }

        if (this.hurtTime > 0) {
            playAnimation(ClientAnimState.HURT);
            return;
        }

        if (moveAnalysis.isMoving()) {
            if (newEntityTick) {
                lookTicks = 0;
            }

            if (this.isAggressive() && !this.swinging) {
                playAnimation(ClientAnimState.RUN);
            } else {
                playAnimation(ClientAnimState.WALK);
            }

            return;
        }

        if (lookTicks > 0) {
            if (newEntityTick) {
                lookTicks--;
            }

            playAnimation(ClientAnimState.LOOK);
            return;
        }

        if (lookCooldown > 0) {
            if (newEntityTick) {
                lookCooldown--;
            }

            playAnimation(ClientAnimState.IDLE);
            return;
        }

        if (this.getRandom().nextDouble() < 0.15) {
            lookTicks = 60;
            lookCooldown = 120;
            playAnimation(ClientAnimState.LOOK);
            return;
        }

        lookCooldown = 40;
        playAnimation(ClientAnimState.IDLE);
    }
}
