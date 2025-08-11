package me.khajiitos.jackseconomy.block;

import com.mojang.serialization.MapCodec;
import me.khajiitos.jackseconomy.blockentity.CurrencyConverterBlockEntity;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.IDisablable;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CurrencyConverterBlock extends BaseEntityBlock implements IDisablable {
    public static final MapCodec<CurrencyConverterBlock> CODEC = CurrencyConverterBlock.simpleCodec(unused -> new CurrencyConverterBlock());
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public CurrencyConverterBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(1.5F, 6.0F));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityReg.CURRENCY_CONVERTER.get().create(pos, state);
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (this.isDisabled()) {
            return InteractionResult.PASS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CurrencyConverterBlockEntity currencyConverterBlockEntity) {
            if (!level.isClientSide()) {
                player.openMenu(currencyConverterBlockEntity, pos);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockEntityReg.CURRENCY_CONVERTER.get(), CurrencyConverterBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof CurrencyConverterBlockEntity blockEntity) {
                CurrencyHelper.getCurrencyItems(blockEntity.getCurrency()).forEach(itemStack -> ItemHelper.dropItem(itemStack, level, blockEntity.getBlockPos()));
                for (int i = 0; i < blockEntity.items.size(); i++) {
                    ItemStack stack = blockEntity.getItem(i);
                    ItemHelper.dropItem(stack, level, blockEntity.getBlockPos());
                }
            }

            if (state.hasBlockEntity() && (!state.is(newState.getBlock()) || !newState.hasBlockEntity())) {
                level.removeBlockEntity(pos);
            }

            //super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public boolean isDisabled() {
        return Config.oneItemCurrencyMode.get();
    }
}
