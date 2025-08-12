package me.khajiitos.jackseconomy.init;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class Packets {
    private static int packetCount;

    private static final String PROTOCOL_VERSION = "1";
    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(JacksEconomy.MOD_ID, "main");
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            CHANNEL_NAME,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        INSTANCE.registerMessage(packetCount++,             ChangeSpeedPacket.class,             ChangeSpeedPacket::encode,             ChangeSpeedPacket::decode,             ChangeSpeedPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,    ChangeRedstoneTogglePacket.class,    ChangeRedstoneTogglePacket::encode,    ChangeRedstoneTogglePacket::decode,    ChangeRedstoneTogglePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,             CreateCheckPacket.class,             CreateCheckPacket::encode,             CreateCheckPacket::decode,             CreateCheckPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++, WithdrawBalanceSpecificPacket.class, WithdrawBalanceSpecificPacket::encode, WithdrawBalanceSpecificPacket::decode, WithdrawBalanceSpecificPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,        OpenCuriosWalletPacket.class,        OpenCuriosWalletPacket::encode,        OpenCuriosWalletPacket::decode,        OpenCuriosWalletPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,      ChangeCurrencyTypePacket.class,      ChangeCurrencyTypePacket::encode,      ChangeCurrencyTypePacket::decode,      ChangeCurrencyTypePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,       AdminShopPurchasePacket.class,       AdminShopPurchasePacket::encode,       AdminShopPurchasePacket::decode,       AdminShopPurchasePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,      ChangeSelectedItemPacket.class,      ChangeSelectedItemPacket::encode,      ChangeSelectedItemPacket::decode,      ChangeSelectedItemPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,     ChangeSelectedFluidPacket.class,     ChangeSelectedFluidPacket::encode,     ChangeSelectedFluidPacket::decode,     ChangeSelectedFluidPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,         UpdateAdminShopPacket.class,         UpdateAdminShopPacket::encode,         UpdateAdminShopPacket::decode,         UpdateAdminShopPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,        UpdateSideConfigPacket.class,        UpdateSideConfigPacket::encode,        UpdateSideConfigPacket::decode,        UpdateSideConfigPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,          InsertToWalletPacket.class,          InsertToWalletPacket::encode,          InsertToWalletPacket::decode,          InsertToWalletPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,              DepositAllPacket.class,              DepositAllPacket::encode,              DepositAllPacket::decode,              DepositAllPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,      AcknowledgeUnlocksPacket.class,      AcknowledgeUnlocksPacket::encode,      AcknowledgeUnlocksPacket::decode,      AcknowledgeUnlocksPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,  RequestAdminShopSchemaPacket.class,  RequestAdminShopSchemaPacket::encode,  RequestAdminShopSchemaPacket::decode,  RequestAdminShopSchemaPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(packetCount++,              PricesInfoPacket.class,              PricesInfoPacket::encode,              PricesInfoPacket::decode,              PricesInfoPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(packetCount++,     UpdateWalletBalancePacket.class,     UpdateWalletBalancePacket::encode,     UpdateWalletBalancePacket::decode,     UpdateWalletBalancePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(packetCount++,        WalletBalanceDifPacket.class,        WalletBalanceDifPacket::encode,        WalletBalanceDifPacket::decode,        WalletBalanceDifPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(packetCount++,         AdminShopSchemaPacket.class,         AdminShopSchemaPacket::encode,         AdminShopSchemaPacket::decode,         AdminShopSchemaPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(packetCount++,          AdminShopColorPacket.class,          AdminShopColorPacket::encode,          AdminShopColorPacket::decode,          AdminShopColorPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <MSG> void sendToServer(MSG packet) {
        INSTANCE.sendToServer(packet);
    }

    public static <MSG> void sendToClient(ServerPlayer player, MSG packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
