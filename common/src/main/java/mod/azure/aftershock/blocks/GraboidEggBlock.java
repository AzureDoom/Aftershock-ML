package mod.azure.aftershock.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.aftershock.entity.blaster.BlasterEntity;
import mod.azure.aftershock.registry.BlockRegistry;
import mod.azure.aftershock.registry.EntityRegistry;

public class GraboidEggBlock extends Block {

    public static final IntegerProperty HATCH = BlockStateProperties.HATCH;

    public static final IntegerProperty EGGS = BlockStateProperties.EGGS;

    public GraboidEggBlock() {
        super(Block.Properties.of().strength(4.0F).sound(SoundType.STONE).randomTicks().noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(EGGS, 1));
    }

    @Override
    public @NotNull VoxelShape getShape(
        @NotNull BlockState state,
        @NotNull BlockGetter worldIn,
        @NotNull BlockPos pos,
        @NotNull CollisionContext context
    ) {
        return Block.box(7, 0, 7, 9, 5, 9);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, EGGS);
    }

    @Override
    public void stepOn(
        @NotNull Level level,
        @NotNull BlockPos blockPos,
        @NotNull BlockState blockState,
        Entity entity
    ) {
        if (!entity.isSteppingCarefully())
            this.destroyEgg(level, blockState, blockPos, entity, 100);
        super.stepOn(level, blockPos, blockState, entity);
    }

    @Override
    public void fallOn(
        @NotNull Level level,
        @NotNull BlockState blockState,
        @NotNull BlockPos blockPos,
        @NotNull Entity entity,
        float f
    ) {
        if (!(entity instanceof Zombie))
            this.destroyEgg(level, blockState, blockPos, entity, 3);
        super.fallOn(level, blockState, blockPos, entity, f);
    }

    protected void destroyEgg(Level level, BlockState blockState, BlockPos blockPos, Entity entity, int i) {
        if (!this.canDestroyEgg(level, entity))
            return;
        if (!level.isClientSide && level.random.nextInt(i) == 0 && blockState.is(this))
            this.decreaseEggs(level, blockPos, blockState);
    }

    protected void decreaseEggs(Level level, BlockPos blockPos, BlockState blockState) {
        level.playSound(
            null,
            blockPos,
            SoundEvents.TURTLE_EGG_BREAK,
            SoundSource.BLOCKS,
            0.2f,
            0.9f + level.random.nextFloat() * 0.2f
        );
        int i = blockState.getValue(EGGS);
        if (i <= 1)
            level.destroyBlock(blockPos, false);
        else {
            level.setBlock(blockPos, blockState.setValue(EGGS, i - 1), 2);
            level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(blockState));
            level.levelEvent(2001, blockPos, Block.getId(blockState));
        }
    }

    protected boolean shouldUpdateHatchLevel(Level level) {
        var f = level.getTimeOfDay(1.0f);
        if ((double) f < 0.69 && (double) f > 0.65)
            return true;
        return level.random.nextInt(200) == 0;
    }

    @Override
    public void randomTick(
        @NotNull BlockState blockState,
        @NotNull ServerLevel serverLevel,
        @NotNull BlockPos blockPos,
        @NotNull RandomSource randomSource
    ) {
        if (this.shouldUpdateHatchLevel(serverLevel)) {
            int i = blockState.getValue(HATCH);
            if (i < 2) {
                serverLevel.playSound(
                    null,
                    blockPos,
                    SoundEvents.TURTLE_EGG_CRACK,
                    SoundSource.BLOCKS,
                    0.3f,
                    0.9f + randomSource.nextFloat() * 0.2f
                );
                serverLevel.setBlock(blockPos, blockState.setValue(HATCH, i + 1), 2);
            } else {
                serverLevel.playSound(
                    null,
                    blockPos,
                    SoundEvents.TURTLE_EGG_HATCH,
                    SoundSource.BLOCKS,
                    0.3f,
                    0.9f + randomSource.nextFloat() * 0.2f
                );
                serverLevel.removeBlock(blockPos, false);
                serverLevel.levelEvent(2001, blockPos, Block.getId(blockState));
                var dirtDragon = EntityRegistry.DIRT_DRAGON.get().create(serverLevel);
                if (dirtDragon == null) {
                    return;
                }
                dirtDragon.setGrowth(0);
                dirtDragon.moveTo(
                    (double) blockPos.getX() + 0.3 + 0,
                    blockPos.getY(),
                    (double) blockPos.getZ() + 0.3,
                    0.0f,
                    0.0f
                );
                serverLevel.addFreshEntity(dirtDragon);
            }
        }
    }

    @Override
    public void playerDestroy(
        @NotNull Level level,
        @NotNull Player player,
        @NotNull BlockPos blockPos,
        @NotNull BlockState blockState,
        @Nullable BlockEntity blockEntity,
        @NotNull ItemStack itemStack
    ) {
        super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
        this.decreaseEggs(level, blockPos, blockState);
    }

    protected boolean canDestroyEgg(Level level, Entity entity) {
        if (entity instanceof BlasterEntity || entity instanceof Bat)
            return false;
        if (entity instanceof LivingEntity)
            return entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        return false;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(
        @NotNull BlockState state,
        Level level,
        @NotNull BlockPos pos,
        @NotNull Player player,
        @NotNull BlockHitResult hitResult
    ) {
        if (!level.isClientSide) {
            level.destroyBlock(pos, true);
            if (!player.getMainHandItem().is(ItemTags.PICKAXES))
                level.addFreshEntity(
                    new ItemEntity(
                        level,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        BlockRegistry.GRABOID_EGG_ITEM.get().getDefaultInstance()
                    )
                );
        }
        return InteractionResult.SUCCESS;
    }

}
