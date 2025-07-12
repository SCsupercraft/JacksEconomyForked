package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.block.AdminShopBlock;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AdminShopBlockEntity extends BlockEntity {
	private @Nullable String name;
	private int color = -1;

	public AdminShopBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(BlockEntityReg.ADMIN_SHOP.get(), pPos, pBlockState);
	}

	@Override
	public void load(CompoundTag tag) {
		name = tag.contains("adminShopName", Tag.TAG_STRING) ? tag.getString("adminShopName") : null;
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		if (name != null) tag.putString("adminShopName", name);
	}

	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		if (pkt.getTag() != null) {
			this.load(pkt.getTag());
		}
	}

	public void handleUpdateTag(CompoundTag tag) {
		this.load(tag);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		this.saveAdditional(tag);
		return tag;
	}

	public @Nullable String getName() {
		return name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	public static void tick(Level level, BlockPos blockPos, BlockState blockState, AdminShopBlockEntity blockEntity) {
		int color = JacksEconomyClient.adminShopColors.containsKey(blockEntity.name)
				? JacksEconomyClient.adminShopColors.get(blockEntity.name)
				: JacksEconomyClient.defaultAdminShopColor;
		int actualColor = blockEntity.color;
		if (color == actualColor) return;

		blockEntity.color = color;

		BlockState newState = blockState.setValue(AdminShopBlock.COLORED, color != -1);
		level.setBlockAndUpdate(blockPos, newState);
		level.sendBlockUpdated(blockPos, blockState, newState, Block.UPDATE_CLIENTS & Block.UPDATE_IMMEDIATE);
	}
}
