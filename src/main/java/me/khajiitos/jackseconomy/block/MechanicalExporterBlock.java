package me.khajiitos.jackseconomy.block;

import me.khajiitos.jackseconomy.blockentity.MechanicalExporterBlockEntity;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import javax.annotation.Nullable;

public class MechanicalExporterBlock extends KineticTransactionMachineBlock<MechanicalExporterBlockEntity> {

    public MechanicalExporterBlock() {
        super(Properties.of().sound(SoundType.METAL).noOcclusion().strength(1.5F, 6.0F));
        this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public Class<MechanicalExporterBlockEntity> getBlockEntityClass() {
        return MechanicalExporterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalExporterBlockEntity> getBlockEntityType() {
        return BlockEntityReg.MECHANICAL_EXPORTER.get();
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite());
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_FACING);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        //return false;
        return face == (state.getValue(HORIZONTAL_FACING).getOpposite());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> pServerType, BlockEntityType<E> pClientType, BlockEntityTicker<? super E> pTicker) {
        return pClientType == pServerType ? (BlockEntityTicker<A>)pTicker : null;
    }

    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<S> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockEntityReg.MECHANICAL_EXPORTER.get(), MechanicalExporterBlockEntity::tick);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getOpposite().getAxis();
    }
}
