package me.khajiitos.jackseconomy.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<Integer> baseImporterEnergyUsage;
    public static final ModConfigSpec.ConfigValue<Integer> baseExporterEnergyUsage;
    public static final ModConfigSpec.ConfigValue<Integer> maxImporterEnergy;
    public static final ModConfigSpec.ConfigValue<Integer> maxExporterEnergy;
    public static final ModConfigSpec.ConfigValue<Integer> maxImporterEnergyReceive;
    public static final ModConfigSpec.ConfigValue<Integer> maxExporterEnergyReceive;
    public static final ModConfigSpec.ConfigValue<Double> baseExporterProgressPerTick;
    public static final ModConfigSpec.ConfigValue<Double> baseImporterProgressPerTick;
    public static final ModConfigSpec.ConfigValue<Double> maxExporterBalance;
    public static final ModConfigSpec.ConfigValue<Double> maxImporterBalance;
    public static final ModConfigSpec.ConfigValue<Double> maxCurrencyConverterBalance;
    public static final ModConfigSpec.ConfigValue<Double> basicWalletCapacity;
    public static final ModConfigSpec.ConfigValue<Double> intermediateWalletCapacity;
    public static final ModConfigSpec.ConfigValue<Double> advancedWalletCapacity;
    public static final ModConfigSpec.ConfigValue<Double> thePhatWalletCapacity;
    public static final ModConfigSpec.ConfigValue<Double> mechanicalImporterProgressPerSpeed;
    public static final ModConfigSpec.ConfigValue<Double> mechanicalExporterProgressPerSpeed;
    public static final ModConfigSpec.ConfigValue<Double> mechanicalExporterStressPerRPM;
    public static final ModConfigSpec.ConfigValue<Double> mechanicalImporterStressPerRPM;
    public static final ModConfigSpec.ConfigValue<Boolean> showNamesForLockedAdminShopItems;
    public static final ModConfigSpec.ConfigValue<Boolean> showStageForLockedAdminShopItems;
    public static final ModConfigSpec.ConfigValue<Boolean> showStageForLockedSellItems;
    public static final ModConfigSpec.ConfigValue<Boolean> disableAdminShopSelling;
    public static final ModConfigSpec.ConfigValue<Boolean> oneItemCurrencyMode;
    public static final ModConfigSpec.ConfigValue<Boolean> adminShopCommandForEveryone;
    public static final ModConfigSpec.ConfigValue<String> adminShopCommandShopName;
    public static final ModConfigSpec.ConfigValue<Boolean> returnManifestItems;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        baseImporterEnergyUsage = builder.define("baseImporterEnergyUsage", 8);
        baseExporterEnergyUsage = builder.define("baseExporterEnergyUsage", 6);

        maxImporterEnergy = builder.worldRestart().define("maxImporterEnergy", 50000);
        maxExporterEnergy = builder.worldRestart().define("maxExporterEnergy", 50000);

        maxImporterEnergyReceive = builder.worldRestart().define("maxImporterEnergyReceive", 256);
        maxExporterEnergyReceive = builder.worldRestart().define("maxExporterEnergyReceive", 256);

        baseExporterProgressPerTick = builder.worldRestart().define("baseExporterProgressPerTick", 0.0005);
        baseImporterProgressPerTick = builder.worldRestart().define("baseImporterProgressPerTick", 0.002);

        maxExporterBalance = builder.define("maxExporterBalance", 1000000.0);
        maxImporterBalance = builder.define("maxImporterBalance", 1000000.0);
        maxCurrencyConverterBalance = builder.define("maxCurrencyConverterBalance", 1000000.0);

        basicWalletCapacity = builder.define("basicWalletCapacity", 1000.0);
        intermediateWalletCapacity = builder.define("intermediateWalletCapacity", 10000.00);
        advancedWalletCapacity = builder.define("advancedWalletCapacity", 1000000.00);
        thePhatWalletCapacity = builder.define("thePhatWalletCapacity", 1000000000.00);

        mechanicalImporterProgressPerSpeed = builder.define("mechanicalImporterProgressPerSpeed", 0.00005);
        mechanicalExporterProgressPerSpeed = builder.define("mechanicalExporterProgressPerSpeed", 0.0002);

        mechanicalExporterStressPerRPM = builder.define("mechanicalExporterStressPerRPM", 8.0);
        mechanicalImporterStressPerRPM = builder.define("mechanicalImporterStressPerRPM", 8.0);

        showNamesForLockedAdminShopItems = builder.comment("If an item in the Admin Shop is locked behind a game stage, it will (or won't) show what item that is")
                .define("showNamesForLockedAdminShopItems", false);

        showStageForLockedAdminShopItems = builder.comment("If an item in the Admin Shop is locked behind a game stage, it will (or won't) show what stage that item is locked behind")
                .define("showStageForLockedAdminShopItems", false);

        showStageForLockedSellItems = builder.comment("If an item to sell in the Admin Shop is locked behind a game stage, it will (or won't) show what stage that item is locked behind")
                .define("showStageForLockedSellItems", false);

        disableAdminShopSelling = builder.define("disableAdminShopSelling", false);

        oneItemCurrencyMode = builder.define("oneItemCurrencyMode", false);

        adminShopCommandForEveryone = builder.define("adminShopCommandForEveryone", false);
        adminShopCommandShopName = builder.define("adminShopCommandShopName", "");

        returnManifestItems = builder.define("returnManifestItems", true);

        SPEC = builder.build();
    }
}
