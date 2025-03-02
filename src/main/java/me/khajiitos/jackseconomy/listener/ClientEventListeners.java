package me.khajiitos.jackseconomy.listener;

import com.mojang.blaze3d.platform.InputConstants;
import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.config.ClientConfig;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.data.price.FluidDescription;
import me.khajiitos.jackseconomy.data.price.PricesFluidPriceInfo;
import me.khajiitos.jackseconomy.init.Packets;
import me.khajiitos.jackseconomy.packet.OpenCuriosWalletPacket;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.data.price.PricesItemPriceInfo;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class ClientEventListeners {

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }

        while (JacksEconomyClient.OPEN_WALLET.consumeClick()) {
            ItemStack curiosWallet = CuriosWallet.get(Minecraft.getInstance().player);

            if (!curiosWallet.isEmpty()) {
                Packets.sendToServer(new OpenCuriosWalletPacket());
            }
        }
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent e) {

        if (ClientConfig.hidePriceTooltips.get()) {
            return;
        }

        PricesItemPriceInfo priceInfo = JacksEconomyClient.priceInfos.get(ItemDescription.ofItem(e.getItemStack()));

        LazyOptional<IFluidHandlerItem> capability = e.getItemStack().getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        IFluidHandlerItem fluidHandlerItem = null;
        PricesFluidPriceInfo fluidPriceInfo = null;
        if (capability.isPresent()) {
            fluidHandlerItem = capability.orElseThrow(RuntimeException::new);
            fluidPriceInfo = JacksEconomyClient.fluidPriceInfos.get(FluidDescription.ofFluid(fluidHandlerItem.getFluidInTank(0)));
        }

        if (priceInfo == null && fluidPriceInfo == null) {
            return;
        }
        if (!(
                (priceInfo != null && (priceInfo.sellPrice != -1 || priceInfo.importerBuyPrice != -1))
                || (fluidPriceInfo != null && (fluidPriceInfo.sellPrice != -1 || fluidPriceInfo.importerBuyPrice != -1))
        )) {
            return;
        }

        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_ALT)) {
            if (priceInfo != null) {
                if (priceInfo.sellPrice > 0) {
                    MutableComponent sellPrice = Component.literal(Config.oneItemCurrencyMode.get() ? "$" + (long) priceInfo.sellPrice : CurrencyHelper.format(priceInfo.sellPrice));
                    if (ClientConfig.alternativeTooltipFormat.get()) {
                        e.getToolTip().add(Component.translatable("jackseconomy.exporter_price", sellPrice.withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                    } else {
                        e.getToolTip().add(Component.translatable("jackseconomy.sell_price", Component.literal("1").withStyle(ChatFormatting.DARK_AQUA), sellPrice.withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                        if (e.getItemStack().getCount() > 1) {
                            e.getToolTip().add(Component.translatable("jackseconomy.sell_price", Component.literal(String.valueOf(e.getItemStack().getCount())).withStyle(ChatFormatting.DARK_AQUA), Component.literal(Config.oneItemCurrencyMode.get() ? "$" + ((long) priceInfo.sellPrice * e.getItemStack().getCount()) : CurrencyHelper.format(priceInfo.sellPrice * e.getItemStack().getCount())).withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                        }
                    }
                }

                if (priceInfo.importerBuyPrice > 0) {
                    MutableComponent buyPrice = Component.literal(Config.oneItemCurrencyMode.get() ? "$" + (long) priceInfo.importerBuyPrice : CurrencyHelper.format(priceInfo.importerBuyPrice));

                    if (ClientConfig.alternativeTooltipFormat.get()) {
                        e.getToolTip().add(Component.translatable("jackseconomy.importer_price", buyPrice.withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                    } else {
                        e.getToolTip().add(Component.translatable("jackseconomy.buy_price", Component.literal("1").withStyle(ChatFormatting.DARK_AQUA), buyPrice.withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                        if (e.getItemStack().getCount() > 1) {
                            e.getToolTip().add(Component.translatable("jackseconomy.buy_price", Component.literal(String.valueOf(e.getItemStack().getCount())).withStyle(ChatFormatting.DARK_AQUA), Component.literal(Config.oneItemCurrencyMode.get() ? "$" + ((long) priceInfo.sellPrice * e.getItemStack().getCount()) : CurrencyHelper.format(priceInfo.importerBuyPrice * e.getItemStack().getCount())).withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                        }
                    }
                }
            }

            if (fluidPriceInfo != null) {
                int amount = fluidHandlerItem.getFluidInTank(0).getAmount();

                if (fluidPriceInfo.sellPrice > 0) {
                    MutableComponent sellPrice = Component.literal(Config.oneItemCurrencyMode.get() ? "$" + (long) fluidPriceInfo.sellPrice : CurrencyHelper.format(fluidPriceInfo.sellPrice));
                    if (ClientConfig.alternativeTooltipFormat.get()) {
                        e.getToolTip().add(Component.translatable("jackseconomy.fluid_exporter_price", sellPrice.withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                    } else {
                        e.getToolTip().add(Component.translatable("jackseconomy.fluid_sell_price", Component.literal("1").withStyle(ChatFormatting.DARK_AQUA), sellPrice.withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                        if (e.getItemStack().getCount() > 1) {
                            e.getToolTip().add(Component.translatable("jackseconomy.fluid_sell_price", Component.literal(String.valueOf(amount)).withStyle(ChatFormatting.DARK_AQUA), Component.literal(Config.oneItemCurrencyMode.get() ? "$" + ((long) fluidPriceInfo.sellPrice * amount) : CurrencyHelper.format(fluidPriceInfo.sellPrice * amount)).withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                        }
                    }
                }

                if (fluidPriceInfo.importerBuyPrice > 0) {
                    MutableComponent buyPrice = Component.literal(Config.oneItemCurrencyMode.get() ? "$" + (long) fluidPriceInfo.importerBuyPrice : CurrencyHelper.format(fluidPriceInfo.importerBuyPrice));

                    if (ClientConfig.alternativeTooltipFormat.get()) {
                        e.getToolTip().add(Component.translatable("jackseconomy.fluid_importer_price", buyPrice.withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                    } else {
                        e.getToolTip().add(Component.translatable("jackseconomy.fluid_buy_price", Component.literal("1").withStyle(ChatFormatting.DARK_AQUA), buyPrice.withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                        if (e.getItemStack().getCount() > 1) {
                            e.getToolTip().add(Component.translatable("jackseconomy.fluid_buy_price", Component.literal(String.valueOf(amount)).withStyle(ChatFormatting.DARK_AQUA), Component.literal(Config.oneItemCurrencyMode.get() ? "$" + ((long) fluidPriceInfo.sellPrice * amount) : CurrencyHelper.format(fluidPriceInfo.importerBuyPrice * amount)).withStyle(ChatFormatting.DARK_AQUA)).withStyle(ChatFormatting.GRAY));
                        }
                    }
                }
            }
        } else {
            e.getToolTip().add(Component.translatable("jackseconomy.view_prices", Component.translatable("jackseconomy.lalt").withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        }

    }

    @SubscribeEvent
    public void onLoggedOut(ClientPlayerNetworkEvent.LoggingOut e) {
        JacksEconomyClient.priceInfos.clear();
        JacksEconomyClient.fluidPriceInfos.clear();
    }
}
