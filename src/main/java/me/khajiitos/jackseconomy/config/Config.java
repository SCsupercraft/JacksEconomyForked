package me.khajiitos.jackseconomy.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Integer> baseImporterEnergyUsage;
    public static final ForgeConfigSpec.ConfigValue<Integer> baseExporterEnergyUsage;
    public static final ForgeConfigSpec.ConfigValue<Integer> maxImporterEnergy;
    public static final ForgeConfigSpec.ConfigValue<Integer> maxExporterEnergy;
    public static final ForgeConfigSpec.ConfigValue<Integer> maxImporterEnergyReceive;
    public static final ForgeConfigSpec.ConfigValue<Integer> maxExporterEnergyReceive;
    public static final ForgeConfigSpec.ConfigValue<Double> baseExporterProgressPerTick;
    public static final ForgeConfigSpec.ConfigValue<Double> baseImporterProgressPerTick;
    public static final ForgeConfigSpec.ConfigValue<Double> maxExporterBalance;
    public static final ForgeConfigSpec.ConfigValue<Double> maxImporterBalance;
    public static final ForgeConfigSpec.ConfigValue<Double> maxCurrencyConverterBalance;
    public static final ForgeConfigSpec.ConfigValue<Double> basicWalletCapacity;
    public static final ForgeConfigSpec.ConfigValue<Double> intermediateWalletCapacity;
    public static final ForgeConfigSpec.ConfigValue<Double> advancedWalletCapacity;
    public static final ForgeConfigSpec.ConfigValue<Double> thePhatWalletCapacity;
    public static final ForgeConfigSpec.ConfigValue<Double> mechanicalImporterProgressPerSpeed;
    public static final ForgeConfigSpec.ConfigValue<Double> mechanicalExporterProgressPerSpeed;
    public static final ForgeConfigSpec.ConfigValue<Double> mechanicalExporterStressPerRPM;
    public static final ForgeConfigSpec.ConfigValue<Double> mechanicalImporterStressPerRPM;
    public static final ForgeConfigSpec.ConfigValue<Boolean> showNamesForLockedAdminShopItems;
    public static final ForgeConfigSpec.ConfigValue<Boolean> showStageForLockedAdminShopItems;
    public static final ForgeConfigSpec.ConfigValue<Boolean> showStageForLockedSellItems;
    public static final ForgeConfigSpec.ConfigValue<Boolean> disableAdminShopSelling;
    public static final ForgeConfigSpec.ConfigValue<Boolean> oneItemCurrencyMode;
    public static final ForgeConfigSpec.ConfigValue<Boolean> adminShopCommandForEveryone;
    public static final ForgeConfigSpec.ConfigValue<String> adminShopCommandShopName;
    public static final ForgeConfigSpec.ConfigValue<Boolean> returnManifestItems;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

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
