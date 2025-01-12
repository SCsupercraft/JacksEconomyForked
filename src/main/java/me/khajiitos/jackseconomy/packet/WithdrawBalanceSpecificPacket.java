package me.khajiitos.jackseconomy.packet;

import me.khajiitos.jackseconomy.packet.handler.WithdrawBalanceSpecificHandler;
import me.khajiitos.jackseconomy.util.CurrencyType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.math.BigDecimal;
import java.util.function.Supplier;

public record WithdrawBalanceSpecificPacket(BigDecimal items, CurrencyType currencyType) {
    public static void encode(WithdrawBalanceSpecificPacket msg, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(msg.items.toString());
        friendlyByteBuf.writeEnum(msg.currencyType);
    }

    public static WithdrawBalanceSpecificPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new WithdrawBalanceSpecificPacket(new BigDecimal(friendlyByteBuf.readUtf()), friendlyByteBuf.readEnum(CurrencyType.class));
    }

    public static void handle(WithdrawBalanceSpecificPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> WithdrawBalanceSpecificHandler.handle(msg, ctx));
        ctx.get().setPacketHandled(true);
    }
}