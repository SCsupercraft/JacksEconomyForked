package me.khajiitos.jackseconomy.block;

import com.mojang.serialization.MapCodec;
import me.khajiitos.jackseconomy.blockentity.ImporterBlockEntity;
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

public class ImporterBlock extends TransactionMachineBlock {
    public static final MapCodec<ImporterBlock> CODEC = AdminShopBlock.simpleCodec(unused -> new ImporterBlock());

    public ImporterBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(1.5F, 6.0F));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityReg.IMPORTER.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockEntityReg.IMPORTER.get(), ImporterBlockEntity::tick);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
