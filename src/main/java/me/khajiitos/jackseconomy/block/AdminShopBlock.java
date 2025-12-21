package me.khajiitos.jackseconomy.block;

import com.mojang.serialization.MapCodec;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.AdminShopBlockEntity;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.AdminShopColorManager;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.item.NameableBlockItem;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AdminShopBlock extends BaseEntityBlock implements NameableBlockItem.NameableBlock {
    public static final MapCodec<AdminShopBlock> CODEC = AdminShopBlock.simpleCodec(unused -> new AdminShopBlock());
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty COLORED = BooleanProperty.create("colored");

    public AdminShopBlock() {
        super(Properties.of().sound(SoundType.METAL).strength(1.5F, 6.0F));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(COLORED, false));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityReg.ADMIN_SHOP.get().create(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        CompoundTag tag = pContext.getItemInHand().getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).getUnsafe();
        String name = tag.contains("adminShopName") ? tag.getString("adminShopName") : null;
        boolean isColored = AdminShopColorManager.getColor(name) != -1;

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(COLORED, isColored);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
        pBuilder.add(COLORED);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos,
                                                     @NotNull Player player, @NotNull BlockHitResult pHit) {
        if (!pLevel.isClientSide && player instanceof ServerPlayer serverPlayer
                && pLevel.getBlockEntity(pPos) instanceof AdminShopBlockEntity entity) {
            CompoundTag compoundTag = PriceManager.toAdminShopSchemaCompound(serverPlayer, entity.getName());
            serverPlayer.openMenu(new SimpleMenuProvider((pContainerId, pPlayerInventory, pPlayer) -> new AdminShopMenu(pContainerId, pPlayerInventory), Component.empty()));
            PacketDistributor.sendToPlayer(serverPlayer, new AdminShopSchemaPacket(compoundTag, Optional.ofNullable(entity.getName()), Config.oneItemCurrencyMode.get()));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.Builder params) {
        ItemStack stack = new ItemStack(ItemBlockReg.ADMIN_SHOP_ITEM.get(), 1);

        if (params.getParameter(LootContextParams.BLOCK_ENTITY) instanceof AdminShopBlockEntity entity) {
            entity.saveToItem(stack, JacksEconomy.server.registryAccess());
        }

        return new ArrayList<>(Collections.singleton(stack));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext tooltipContext, @NotNull List<Component> tooltip, TooltipFlag flag) {
        if (flag.isAdvanced()) {
            CompoundTag tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).getUnsafe();

            tooltip.add(Component.translatable(
                    "jackseconomy.admin_shop_name",
                    tag.contains("adminShopName") ?
                            Component.literal(tag.getString("adminShopName")).withStyle(ChatFormatting.GRAY) :
                            Component.translatable("jackseconomy.default").withStyle(ChatFormatting.GRAY)
            ));
        }
    }

    @Override
    public MutableComponent getItemName(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).getUnsafe();
        if (!tag.contains("adminShopName")) return getName();
        return Component.translatable(tag.getString("adminShopName"));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlockEntityReg.ADMIN_SHOP.get(), AdminShopBlockEntity::tick);
    }
}
