package me.khajiitos.jackseconomy.packet.handler;

import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.data.PurchaseManager;
import me.khajiitos.jackseconomy.gamestages.GameStagesManager;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.item.GoldenWalletItem;
import me.khajiitos.jackseconomy.item.OIMWalletItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.packet.AdminShopPurchasePacket;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import me.khajiitos.jackseconomy.util.ItemHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;

public class AdminShopPurchaseHandler {
    public static void handle(AdminShopPurchasePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();

        if (sender == null) {
            return;
        }

        ItemStack wallet = CuriosWallet.get(sender);

        PurchaseManager.Purchases purchases = new PurchaseManager.Purchases(sender.getUUID(), PurchaseManager.PurchaseSource.ADMIN_SHOP);

        BigDecimal value = BigDecimal.ZERO;
        for (Map.Entry<AdminShopPurchasePacket.ShopItemDescription, Integer> entry : msg.shoppingCart().entrySet()) {
            ItemDescription description = entry.getKey().itemDescription();
            double price = PriceManager.getAdminShopBuyPrice(description, entry.getValue(), entry.getKey().slot(), entry.getKey().category(), msg.adminShopName());
            if (price <= 0) {
                return;
            }

            purchases.addPurchase(description, entry.getValue());

            if (Config.oneItemCurrencyMode.get()) {
                value = value.add(BigDecimal.valueOf(Math.round(price)));
            } else {
                value = value.add(BigDecimal.valueOf(price));
            }
        }

        if (!Config.disableAdminShopSelling.get()) {
            for (Map.Entry<ItemDescription, Integer> entry : msg.itemsToSell().entrySet()) {
                double price = PriceManager.getAdminShopSellPrice(entry.getKey(), entry.getValue(), msg.adminShopName());
                if (price <= 0) {
                    return;
                }
                String stage = PriceManager.getAdminShopSellStage(entry.getKey(), msg.adminShopName());

                if (stage != null && !GameStagesManager.hasGameStage(sender, stage)) {
                    // Player doesn't have required game stage to sell item
                    // The client shouldn't allow for that
                    return;
                }

                purchases.addPurchase(entry.getKey(), entry.getValue() * -1);

                if (Config.oneItemCurrencyMode.get()) {
                    value = value.subtract(BigDecimal.valueOf(Math.round(price)));
                } else {
                    value = value.subtract(BigDecimal.valueOf(price));
                }
            }
        }

        if (Config.oneItemCurrencyMode.get()) {
            long valueLong = value.longValue();
            long totalDollars = OIMWalletItem.getTotalDollars(wallet, sender);

            if (totalDollars < valueLong && totalDollars != -1) {
                // u broke
                return;
            }
        } else {
            BigDecimal walletBalance = WalletItem.getBalance(wallet);

            if (walletBalance.compareTo(value) < 0 && !walletBalance.equals(BigDecimal.valueOf(-1))) {
                // Can't afford
                return;
            }

            if (wallet.getItem() instanceof WalletItem walletItem && value.compareTo(BigDecimal.ZERO) <= 0) {
                if (BigDecimal.valueOf(walletItem.getCapacity()).compareTo(value) < 0) {
                    return;
                }
            }
        }

        if (!Config.disableAdminShopSelling.get()) {
            Set<ItemStack> consideredItemStacks = new HashSet<>();

            for (Map.Entry<ItemDescription, Integer> entry : msg.itemsToSell().entrySet()) {
                int countLeft = entry.getValue();
                ItemDescription itemDescription = entry.getKey();

                for (ItemStack itemStack : sender.getInventory().items) {
                    if (consideredItemStacks.contains(itemStack)) {
                        continue;
                    }

                    ItemDescription itemStackDescription = ItemDescription.ofItem(itemStack);

                    if (itemDescription.equals(itemStackDescription)) {
                        countLeft -= Math.min(countLeft, itemStack.getCount());
                        consideredItemStacks.add(itemStack);

                        if (countLeft == 0) {
                            break;
                        }
                    }
                }

                if (countLeft > 0) {
                    // The client LIED - the player doesn't actually have the items they're trying to sell
                    return;
                }
            }
        }

        if (!(wallet.getItem() instanceof GoldenWalletItem)) {
            if (Config.oneItemCurrencyMode.get()) {
                long valueLong = value.longValue();
                //long totalDollars = OIMWalletItem.getTotalDollars(wallet, sender);

                if (valueLong < 0) {
                    // Should only be $1 bills
                    List<ItemStack> items = CurrencyHelper.getCurrencyItems(BigDecimal.valueOf(-valueLong));
                    Optional<IItemHandler> handlerOptional = wallet.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();

                    items.forEach(itemStack -> {
                        ItemStack left = itemStack;
                        if (handlerOptional.isPresent()) {
                            IItemHandler itemHandler = handlerOptional.get();
                            for (int i = 0; i < itemHandler.getSlots(); i++) {
                                left = itemHandler.insertItem(i, left, false);

                                if (left.isEmpty()) {
                                    return;
                                }
                            }
                        }

                        if (!sender.getInventory().add(left)) {
                            ItemHelper.dropItem(left, sender.level(), sender.blockPosition());
                        }
                    });
                } else {
                    long left = valueLong;

                    for (ItemStack itemStack: sender.getInventory().items) {

                        if (itemStack.getItem() instanceof CurrencyItem currencyItem && !currencyItem.isDisabled()) {
                            int toTake = Math.min(itemStack.getCount(), (int) Math.ceil(left / currencyItem.value.doubleValue()));
                            left -= toTake * currencyItem.value.doubleValue();
                            itemStack.shrink(toTake);

                            if (left <= 0) {
                                break;
                            }
                        }
                    }
                    // money not in the inventory, take from wallet
                    if (left > 0) {
                        Optional<IItemHandler> handlerOptional = wallet.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
                        if (handlerOptional.isPresent()) {
                            IItemHandler handler = handlerOptional.get();

                            for (int i = 0; i < handler.getSlots(); i++) {
                                ItemStack itemStack = handler.getStackInSlot(i);

                                if (itemStack.getItem() instanceof CurrencyItem currencyItem && !currencyItem.isDisabled()) {
                                    int toTake = Math.min(itemStack.getCount(), (int) Math.ceil(left / currencyItem.value.doubleValue()));
                                    left -= toTake * currencyItem.value.doubleValue();
                                    handler.extractItem(i, toTake, false);

                                    if (left <= 0) {
                                        break;
                                    }

                                }
                            }
                        }
                    }
                }
            } else {
                BigDecimal walletBalance = WalletItem.getBalance(wallet);

                if (value.compareTo(BigDecimal.ZERO) < 0 && wallet.getItem() instanceof WalletItem walletItem) {
                    BigDecimal toGive = value.negate();
                    BigDecimal capacity = BigDecimal.valueOf(walletItem.getCapacity());

                    if (walletBalance.compareTo(capacity) < 0) { // Check if the wallet is not full
                        BigDecimal spaceInWallet = capacity.subtract(walletBalance);
                        if (toGive.compareTo(spaceInWallet) <= 0) {
                            // The wallet can hold the entire amount
                            WalletItem.setBalance(wallet, walletBalance.add(toGive));
                        } else {
                            // The wallet is not enough to hold the entire amount
                            WalletItem.setBalance(wallet, capacity); // Fill the wallet to its capacity
                            BigDecimal remainingAmount = toGive.subtract(spaceInWallet);
                            CurrencyHelper.getCurrencyItems(remainingAmount).forEach(itemStack -> {
                                if (!sender.addItem(itemStack)) {
                                    ItemHelper.dropItem(itemStack, sender.level(), sender.blockPosition());
                                }
                            });
                        }
                    } else {
                        // Wallet is already full, give the remaining amount to the player as items
                        CurrencyHelper.getCurrencyItems(toGive).forEach(itemStack -> {
                            if (!sender.addItem(itemStack)) {
                                ItemHelper.dropItem(itemStack, sender.level(), sender.blockPosition());
                            }
                        });
                    }
                } else {
                    WalletItem.setBalance(wallet, walletBalance.subtract(value));
                }
            }
        }

        for (Map.Entry<AdminShopPurchasePacket.ShopItemDescription, Integer> entry : msg.shoppingCart().entrySet()) {
            int countLeft = entry.getValue();
            int stackCount = entry.getKey().itemDescription().item().getMaxStackSize();

            while (countLeft > 0) {
                int thisStackCount = Math.min(countLeft, stackCount);
                double price = PriceManager.getAdminShopBuyPrice(entry.getKey().itemDescription(), thisStackCount, entry.getKey().slot(), entry.getKey().category(), msg.adminShopName());
                if (price <= 0) {
                    continue;
                }

                ItemStack itemStack = entry.getKey().itemDescription().createItemStack();
                itemStack.setCount(thisStackCount);

                if (!sender.addItem(itemStack)) {
                    ItemHelper.dropItem(itemStack, sender.level(), sender.blockPosition());
                }
                countLeft -= thisStackCount;
            }
        }

        if (!Config.disableAdminShopSelling.get()) {
            for (Map.Entry<ItemDescription, Integer> entry : msg.itemsToSell().entrySet()) {
                int countLeft = entry.getValue();
                ItemDescription itemDescription = entry.getKey();

                for (ItemStack itemStack : sender.getInventory().items) {
                    ItemDescription itemStackDescription = ItemDescription.ofItem(itemStack);

                    if (itemDescription.equals(itemStackDescription)) {
                        int taken = Math.min(countLeft, itemStack.getCount());
                        itemStack.shrink(taken);
                        countLeft -= taken;

                        if (countLeft == 0) {
                            break;
                        }
                    }
                }
            }
        }

        purchases.processPurchases();
    }
}
