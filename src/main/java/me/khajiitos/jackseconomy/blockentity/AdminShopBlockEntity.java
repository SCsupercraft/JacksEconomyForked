package me.khajiitos.jackseconomy.blockentity;

import me.khajiitos.jackseconomy.block.AdminShopBlock;
import me.khajiitos.jackseconomy.data.AdminShopColorManager;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdminShopBlockEntity extends BlockEntity {
	private @Nullable String name;

	public AdminShopBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(BlockEntityReg.ADMIN_SHOP.get(), pPos, pBlockState);
	}

	@Override
	public void load(CompoundTag tag) {
		name = tag.contains("adminShopName", Tag.TAG_STRING) ? tag.getString("adminShopName") : null;
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		if (name != null) tag.putString("adminShopName", name);
	}

	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		if (pkt.getTag() != null) {
			this.load(pkt.getTag());
		}

        Minecraft mc = Minecraft.getInstance();
        BlockPos pos = pkt.getPos();
        BlockState state = mc.level.getBlockState(pos);

        // Force minecraft to re-render the block
        mc.level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
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
	public @NotNull CompoundTag getUpdateTag() {
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
		boolean colored = AdminShopColorManager.getColor(blockEntity.name) != -1;
		if (colored == blockState.getValue(AdminShopBlock.COLORED) || !(level instanceof ServerLevel serverLevel)) return;
		BlockState newState = blockState.setValue(AdminShopBlock.COLORED, colored);
		serverLevel.setBlockAndUpdate(blockPos, newState);
		serverLevel.sendBlockUpdated(blockPos, blockState, newState, 0); // flags apparently are not used
	}
}
