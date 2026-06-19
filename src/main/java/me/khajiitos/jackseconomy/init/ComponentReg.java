package me.khajiitos.jackseconomy.init;

import com.mojang.serialization.Codec;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.util.Utils;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ComponentReg {
	public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, JacksEconomy.MOD_ID);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BigDecimal>> BALANCE = COMPONENTS.registerComponentType(
			"balance",
			builder -> builder
					.persistent(Utils.BIG_DECIMAL_CODEC)
					.networkSynchronized(Utils.BIG_DECIMAL_STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> OIM_WALLET_BALANCE = COMPONENTS.registerComponentType(
		"oim_wallet_balance",
		builder -> builder
				.persistent(CompoundTag.CODEC)
				.networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemDescription>>> TICKET_ITEMS = COMPONENTS.registerComponentType(
			"ticket_items",
			builder -> builder
					.persistent(Codec.list(ItemDescription.CODEC))
					.networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemDescription.STREAM_CODEC))
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<FluidDescription>>> TICKET_FLUIDS = COMPONENTS.registerComponentType(
			"ticket_fluids",
			builder -> builder
					.persistent(Codec.list(FluidDescription.CODEC))
					.networkSynchronized(ByteBufCodecs.collection(ArrayList::new, FluidDescription.STREAM_CODEC))
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAX_PROCESS_COUNT = COMPONENTS.registerComponentType(
			"max_process_count",
			builder -> builder
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.INT)
	);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> GHOST = COMPONENTS.registerComponentType(
            "ghost",
            builder -> builder
                    .persistent(Unit.CODEC)
    );

	public static void register(IEventBus bus) {
		COMPONENTS.register(bus);
	}

    // Ghost Items (JEI)

    public static boolean isGhostItem(ItemStack stack) {
        return stack.getComponents().has(GHOST.get());
    }

    public static void addGhostItem(ItemStack stack) {
        stack.applyComponents(DataComponentPatch.builder().set(GHOST.get(), Unit.INSTANCE).build());
    }

    public static void removeGhostItem(ItemStack stack) {
        stack.applyComponents(DataComponentPatch.builder().remove(GHOST.get()).build());
    }

    public static class GhostSlot extends Slot {
        public GhostSlot(Container pContainer, int pSlot, int pX, int pY) {
            super(pContainer, pSlot, pX, pY);
        }

        @Override
        public void onTake(Player pPlayer, ItemStack pStack) {
            if (isGhostItem(pStack)) {
                pStack.setCount(0);
                removeGhostItem(pStack);
            }
            super.onTake(pPlayer, pStack);
        }
    }
}
