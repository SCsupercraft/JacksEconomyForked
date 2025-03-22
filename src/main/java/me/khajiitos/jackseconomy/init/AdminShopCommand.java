package me.khajiitos.jackseconomy.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
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
        dispatcher.register(Commands.literal("adminshop")
                .requires(stack -> stack.hasPermission(4) || Config.adminShopCommandForEveryone.get())
                .then(Commands.literal("default")
                        .requires(stack -> stack.hasPermission(4))
                        .executes(AdminShopCommand::openDefault)
                )
                .then(Commands.literal("named")
                        .requires(stack -> stack.hasPermission(4))
                        .then(Commands.argument("admin_shop_name", StringArgumentType.greedyString())
                                .executes(AdminShopCommand::openNamed)
                        )
                )
                .then(Commands.literal("name")
                        .requires(stack -> stack.hasPermission(4))
                        .then(Commands.argument("admin_shop_name", StringArgumentType.greedyString())
                                .executes(AdminShopCommand::setName)
                        )
                        .executes(AdminShopCommand::resetName)
                )
                .executes(AdminShopCommand::openBasedOnConfig));
    }

    private static int openBasedOnConfig(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player != null) {
            String configOpt = Config.adminShopCommandShopName.get();
            String name = configOpt.equals("") ? null : configOpt;

            CompoundTag compoundTag = PriceManager.toAdminShopSchemaCompound(player, name);
            NetworkHooks.openScreen(player, new SimpleMenuProvider((pContainerId, pPlayerInventory, pPlayer) -> new AdminShopMenu(pContainerId, pPlayerInventory), Component.empty()));
            Packets.sendToClient(player, new AdminShopSchemaPacket(compoundTag, name));
        }

        return 0;
    }

    private static int openDefault(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player != null) {
            CompoundTag compoundTag = PriceManager.toAdminShopSchemaCompound(player, null);
            NetworkHooks.openScreen(player, new SimpleMenuProvider((pContainerId, pPlayerInventory, pPlayer) -> new AdminShopMenu(pContainerId, pPlayerInventory), Component.empty()));
            Packets.sendToClient(player, new AdminShopSchemaPacket(compoundTag, null));
        }

        return 0;
    }

    private static int openNamed(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player != null) {
            String name = StringArgumentType.getString(ctx, "admin_shop_name");

            CompoundTag compoundTag = PriceManager.toAdminShopSchemaCompound(player, name);
            NetworkHooks.openScreen(player, new SimpleMenuProvider((pContainerId, pPlayerInventory, pPlayer) -> new AdminShopMenu(pContainerId, pPlayerInventory), Component.empty()));
            Packets.sendToClient(player, new AdminShopSchemaPacket(compoundTag, name));
        }

        return 0;
    }

    private static int setName(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player == null) {
            return 1;
        }

        ItemStack itemInHand = player.getMainHandItem();

        if (itemInHand.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("jackseconomy.hold_an_admin_shop").withStyle(ChatFormatting.RED));
            return 1;
        }

        String name = StringArgumentType.getString(ctx, "admin_shop_name");

        CompoundTag tag = BlockItem.getBlockEntityData(itemInHand);
        if (tag != null && tag.contains("adminShopName") && tag.getString("adminShopName").equals(name)) return 1;
        if (tag == null) tag = new CompoundTag();

        tag.putString("adminShopName", name);

        BlockItem.setBlockEntityData(itemInHand, BlockEntityReg.ADMIN_SHOP.get(), tag);
        ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.set_admin_shop_name", name).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int resetName(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();

        if (player == null) {
            return 1;
        }

        ItemStack itemInHand = player.getMainHandItem();

        if (itemInHand.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("jackseconomy.hold_an_admin_shop").withStyle(ChatFormatting.RED));
            return 1;
        }

        CompoundTag tag = BlockItem.getBlockEntityData(itemInHand);
        if (tag == null || !tag.contains("adminShopName")) return 1;

        tag.remove("adminShopName");

        BlockItem.setBlockEntityData(itemInHand, BlockEntityReg.ADMIN_SHOP.get(), tag);
        ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.reset_admin_shop_name").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}
