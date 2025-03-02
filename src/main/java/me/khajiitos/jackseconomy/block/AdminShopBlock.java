package me.khajiitos.jackseconomy.block;

import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class AdminShopBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public AdminShopBlock() {
        super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(1.5F, 6.0F));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player player, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide && player instanceof ServerPlayer serverPlayer) {
            CompoundTag compoundTag = PriceManager.toAdminShopSchemaCompound(serverPlayer);
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((pContainerId, pPlayerInventory, pPlayer) -> new AdminShopMenu(pContainerId, pPlayerInventory), Component.empty()));
            Packets.sendToClient(serverPlayer, new AdminShopSchemaPacket(compoundTag));
        }

        return InteractionResult.SUCCESS;
    }
}
