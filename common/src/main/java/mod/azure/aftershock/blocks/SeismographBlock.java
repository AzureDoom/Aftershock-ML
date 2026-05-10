package mod.azure.aftershock.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import mod.azure.aftershock.blocks.be.SeismographBlockEntity;
import mod.azure.aftershock.registry.EntityRegistry;

public class SeismographBlock extends BaseEntityBlock implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final EnumProperty<SeismographStates> STORAGE_STATE = SeismographProperties.STORAGE_STATE;

    public static final MapCodec<SeismographBlock> CODEC = simpleCodec(SeismographBlock::new);

    public SeismographBlock(Properties properties) {
        super(properties.strength(4.0F).sound(SoundType.METAL).noOcclusion());
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STORAGE_STATE, SeismographStates.CLOSED)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STORAGE_STATE);
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull VoxelShape getShape(
        @NotNull BlockState state,
        @NotNull BlockGetter worldIn,
        @NotNull BlockPos pos,
        @NotNull CollisionContext context
    ) {
        return Block.box(3, 0, 3, 13, 6, 13);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return EntityRegistry.SEIMOGRAPH.get().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        @NotNull Level level,
        @NotNull BlockState state,
        @NotNull BlockEntityType<T> type
    ) {
        return createTickerHelper(type, EntityRegistry.SEIMOGRAPH.get(), SeismographBlockEntity::tick);
    }

}
