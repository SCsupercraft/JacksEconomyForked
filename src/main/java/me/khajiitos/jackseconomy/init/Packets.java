package me.khajiitos.jackseconomy.init;

import me.khajiitos.jackseconomy.packet.*;
import me.khajiitos.jackseconomy.packet.handler.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class Packets {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(IEventBus eventBus) {
        eventBus.addListener(Packets::registerPayloads);
    }

    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(             ChangeSpeedPacket.TYPE,             ChangeSpeedPacket.STREAM_CODEC,             ChangeSpeedHandler::handle);
        registrar.playToServer(    ChangeRedstoneTogglePacket.TYPE,    ChangeRedstoneTogglePacket.STREAM_CODEC,    ChangeRedstoneToggleHandler::handle);
        registrar.playToServer(             CreateCheckPacket.TYPE,             CreateCheckPacket.STREAM_CODEC,             CreateCheckHandler::handle);
        registrar.playToServer( WithdrawBalanceSpecificPacket.TYPE, WithdrawBalanceSpecificPacket.STREAM_CODEC, WithdrawBalanceSpecificHandler::handle);
        registrar.playToServer(        OpenCuriosWalletPacket.TYPE,        OpenCuriosWalletPacket.STREAM_CODEC,        OpenCuriosWalletHandler::handle);
        registrar.playToServer(      ChangeCurrencyTypePacket.TYPE,      ChangeCurrencyTypePacket.STREAM_CODEC,      ChangeCurrencyTypeHandler::handle);
        registrar.playToServer(       AdminShopPurchasePacket.TYPE,       AdminShopPurchasePacket.STREAM_CODEC,       AdminShopPurchaseHandler::handle);
        registrar.playToServer(      ChangeSelectedItemPacket.TYPE,      ChangeSelectedItemPacket.STREAM_CODEC,      ChangeSelectedItemHandler::handle);
        registrar.playToServer(     ChangeSelectedFluidPacket.TYPE,     ChangeSelectedFluidPacket.STREAM_CODEC,     ChangeSelectedFluidHandler::handle);
        registrar.playToServer(         UpdateAdminShopPacket.TYPE,         UpdateAdminShopPacket.STREAM_CODEC,         UpdateAdminShopHandler::handle);
        registrar.playToServer(        UpdateSideConfigPacket.TYPE,        UpdateSideConfigPacket.STREAM_CODEC,        UpdateSideConfigHandler::handle);
        registrar.playToServer(          InsertToWalletPacket.TYPE,          InsertToWalletPacket.STREAM_CODEC,          InsertToWalletHandler::handle);
        registrar.playToServer(              DepositAllPacket.TYPE,              DepositAllPacket.STREAM_CODEC,              DepositAllHandler::handle);
        registrar.playToServer(      AcknowledgeUnlocksPacket.TYPE,      AcknowledgeUnlocksPacket.STREAM_CODEC,      AcknowledgeUnlocksHandler::handle); // TODO: Find GameStages alternative
        registrar.playToServer(  RequestAdminShopSchemaPacket.TYPE,  RequestAdminShopSchemaPacket.STREAM_CODEC,  RequestAdminShopSchemaHandler::handle);
        registrar.playToServer(        UpdateItemPricesPacket.TYPE,        UpdateItemPricesPacket.STREAM_CODEC,        UpdateItemPricesHandler::handle);
        registrar.playToServer(       UpdateFluidPricesPacket.TYPE,       UpdateFluidPricesPacket.STREAM_CODEC,       UpdateFluidPricesHandler::handle);
        registrar.playToClient(              PricesInfoPacket.TYPE,              PricesInfoPacket.STREAM_CODEC,              PricesInfoHandler::handle);
        registrar.playToClient(     UpdateWalletBalancePacket.TYPE,     UpdateWalletBalancePacket.STREAM_CODEC,     UpdateWalletBalanceHandler::handle);
        registrar.playToClient(        WalletBalanceDifPacket.TYPE,        WalletBalanceDifPacket.STREAM_CODEC,        WalletBalanceDifHandler::handle);
        registrar.playToClient(         AdminShopSchemaPacket.TYPE,         AdminShopSchemaPacket.STREAM_CODEC,         AdminShopSchemaHandler::handle);
        registrar.playToClient(          AdminShopColorPacket.TYPE,          AdminShopColorPacket.STREAM_CODEC,          AdminShopColorHandler::handle);
    }
}
