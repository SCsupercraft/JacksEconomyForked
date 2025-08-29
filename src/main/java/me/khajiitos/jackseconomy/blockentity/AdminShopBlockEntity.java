package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.block.AdminShopBlock;
import me.khajiitos.jackseconomy.data.AdminShopColorManager;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
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
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		name = tag.contains("adminShopName", Tag.TAG_STRING) ? tag.getString("adminShopName") : null;
		color = tag.contains("color", Tag.TAG_INT) ? tag.getInt("color") : -1;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		if (name != null) tag.putString("adminShopName", name);
		if (color != -1) tag.putInt("color", color);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
		this.loadAdditional(pkt.getTag(), provider);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
		this.loadAdditional(tag, provider);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		this.saveAdditional(tag, provider);
		return tag;
	}

	public @Nullable String getName() {
		return name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	public static void tick(Level level, BlockPos blockPos, BlockState blockState, AdminShopBlockEntity blockEntity) {
		int color = AdminShopColorManager.adminShopColors.containsKey(blockEntity.name)
				? AdminShopColorManager.adminShopColors.get(blockEntity.name)
				: AdminShopColorManager.defaultAdminShopColor;
		int actualColor = blockEntity.color;
		if (color == actualColor || !(level instanceof ServerLevel serverLevel)) return;

		blockEntity.color = color;

		BlockState newState = blockState.setValue(AdminShopBlock.COLORED, color != -1);
		serverLevel.setBlockAndUpdate(blockPos, newState);
		serverLevel.sendBlockUpdated(blockPos, blockState, newState, 0); // flags apparently are not used
	}
}
