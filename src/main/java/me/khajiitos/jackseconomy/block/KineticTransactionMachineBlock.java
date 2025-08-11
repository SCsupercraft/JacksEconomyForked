package me.khajiitos.jackseconomy.block;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import me.khajiitos.jackseconomy.blockentity.TransactionKineticMachineBlockEntity;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class KineticTransactionMachineBlock<T extends BlockEntity> extends HorizontalKineticBlock implements IBE<T> {
    public KineticTransactionMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public <S extends BlockEntity> BlockEntityTicker<S> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<S> p_153214_) {
        return IBE.super.getTicker(p_153212_, p_153213_, p_153214_);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MenuProvider menuProvider) {
            if (!level.isClientSide()) {
                player.openMenu(menuProvider, pos);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof TransactionKineticMachineBlockEntity blockEntity) {
                CurrencyHelper.getCurrencyItems(blockEntity.getBalance()).forEach(itemStack -> ItemHelper.dropItem(itemStack, level, blockEntity.getBlockPos()));
                for (int i = 0; i < blockEntity.items.size(); i++) {
                    ItemStack stack = blockEntity.getItem(i);
                    ItemHelper.dropItem(stack, level, blockEntity.getBlockPos());
                }
            }

            //if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            //    level.removeBlockEntity(pos);
            //}
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
}
