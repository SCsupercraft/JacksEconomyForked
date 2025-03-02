package me.khajiitos.jackseconomy.block;

import me.khajiitos.jackseconomy.blockentity.FluidExporterBlockEntity;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class FluidExporterBlock extends FluidTransactionMachineBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public FluidExporterBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(1.5F, 6.0F));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityReg.FLUID_EXPORTER.get().create(pos, state);
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockEntityReg.FLUID_EXPORTER.get(), FluidExporterBlockEntity::tick);
    }
}
