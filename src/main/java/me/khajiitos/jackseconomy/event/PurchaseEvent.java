package me.khajiitos.jackseconomy.event;

import me.khajiitos.jackseconomy.data.PurchaseManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

import java.util.Collections;
import java.util.List;

public abstract class PurchaseEvent extends Event {
	private final List<PurchaseManager.Purchase> purchaseList;

	protected PurchaseEvent(List<PurchaseManager.Purchase> purchases) {
		this.purchaseList = Collections.unmodifiableList(purchases);
	}

	public List<PurchaseManager.Purchase> getPurchases() {
		return purchaseList;
	}

	public static class Block extends PurchaseEvent {
		private final BlockPos pos;
		private final ServerLevel level;

		public Block(List<PurchaseManager.Purchase> purchases, BlockPos pos, ServerLevel level) {
			super(purchases);
			this.pos = pos;
			this.level = level;
		}

		public BlockPos getPos() {
			return pos;
		}

		public ServerLevel getLevel() {
			return level;
		}
	}

	public static class Player extends PurchaseEvent {
		private final ServerPlayer buyer;

		public Player(List<PurchaseManager.Purchase> purchases, ServerPlayer buyer) {
			super(purchases);
			this.buyer = buyer;
		}

		public ServerPlayer getBuyer() {
			return buyer;
		}
	}
}
