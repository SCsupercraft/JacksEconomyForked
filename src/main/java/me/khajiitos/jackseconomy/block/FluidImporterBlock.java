package me.khajiitos.jackseconomy.block;

import com.mojang.serialization.MapCodec;
import me.khajiitos.jackseconomy.blockentity.FluidImporterBlockEntity;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidImporterBlock extends FluidTransactionMachineBlock {
    public static final MapCodec<FluidImporterBlock> CODEC = AdminShopBlock.simpleCodec(unused -> new FluidImporterBlock());

    public FluidImporterBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(1.5F, 6.0F));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityReg.FLUID_IMPORTER.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockEntityReg.FLUID_IMPORTER.get(), FluidImporterBlockEntity::tick);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
