package me.khajiitos.jackseconomy.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<Boolean> hidePriceTooltips;
    public static final ModConfigSpec.ConfigValue<Boolean> alternativeTooltipFormat;
    public static final ModConfigSpec.ConfigValue<Double> balanceChangePopupTime;
    public static final ModConfigSpec.ConfigValue<Boolean> walletHudPositionRight;
    public static final ModConfigSpec.ConfigValue<Integer> walletHudPositionYOffset;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        hidePriceTooltips = builder.define("hidePriceTooltips", false);
        alternativeTooltipFormat = builder.define("alternativeTooltipFormat", true);
        balanceChangePopupTime = builder.defineInRange("balanceChangePopupTime", 6.0, 1.0, 60.0);
        walletHudPositionRight = builder.define("walletHudPositionRight", false);
        walletHudPositionYOffset = builder.define("walletHudPositionYOffset", 0);

        SPEC = builder.build();
    }
}
