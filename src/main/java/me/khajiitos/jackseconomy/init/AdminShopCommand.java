package me.khajiitos.jackseconomy.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.arguments.AdminShopArgument;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.AdminShopColorManager;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
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
                        .then(Commands.argument("admin_shop_name", AdminShopArgument.greedyString())
                                .executes(AdminShopCommand::openNamed)
                        )
                )
                .then(Commands.literal("name")
                        .requires(stack -> stack.hasPermission(4))
                        .then(Commands.argument("admin_shop_name", AdminShopArgument.greedyString())
                                .executes(AdminShopCommand::setName)
                        )
                        .executes(AdminShopCommand::resetName)
                )
                .then(Commands.literal("color")
                        .requires(stack -> stack.hasPermission(4))
                        .then(Commands.argument("color", ColorArgument.color())
                                .then(Commands.argument("admin_shop_name", AdminShopArgument.greedyString())
                                        .executes(ctx -> setColor(ctx, false))
                                )
                                .executes(ctx -> setColor(ctx, true))
                        )
                        .then(Commands.argument("hex_code", StringArgumentType.word())
                                .then(Commands.argument("admin_shop_name", AdminShopArgument.greedyString())
                                        .executes(ctx -> setColorFromHex(ctx, true))
                                )
                                .executes(ctx -> setColorFromHex(ctx, true))
                        )
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
            String name = AdminShopArgument.getName(ctx, "admin_shop_name");

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

        String name = AdminShopArgument.getName(ctx, "admin_shop_name");

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

    private static int setColor(CommandContext<CommandSourceStack> ctx, boolean isDefault) {
        String name = isDefault ? null : AdminShopArgument.getName(ctx, "admin_shop_name");
        ChatFormatting formatting = ColorArgument.getColor(ctx, "color");

        if (formatting == ChatFormatting.RESET) return resetColor(ctx, isDefault);

        AdminShopColorManager.setColor(name, formatting.getColor());
        ctx.getSource().sendSuccess(
                () -> (
                        isDefault
                                ? Component.translatable("jackseconomy.set_default_admin_shop_color", formatting.getName())
                                : Component.translatable("jackseconomy.set_admin_shop_color", name, formatting.getName())
                ).withStyle(ChatFormatting.GREEN),
                false
        );
        return 1;
    }
    private static int setColorFromHex(CommandContext<CommandSourceStack> ctx, boolean isDefault) {
        try {
            String name = isDefault ? null : AdminShopArgument.getName(ctx, "admin_shop_name");
            String color = StringArgumentType.getString(ctx, "hex_code");

            AdminShopColorManager.setColorFromHex(name, color);
            ctx.getSource().sendSuccess(
                    () -> (
                            isDefault
                                    ? Component.translatable("jackseconomy.set_default_admin_shop_color", color)
                                    : Component.translatable("jackseconomy.set_admin_shop_color", name, color)
                    ).withStyle(ChatFormatting.GREEN),
                    false
            );
        } catch (NumberFormatException e) {
            JacksEconomy.LOGGER.warn("Failed to parse color!", e);
            ctx.getSource().sendFailure(Component.translatable("jackseconomy.failed_to_parse_color"));
        }
        return 1;
    }
    private static int resetColor(CommandContext<CommandSourceStack> ctx, boolean isDefault) {
        String name = isDefault ? null : AdminShopArgument.getName(ctx, "admin_shop_name");

        AdminShopColorManager.resetColor(name);
        ctx.getSource().sendSuccess(
                () -> (
                        isDefault
                                ? Component.translatable("jackseconomy.reset_default_admin_shop_color")
                                : Component.translatable("jackseconomy.reset_admin_shop_color", name)
                ).withStyle(ChatFormatting.GREEN),
                false
        );
        return 1;
    }
}
