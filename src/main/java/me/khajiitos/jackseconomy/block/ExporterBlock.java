package me.khajiitos.jackseconomy.block;

import com.mojang.serialization.MapCodec;
import me.khajiitos.jackseconomy.blockentity.ExporterBlockEntity;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
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

public class ExporterBlock extends TransactionMachineBlock {
    public static final MapCodec<ExporterBlock> CODEC = AdminShopBlock.simpleCodec(unused -> new ExporterBlock());
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ExporterBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(1.5F, 6.0F));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityReg.EXPORTER.get().create(pos, state);
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
        return createTickerHelper(blockEntityType, BlockEntityReg.EXPORTER.get(), ExporterBlockEntity::tick);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
