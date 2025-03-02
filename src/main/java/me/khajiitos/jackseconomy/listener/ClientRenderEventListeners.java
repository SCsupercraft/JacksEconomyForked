package me.khajiitos.jackseconomy.listener;

import com.mojang.blaze3d.systems.RenderSystem;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.config.ClientConfig;
import me.khajiitos.jackseconomy.curios.CuriosWallet;
import me.khajiitos.jackseconomy.item.OIMWalletItem;
import me.khajiitos.jackseconomy.item.WalletItem;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

@OnlyIn(Dist.CLIENT)
public class ClientRenderEventListeners {

    protected static final ResourceLocation BALANCE_PROGRESS = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/balance_progress.png");

    @SubscribeEvent
    public void onDrawHud(RenderGuiEvent.Post e) {
        ItemStack wallet = CuriosWallet.get(Minecraft.getInstance().player);

        if (wallet == null || wallet.isEmpty()) {
            return;
        }

        Component balanceText;

        int yOffset = ClientConfig.walletHudPositionYOffset.get();

        if (wallet.getItem() instanceof WalletItem walletItem) {
            BigDecimal balance = WalletItem.getBalance(wallet);

            balanceText = Component.literal(CurrencyHelper.formatShortened(balance));
            int balanceTextWidth = Minecraft.getInstance().font.width(balanceText);
            int startX = ClientConfig.walletHudPositionRight.get() ? e.getGuiGraphics().guiWidth() - (Math.max(balanceTextWidth - 21, 54)) : 1;
            e.getGuiGraphics().renderItem(wallet, startX, 1 + yOffset);

            BigDecimal capacity = BigDecimal.valueOf(walletItem.getCapacity());
            double progress = balance.divide(capacity, RoundingMode.DOWN).min(BigDecimal.ONE).doubleValue();

            e.getGuiGraphics().drawString(Minecraft.getInstance().font, balanceText, startX + 18, 7 + yOffset, 0xFFFFFFFF);
            RenderSystem.setShaderTexture(0, BALANCE_PROGRESS);
            e.getGuiGraphics().blit(BALANCE_PROGRESS, startX + 2, 19 + yOffset, 0, 0, 0, 51, 5, 256, 256);
            e.getGuiGraphics().blit(BALANCE_PROGRESS, startX + 2, 19 + yOffset, 0, 0, 5, ((int)(51 * progress)), 5, 256, 256);
        } else if (wallet.getItem() instanceof OIMWalletItem) {
            long balance = OIMWalletItem.getDollars(wallet);
            balanceText = Component.literal("$" + balance);
            int balanceTextWidth = Minecraft.getInstance().font.width(balanceText);
            int startX = ClientConfig.walletHudPositionRight.get() ? e.getGuiGraphics().guiWidth() - balanceTextWidth - 21 : 1;

            e.getGuiGraphics().renderItem(wallet, startX, 1 + yOffset);
            e.getGuiGraphics().drawString(Minecraft.getInstance().font, balanceText, startX + 18, 7 + yOffset, 0xFFFFFFFF);
        } else {
            return;
        }

        if (JacksEconomyClient.balanceDifPopup != null) {
            // tbh using millis here might not be the best idea but idc it works
            long timeDelta = System.currentTimeMillis() - JacksEconomyClient.balanceDifPopupStartMillis;
            long maxTimeDelta = (long)(ClientConfig.balanceChangePopupTime.get() * 1000);

            int alpha;

            if (timeDelta > maxTimeDelta) {
                JacksEconomyClient.balanceDifPopup = null;
                JacksEconomyClient.balanceDifPopupStartMillis = -1;
                return;
            }

            if (timeDelta < 500) {
                alpha = (int)(255 * Mth.lerp(timeDelta / 500.0, 0.0, 1.0));
            } else if (timeDelta >= maxTimeDelta - 500) {
                alpha = (int)(255 * Mth.lerp((maxTimeDelta - timeDelta) / 500.0, 0.0, 1.0));
            } else {
                alpha = 255;
            }

            int balanceTextWidth = Minecraft.getInstance().font.width(balanceText);

            String balanceDif = CurrencyHelper.formatShortened(JacksEconomyClient.balanceDifPopup);
            boolean positive = JacksEconomyClient.balanceDifPopup.compareTo(BigDecimal.ZERO) > 0;

            Component balanceDifComponent = Component.literal((positive ? "(+" : "(") + balanceDif + ")").withStyle(positive ? ChatFormatting.GREEN : ChatFormatting.RED);

            int balanceDifWidth = Minecraft.getInstance().font.width(balanceDifComponent);

            int startX = ClientConfig.walletHudPositionRight.get() ? e.getGuiGraphics().guiWidth() - balanceTextWidth - balanceDifWidth - 22 : 24 + balanceTextWidth;
            e.getGuiGraphics().drawString(Minecraft.getInstance().font, balanceDifComponent, startX, 7 + yOffset, 0x00FFFFFF | (alpha << 24));
        }
    }
}
