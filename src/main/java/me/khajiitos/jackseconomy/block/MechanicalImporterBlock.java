package me.khajiitos.jackseconomy.block;

import me.khajiitos.jackseconomy.blockentity.MechanicalImporterBlockEntity;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class MechanicalImporterBlock extends KineticTransactionMachineBlock<MechanicalImporterBlockEntity> {

    public MechanicalImporterBlock() {
        super(Properties.of().sound(SoundType.METAL).noOcclusion().strength(1.5F, 6.0F));
        this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public Class<MechanicalImporterBlockEntity> getBlockEntityClass() {
        return MechanicalImporterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalImporterBlockEntity> getBlockEntityType() {
        return BlockEntityReg.MECHANICAL_IMPORTER.get();
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
        return createTickerHelper(blockEntityType, BlockEntityReg.MECHANICAL_IMPORTER.get(), MechanicalImporterBlockEntity::tick);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getOpposite().getAxis();
    }
}
