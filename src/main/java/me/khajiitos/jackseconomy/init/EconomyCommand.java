package me.khajiitos.jackseconomy.init;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.data.PurchaseManager;
import me.khajiitos.jackseconomy.data.price.*;
import me.khajiitos.jackseconomy.item.GoldenWalletItem;
import me.khajiitos.jackseconomy.item.TicketItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.screen.BulkFluidScreen;
import me.khajiitos.jackseconomy.screen.BulkItemScreen;
import me.khajiitos.jackseconomy.screen.BulkAdminShopScreen;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.NetworkHooks;

import java.math.BigDecimal;

public class EconomyCommand {
	// TODO: Purchases command
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("economy").requires(stack -> stack.hasPermission(Commands.LEVEL_OWNERS))
				.then(ManifestCommand.command)
				.then(PriceCommand.command)
				.then(PurchasesCommand.command)
                .then(PlayerCommand.command)
				.then(Commands.literal("reset_all").executes(EconomyCommand::resetAll))
		);
	}

	private static int resetAll(CommandContext<CommandSourceStack> ctx) {
		AdminShopCommand.resetAll(ctx);
		PriceCommand.resetPrices(ctx);
		PurchasesCommand.resetPurchases(ctx);
		ctx.getSource().sendSystemMessage(Component.translatable("jackseconomy.reset_all_data").withStyle(ChatFormatting.RED));
		return 1;
	}

	private static class PriceCommand {
		public static LiteralArgumentBuilder<CommandSourceStack> command =
				Commands.literal("price")
						.then(Commands.literal("set")
								.then(Commands.literal("exporter").then(Commands.argument("price", DoubleArgumentType.doubleArg(-1.0)).executes(PriceCommand::setExporterPrice).then(Commands.literal("strip_nbt").executes(PriceCommand::setExporterPriceStripNbt))))
								.then(Commands.literal("importer").then(Commands.argument("price", DoubleArgumentType.doubleArg(-1.0)).executes(PriceCommand::setImporterPrice).then(Commands.literal("strip_nbt").executes(PriceCommand::setImporterPriceStripNbt))))
								.then(Commands.literal("fluid_exporter").then(Commands.argument("price_per_mB", DoubleArgumentType.doubleArg(-1.0)).executes(PriceCommand::setFluidExporterPrice).then(Commands.literal("strip_nbt").executes(PriceCommand::setFluidExporterPriceStripNbt))))
								.then(Commands.literal("fluid_importer").then(Commands.argument("price_per_mB", DoubleArgumentType.doubleArg(-1.0)).executes(PriceCommand::setFluidImporterPrice).then(Commands.literal("strip_nbt").executes(PriceCommand::setFluidImporterPriceStripNbt))))
						)
						.then(Commands.literal("bulk")
								.then(Commands.literal("adminshop").executes(PriceCommand::bulkSetAdminShopPrices))
								.then(Commands.literal("item").executes(PriceCommand::bulkSetItemPrices))
								.then(Commands.literal("fluid").executes(PriceCommand::bulkSetFluidPrices))
						)
						.then(Commands.literal("reload")
								.executes(PriceCommand::reloadPrices)
						)
						.then(Commands.literal("reset")
								.executes(PriceCommand::resetPrices)
						);

		private static int resetPrices(CommandContext<CommandSourceStack> ctx) {
			PriceManager.resetData();
			ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.reset_prices_data").withStyle(ChatFormatting.RED), true);
			return 1;
		}

		private static int reloadPrices(CommandContext<CommandSourceStack> ctx) {
			PriceManager.load();
			ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.prices_reloaded").withStyle(ChatFormatting.GREEN), true);
			return 1;
		}

		private static int setImporterPrice(CommandContext<CommandSourceStack> ctx) {
			return setImporterPrice(ctx, false);
		}

		private static int setExporterPrice(CommandContext<CommandSourceStack> ctx) {
			return setExporterPrice(ctx, false);
		}

		private static int setImporterPriceStripNbt(CommandContext<CommandSourceStack> ctx) {
			return setImporterPrice(ctx, true);
		}

		private static int setExporterPriceStripNbt(CommandContext<CommandSourceStack> ctx) {
			return setExporterPrice(ctx, true);
		}

		private static int setFluidImporterPrice(CommandContext<CommandSourceStack> ctx) {
			return setFluidImporterPrice(ctx, false);
		}

		private static int setFluidExporterPrice(CommandContext<CommandSourceStack> ctx) {
			return setFluidExporterPrice(ctx, false);
		}

		private static int setFluidImporterPriceStripNbt(CommandContext<CommandSourceStack> ctx) {
			return setFluidImporterPrice(ctx, true);
		}

		private static int setFluidExporterPriceStripNbt(CommandContext<CommandSourceStack> ctx) {
			return setFluidExporterPrice(ctx, true);
		}

		private static int setImporterPrice(CommandContext<CommandSourceStack> ctx, boolean stripNbt) {
			ServerPlayer player = ctx.getSource().getPlayer();

			if (player == null) {
				return 1;
			}

			double priceArg = DoubleArgumentType.getDouble(ctx, "price");
			final double price = priceArg <= 0 ? -1.0 : priceArg;

			ItemStack itemInHand = player.getMainHandItem();

			if (itemInHand.isEmpty()) {
				ctx.getSource().sendFailure(Component.translatable("jackseconomy.hold_an_item").withStyle(ChatFormatting.RED));
				return 1;
			}

			PricesItemPriceInfo existingInfo = PriceManager.getPricesInfo(new ItemDescription(itemInHand.getItem(), stripNbt ? null : itemInHand.getTag()));

			if (existingInfo != null) {
				existingInfo.importerBuyPrice = price;
			} else {
				PriceManager.addPriceInfo(itemInHand, new PricesItemPriceInfo(-1, -1, price, null, null));
			}

			if (price > 0) {
				ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.importer_price_set", itemInHand.getItem().getDescription().copy().withStyle(ChatFormatting.YELLOW), Component.literal(CurrencyHelper.format(price)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD), true);
			} else {
				ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.importer_price_removed", itemInHand.getItem().getDescription().copy().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD), true);
			}

			PriceManager.save();
			PriceManager.sendDataToPlayers(false);

			return 0;
		}

		private static int setExporterPrice(CommandContext<CommandSourceStack> ctx, boolean stripNbt) {
			ServerPlayer player = ctx.getSource().getPlayer();

			if (player == null) {
				return 1;
			}

			double priceArg = DoubleArgumentType.getDouble(ctx, "price");
			final double price = priceArg <= 0 ? -1.0 : priceArg;

			ItemStack itemInHand = player.getMainHandItem();

			if (itemInHand.isEmpty()) {
				ctx.getSource().sendFailure(Component.translatable("jackseconomy.hold_an_item").withStyle(ChatFormatting.RED));
				return 1;
			}

			PricesItemPriceInfo existingInfo = PriceManager.getPricesInfo(new ItemDescription(itemInHand.getItem(), stripNbt ? null : itemInHand.getTag()));

			if (existingInfo != null) {
				existingInfo.sellPrice = price;
			} else {
				PriceManager.addPriceInfo(itemInHand, new PricesItemPriceInfo(price, -1, -1, null, null));
			}

			if (price > 0) {
				ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.exporter_price_set", itemInHand.getItem().getDescription().copy().withStyle(ChatFormatting.YELLOW), Component.literal(CurrencyHelper.format(price)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD), true);
			} else {
				ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.exporter_price_removed", itemInHand.getItem().getDescription().copy().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD), true);
			}

			PriceManager.save();
			PriceManager.sendDataToPlayers(false);

			return 0;
		}

		private static int setFluidImporterPrice(CommandContext<CommandSourceStack> ctx, boolean stripNbt) {
			ServerPlayer player = ctx.getSource().getPlayer();

			if (player == null) {
				return 1;
			}

			double priceArg = DoubleArgumentType.getDouble(ctx, "price_per_mB");
			final double price = priceArg <= 0 ? -1.0 : priceArg;

			ItemStack itemInHand = player.getMainHandItem();

			if (itemInHand.isEmpty()) {
				ctx.getSource().sendFailure(Component.translatable("jackseconomy.hold_an_item").withStyle(ChatFormatting.RED));
				return 1;
			}
			if (!itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
				ctx.getSource().sendFailure(Component.translatable("jackseconomy.hold_an_item_with_fluid_content").withStyle(ChatFormatting.RED));
				return 1;
			}

			IFluidHandlerItem fluidTank = itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(RuntimeException::new);

			FluidStack fluidContents = fluidTank.getFluidInTank(0);
			PricesFluidPriceInfo existingInfo = PriceManager.getPricesInfo(new FluidDescription(fluidContents.getFluid(), stripNbt ? null : fluidContents.getTag()));

			if (existingInfo != null) {
				existingInfo.importerBuyPrice = price;
			} else {
				PriceManager.addPriceInfo(fluidContents, new PricesFluidPriceInfo(-1, price));
			}

			if (price > 0) {
				ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.fluid_importer_price_set", fluidContents.getFluid().getFluidType().getDescription().copy().withStyle(ChatFormatting.YELLOW), Component.literal(CurrencyHelper.format(price)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD), true);
			} else {
				ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.fluid_importer_price_removed", fluidContents.getFluid().getFluidType().getDescription().copy().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD), true);
			}

			PriceManager.save();
			PriceManager.sendDataToPlayers(false);

			return 0;
		}

		private static int setFluidExporterPrice(CommandContext<CommandSourceStack> ctx, boolean stripNbt) {
			ServerPlayer player = ctx.getSource().getPlayer();

			if (player == null) {
				return 1;
			}

			double priceArg = DoubleArgumentType.getDouble(ctx, "price_per_mB");
			final double price = priceArg <= 0 ? -1.0 : priceArg;

			ItemStack itemInHand = player.getMainHandItem();

			if (itemInHand.isEmpty()) {
				ctx.getSource().sendFailure(Component.translatable("jackseconomy.hold_an_item").withStyle(ChatFormatting.RED));
				return 1;
			}
			if (!itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
				ctx.getSource().sendFailure(Component.translatable("jackseconomy.hold_an_item_with_fluid_content").withStyle(ChatFormatting.RED));
				return 1;
			}

			IFluidHandlerItem fluidTank = itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(RuntimeException::new);

			FluidStack fluidContents = fluidTank.getFluidInTank(0);
			PricesFluidPriceInfo existingInfo = PriceManager.getPricesInfo(new FluidDescription(fluidContents.getFluid(), stripNbt ? null : fluidContents.getTag()));

			if (existingInfo != null) {
				existingInfo.sellPrice = price;
			} else {
				PriceManager.addPriceInfo(fluidContents, new PricesFluidPriceInfo(price, -1));
			}

			if (price > 0) {
				ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.fluid_exporter_price_set", fluidContents.getFluid().getFluidType().getDescription().copy().withStyle(ChatFormatting.YELLOW), Component.literal(CurrencyHelper.format(price)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD), true);
			} else {
				ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.fluid_exporter_price_removed", fluidContents.getFluid().getFluidType().getDescription().copy().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GOLD), true);
			}

			PriceManager.save();
			PriceManager.sendDataToPlayers(false);

			return 0;
		}

		private static int bulkSetAdminShopPrices(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
			ServerPlayer player = ctx.getSource().getPlayer();
			if (player == null) return 1;

			if (!player.isCreative()) {
				throw new SimpleCommandExceptionType(Component.translatable("jackseconomy.bulk_admin_shop_not_creative")).create();
			}

			NetworkHooks.openScreen(player, new SimpleMenuProvider((pContainerId, pPlayerInventory, pPlayer) -> new BulkAdminShopScreen.Menu(pContainerId, pPlayerInventory), Component.empty()));
			return 1;
		}

		private static int bulkSetItemPrices(CommandContext<CommandSourceStack> ctx) {
			ServerPlayer player = ctx.getSource().getPlayer();
			if (player == null) return 1;

			NetworkHooks.openScreen(player, new SimpleMenuProvider((pContainerId, pPlayerInventory, pPlayer) -> new BulkItemScreen.Menu(pContainerId, pPlayerInventory), Component.empty()));
			return 1;
		}

		private static int bulkSetFluidPrices(CommandContext<CommandSourceStack> ctx) {
			ServerPlayer player = ctx.getSource().getPlayer();
			if (player == null) return 1;

			NetworkHooks.openScreen(player, new SimpleMenuProvider((pContainerId, pPlayerInventory, pPlayer) -> new BulkFluidScreen.Menu(pContainerId, pPlayerInventory), Component.empty()));
			return 1;
		}
	}

	private static class PurchasesCommand {
		public static LiteralArgumentBuilder<CommandSourceStack> command =
				Commands.literal("purchases")
						.then(Commands.literal("reset").executes(PurchasesCommand::resetPurchases));

		private static int resetPurchases(CommandContext<CommandSourceStack> ctx) {
			PurchaseManager.resetData();
			ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.reset_purchase_data").withStyle(ChatFormatting.RED), true);
			return 1;
		}
	}

	private static class ManifestCommand {
		public static LiteralArgumentBuilder<CommandSourceStack> command =
				Commands.literal("manifest")
						.then(
								Commands.literal("max_process_count").executes(ManifestCommand::getMaxProcessCount).then(
										Commands.argument("count", IntegerArgumentType.integer(1, 100000)).executes(ManifestCommand::setMaxProcessCount)
								)
						)
						.then(
								Commands.literal("max_uses").executes(ManifestCommand::getMaxUsage)
										.then(
												Commands.literal("set").then(Commands.argument("count", IntegerArgumentType.integer(1, 1000000)).executes(ManifestCommand::setMaxUsage))
										)
										.then(
												Commands.literal("remove").executes(ManifestCommand::removeMaxUsage)
										)
						);

		private static int getMaxProcessCount(CommandContext<CommandSourceStack> ctx) {
			CommandSourceStack source = ctx.getSource();
			ServerPlayer player = source.getPlayer();

			if (player == null) return 1;

			ItemStack ticketItemStack = player.getMainHandItem();
			if (!(ticketItemStack.getItem() instanceof TicketItem)) {
				source.sendSystemMessage(Component.translatable("jackseconomy.hold_a_manifest").withStyle(ChatFormatting.RED));
				return 1;
			}

			int maxProcessCount = TicketItem.getMaxProcessCount(ticketItemStack);

			source.sendSuccess(() -> Component.translatable("jackseconomy.get_ticket_process_count", maxProcessCount).withStyle(ChatFormatting.GREEN), false);
			return 1;
		}

		private static int setMaxProcessCount(CommandContext<CommandSourceStack> ctx) {
			int maxProcessCount = IntegerArgumentType.getInteger(ctx, "count");
			CommandSourceStack source = ctx.getSource();
			ServerPlayer player = source.getPlayer();

			if (player == null) return 1;

			ItemStack ticketItemStack = player.getMainHandItem();
			if (ticketItemStack.getItem() instanceof TicketItem) {
				TicketItem.setMaxProcessCount(ticketItemStack, maxProcessCount);
			} else {
				source.sendFailure(Component.translatable("jackseconomy.hold_a_manifest").withStyle(ChatFormatting.RED));
				return 1;
			}

			source.sendSuccess(() -> Component.translatable("jackseconomy.set_ticket_process_count", maxProcessCount).withStyle(ChatFormatting.GREEN), false);
			return 1;
		}

		private static int getMaxUsage(CommandContext<CommandSourceStack> ctx) {
			CommandSourceStack source = ctx.getSource();
			ServerPlayer player = source.getPlayer();

			if (player == null) return 1;

			ItemStack ticketItemStack = player.getMainHandItem();

			if (!(ticketItemStack.getItem() instanceof TicketItem)) {
				source.sendSystemMessage(Component.translatable("jackseconomy.hold_a_manifest").withStyle(ChatFormatting.RED));
				return 1;
			}

			int maxUsage = TicketItem.getMaxUsage(ticketItemStack);

			source.sendSuccess(() -> Component.translatable("jackseconomy.get_ticket_max_usage", maxUsage).withStyle(ChatFormatting.GREEN), false);
			return 1;
		}

		private static int setMaxUsage(CommandContext<CommandSourceStack> ctx) {
			int maxUsage = IntegerArgumentType.getInteger(ctx, "count");
			CommandSourceStack source = ctx.getSource();
			ServerPlayer player = source.getPlayer();

			if (player == null) return 1;

			ItemStack ticketItemStack = player.getMainHandItem();
			if (ticketItemStack.getItem() instanceof TicketItem) {
				TicketItem.setMaxUsage(ticketItemStack, maxUsage);
			} else {
				source.sendFailure(Component.translatable("jackseconomy.hold_a_manifest").withStyle(ChatFormatting.RED));
				return 1;
			}

			source.sendSuccess(() -> Component.translatable("jackseconomy.set_ticket_max_usage", maxUsage).withStyle(ChatFormatting.GREEN), false);
			return 1;
		}

		private static int removeMaxUsage(CommandContext<CommandSourceStack> ctx) {
			CommandSourceStack source = ctx.getSource();
			ServerPlayer player = source.getPlayer();

			if (player == null) return 1;

			ItemStack ticketItemStack = player.getMainHandItem();
			if (ticketItemStack.getItem() instanceof TicketItem) {
				TicketItem.removeMaxUsage(ticketItemStack);
			} else {
				source.sendFailure(Component.translatable("jackseconomy.hold_a_manifest").withStyle(ChatFormatting.RED));
				return 1;
			}

			source.sendSuccess(() -> Component.translatable("jackseconomy.remove_ticket_max_usage").withStyle(ChatFormatting.GREEN), false);
			return 1;
		}
	}

    private static class PlayerCommand {
        public static LiteralArgumentBuilder<CommandSourceStack> command =
                Commands.literal("player")
                        .requires(stack -> !Config.oneItemCurrencyMode.get())
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(apply("give", PlayerCommand::give))
                                .then(apply("take", PlayerCommand::take))
                                .then(Commands.literal("clear").executes(PlayerCommand::clear))
                                .then(Commands.literal("count").executes(PlayerCommand::count))
                        );

        private static LiteralArgumentBuilder<CommandSourceStack> apply(String name, Command<CommandSourceStack> command) {
            return Commands.literal(name)
                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                            .executes(command));
        }

        private static int give(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
            double money = DoubleArgumentType.getDouble(ctx, "amount");

            ItemStack stack = CuriosWallet.get(player);
            if (stack.isEmpty()) {
                ctx.getSource().sendFailure(Component.translatable("jackseconomy.player_no_wallet", player.getDisplayName()));
                return 0;
            }

            CurrencyHelper.addMoney(BigDecimal.valueOf(money), stack, player);
            ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.player_give_money", CurrencyHelper.formatShortened(money), player.getDisplayName()), true);
            return 1;
        }

        private static int take(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
            double money = DoubleArgumentType.getDouble(ctx, "amount");

            ItemStack stack = CuriosWallet.get(player);
            if (stack.isEmpty()) {
                ctx.getSource().sendFailure(Component.translatable("jackseconomy.player_no_wallet", player.getDisplayName()));
                return 0;
            }

            if (stack.getItem() instanceof WalletItem) {
                BigDecimal balance = WalletItem.getBalance(stack);
                WalletItem.setBalance(stack, balance.subtract(BigDecimal.valueOf(money)).max(BigDecimal.ZERO));
            }

            ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.player_remove_money", CurrencyHelper.formatShortened(money), player.getDisplayName()), true);
            return 1;
        }

        private static int clear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "target");

            ItemStack stack = CuriosWallet.get(player);
            if (stack.isEmpty()) {
                ctx.getSource().sendFailure(Component.translatable("jackseconomy.player_no_wallet", player.getDisplayName()));
                return 0;
            }

            BigDecimal cleared;
            if (stack.getItem() instanceof WalletItem) {
                cleared = WalletItem.getBalance(stack);
                WalletItem.setBalance(stack, BigDecimal.ZERO);
            } else cleared = BigDecimal.ZERO;

            ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.player_cleared_money", CurrencyHelper.formatShortened(cleared), player.getDisplayName()), true);
            return 1;
        }

        private static int count(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "target");

            ItemStack stack = CuriosWallet.get(player);
            if (stack.isEmpty()) {
                ctx.getSource().sendFailure(Component.translatable("jackseconomy.player_no_wallet", player.getDisplayName()));
                return 0;
            }

            Component count;
            if (stack.getItem() instanceof WalletItem) {
                count = Component.literal(CurrencyHelper.formatShortened(WalletItem.getBalance(stack)));
            } else if (stack.getItem() instanceof GoldenWalletItem) {
                count = Component.literal("$").append(Component.translatable("jackseconomy.infinite"));
            } else return 0;

            ctx.getSource().sendSuccess(() -> Component.translatable("jackseconomy.player_count_money", player.getDisplayName(), count), true);
            return 1;
        }
    }
}
