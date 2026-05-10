package mod.azure.aftershock.blocks.be;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;

import mod.azure.aftershock.blocks.SeismographProperties;
import mod.azure.aftershock.blocks.SeismographStates;
import mod.azure.aftershock.entity.SoundTrackingEntity;
import mod.azure.aftershock.registry.EntityRegistry;

public class SeismographBlockEntity extends BlockEntity {

    public static final EnumProperty<SeismographStates> CHEST_STATE = SeismographProperties.STORAGE_STATE;

    protected static int soundcounter;

    public final SeismographAnimationDispatcher animationDispatcher;

    public SeismographBlockEntity(BlockPos pos, BlockState blockState) {
        super(EntityRegistry.SEIMOGRAPH.get(), pos, blockState);
        this.animationDispatcher = new SeismographAnimationDispatcher(this);
    }

    public void setChestState(SeismographStates state) {
        if (this.getLevel() == null) {
            return;
        }
        this.getLevel().setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(CHEST_STATE, state));
    }

    public static void tick(Level world, BlockPos pos, BlockState state, SeismographBlockEntity blockEntity) {
        final var aabb = new AABB(pos).inflate(64D, 64D, 64D);
        if (world != null)
            world.getEntitiesOfClass(SoundTrackingEntity.class, aabb).forEach(e -> {
                if (e.walkAnimation.speed() >= 0.15F) {
                    SeismographBlockEntity.soundcounter++;
                    blockEntity.setChestState(SeismographStates.OPENED);
                    blockEntity.animationDispatcher.sendMovingAnimation();
                    if (SeismographBlockEntity.soundcounter >= 5) {
                        if (blockEntity.getLevel() instanceof ServerLevel serverLevel) {
                            serverLevel.playSound(
                                e,
                                pos,
                                SoundEvents.NOTE_BLOCK_BELL.value(),
                                SoundSource.BLOCKS,
                                1.0f,
                                3.3f
                            );
                        }
                        SeismographBlockEntity.soundcounter = 0;
                    }
                } else {
                    blockEntity.setChestState(SeismographStates.CLOSED);
                    blockEntity.animationDispatcher.sendIdleAnimation();
                    SeismographBlockEntity.soundcounter = 0;
                }
            });
    }
}
