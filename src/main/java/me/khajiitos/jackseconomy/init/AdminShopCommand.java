package me.khajiitos.jackseconomy.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;

public class AdminShopCommand {

    public static void init(IEventBus eventBus) {
        eventBus.register(AdminShopCommand.class);
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent e) {
        register(e.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("adminshop").requires(stack -> stack.hasPermission(4) || Config.adminShopCommandForEveryone.get()).executes(AdminShopCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player != null) {
            CompoundTag compoundTag = PriceManager.toAdminShopSchemaCompound(player);
            NetworkHooks.openScreen(player, new SimpleMenuProvider((pContainerId, pPlayerInventory, pPlayer) -> new AdminShopMenu(pContainerId, pPlayerInventory), Component.empty()));
            Packets.sendToClient(player, new AdminShopSchemaPacket(compoundTag));
        }

        return 0;
    }
}
