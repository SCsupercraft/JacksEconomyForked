package me.khajiitos.jackseconomy.init;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.block.*;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.create.CreateCheck;
import me.khajiitos.jackseconomy.create.CreateItemBlockReg;
import me.khajiitos.jackseconomy.item.*;
import me.khajiitos.jackseconomy.util.CurrencyType;
import me.khajiitos.jackseconomy.util.IDisablable;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.math.BigDecimal;
import java.util.List;

public class ItemBlockReg {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(JacksEconomy.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JacksEconomy.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JacksEconomy.MOD_ID);

    public static final DeferredBlock<ExporterBlock> EXPORTER = BLOCKS.register("exporter", ExporterBlock::new);
    public static final DeferredBlock<ImporterBlock> IMPORTER = BLOCKS.register("importer", ImporterBlock::new);
    public static final DeferredBlock<FluidExporterBlock> FLUID_EXPORTER = BLOCKS.register("fluid_exporter", FluidExporterBlock::new);
    public static final DeferredBlock<FluidImporterBlock> FLUID_IMPORTER = BLOCKS.register("fluid_importer", FluidImporterBlock::new);
    public static final DeferredBlock<MechanicalExporterBlock> MECHANICAL_EXPORTER = CreateCheck.isInstalled() ? CreateItemBlockReg.MECHANICAL_EXPORTER : null;
    public static final DeferredBlock<MechanicalImporterBlock> MECHANICAL_IMPORTER = CreateCheck.isInstalled() ? CreateItemBlockReg.MECHANICAL_IMPORTER : null;
    public static final DeferredBlock<MechanicalFluidExporterBlock> MECHANICAL_FLUID_EXPORTER = CreateCheck.isInstalled() ? CreateItemBlockReg.MECHANICAL_FLUID_EXPORTER : null;
    public static final DeferredBlock<MechanicalFluidImporterBlock> MECHANICAL_FLUID_IMPORTER = CreateCheck.isInstalled() ? CreateItemBlockReg.MECHANICAL_FLUID_IMPORTER : null;
    public static final DeferredBlock<CurrencyConverterBlock> CURRENCY_CONVERTER = BLOCKS.register("currency_converter", CurrencyConverterBlock::new);
    public static final DeferredBlock<AdminShopBlock> ADMIN_SHOP = BLOCKS.register("admin_shop", AdminShopBlock::new);

    public static final DeferredItem<BlockItem> EXPORTER_ITEM = ITEMS.register("exporter", () -> new BlockItem(EXPORTER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> IMPORTER_ITEM = ITEMS.register("importer", () -> new BlockItem(IMPORTER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> FLUID_EXPORTER_ITEM = ITEMS.register("fluid_exporter", () -> new BlockItem(FLUID_EXPORTER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> FLUID_IMPORTER_ITEM = ITEMS.register("fluid_importer", () -> new BlockItem(FLUID_IMPORTER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> MECHANICAL_EXPORTER_ITEM = CreateCheck.isInstalled() ? CreateItemBlockReg.MECHANICAL_EXPORTER_ITEM : null;
    public static final DeferredItem<BlockItem> MECHANICAL_IMPORTER_ITEM = CreateCheck.isInstalled() ? CreateItemBlockReg.MECHANICAL_IMPORTER_ITEM : null;
    public static final DeferredItem<BlockItem> MECHANICAL_FLUID_EXPORTER_ITEM = CreateCheck.isInstalled() ? CreateItemBlockReg.MECHANICAL_FLUID_EXPORTER_ITEM : null;
    public static final DeferredItem<BlockItem> MECHANICAL_FLUID_IMPORTER_ITEM = CreateCheck.isInstalled() ? CreateItemBlockReg.MECHANICAL_FLUID_IMPORTER_ITEM : null;
    public static final DeferredItem<BlockItem> CURRENCY_CONVERTER_ITEM = ITEMS.register("currency_converter", () -> new BlockItem(CURRENCY_CONVERTER.get(), new Item.Properties()) {
        @Override
        public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag flag) {
            if (this.getBlock() instanceof IDisablable disablable && disablable.isDisabled()) {
                tooltip.addAll(disablable.getDisabledTooltip());
            }
        }
    });
    public static final DeferredItem<BlockItem> ADMIN_SHOP_ITEM = ITEMS.register("admin_shop", () -> new NameableBlockItem(ADMIN_SHOP.get(), new Item.Properties()));

    public static final DeferredItem<CurrencyItem> PENNY_ITEM = ITEMS.register("penny", () -> new CurrencyItem(new BigDecimal("0.01"), false));
    public static final DeferredItem<CurrencyItem> NICKEL_ITEM = ITEMS.register("nickel", () -> new CurrencyItem(new BigDecimal("0.05"), false));
    public static final DeferredItem<CurrencyItem> DIME_ITEM = ITEMS.register("dime", () -> new CurrencyItem(new BigDecimal("0.10"), false));
    public static final DeferredItem<CurrencyItem> QUARTER_ITEM = ITEMS.register("quarter", () -> new CurrencyItem(new BigDecimal("0.25"), false));
    public static final DeferredItem<CurrencyItem> DOLLAR_BILL_ITEM = ITEMS.register("dollar_bill", () -> new CurrencyItem(new BigDecimal("1.00"), true));
    public static final DeferredItem<CurrencyItem> FIVE_DOLLAR_BILL_ITEM = ITEMS.register("five_dollar_bill", () -> new CurrencyItem(new BigDecimal("5.00"), true));
    public static final DeferredItem<CurrencyItem> TEN_DOLLAR_BILL_ITEM = ITEMS.register("ten_dollar_bill", () -> new CurrencyItem(new BigDecimal("10.00"), true));
    public static final DeferredItem<CurrencyItem> TWENTY_DOLLAR_BILL_ITEM = ITEMS.register("twenty_dollar_bill", () -> new CurrencyItem(new BigDecimal("20.00"), true));
    public static final DeferredItem<CurrencyItem> FIFTY_DOLLAR_BILL_ITEM = ITEMS.register("fifty_dollar_bill", () -> new CurrencyItem(new BigDecimal("50.00"), true));
    public static final DeferredItem<CurrencyItem> HUNDRED_DOLLAR_BILL_ITEM = ITEMS.register("hundred_dollar_bill", () -> new CurrencyItem(new BigDecimal("100.00"), true));
    public static final DeferredItem<CurrencyItem> THOUSAND_DOLLAR_BILL_ITEM = ITEMS.register("thousand_dollar_bill", () -> new CurrencyItem(new BigDecimal("1000.00"), true));

    public static final DeferredItem<CurrencyStackItem> PENNY_STACK_ITEM = ITEMS.register("penny_stack", () -> new CurrencyStackItem(CurrencyType.PENNY));
    public static final DeferredItem<CurrencyStackItem> NICKEL_STACK_ITEM = ITEMS.register("nickel_stack", () -> new CurrencyStackItem(CurrencyType.NICKEL));
    public static final DeferredItem<CurrencyStackItem> DIME_STACK_ITEM = ITEMS.register("dime_stack", () -> new CurrencyStackItem(CurrencyType.DIME));
    public static final DeferredItem<CurrencyStackItem> QUARTER_STACK_ITEM = ITEMS.register("quarter_stack", () -> new CurrencyStackItem(CurrencyType.QUARTER));
    public static final DeferredItem<CurrencyStackItem> DOLLAR_BILL_STACK_ITEM = ITEMS.register("dollar_bill_stack", () -> new CurrencyStackItem(CurrencyType.DOLLAR_BILL));
    public static final DeferredItem<CurrencyStackItem> FIVE_DOLLAR_BILL_STACK_ITEM = ITEMS.register("five_dollar_bill_stack", () -> new CurrencyStackItem(CurrencyType.FIVE_DOLLAR_BILL));
    public static final DeferredItem<CurrencyStackItem> TEN_DOLLAR_BILL_STACK_ITEM = ITEMS.register("ten_dollar_bill_stack", () -> new CurrencyStackItem(CurrencyType.TEN_DOLLAR_BILL));
    public static final DeferredItem<CurrencyStackItem> TWENTY_DOLLAR_BILL_STACK_ITEM = ITEMS.register("twenty_dollar_bill_stack", () -> new CurrencyStackItem(CurrencyType.TWENTY_DOLLAR_BILL));
    public static final DeferredItem<CurrencyStackItem> FIFTY_DOLLAR_BILL_STACK_ITEM = ITEMS.register("fifty_dollar_bill_stack", () -> new CurrencyStackItem(CurrencyType.FIFTY_DOLLAR_BILL));
    public static final DeferredItem<CurrencyStackItem> HUNDRED_DOLLAR_BILL_STACK_ITEM = ITEMS.register("hundred_dollar_bill_stack", () -> new CurrencyStackItem(CurrencyType.HUNDRED_DOLLAR_BILL));
    public static final DeferredItem<CurrencyStackItem> THOUSAND_DOLLAR_BILL_STACK_ITEM = ITEMS.register("thousand_dollar_bill_stack", () -> new CurrencyStackItem(CurrencyType.THOUSAND_DOLLAR_BILL));

    public static final DeferredBlock<CurrencyStackBlock> PENNY_STACK_BLOCK = BLOCKS.register("penny_stack", () -> new CurrencyStackBlock(false));
    public static final DeferredBlock<CurrencyStackBlock> NICKEL_STACK_BLOCK = BLOCKS.register("nickel_stack", () -> new CurrencyStackBlock(false));
    public static final DeferredBlock<CurrencyStackBlock> DIME_STACK_BLOCK = BLOCKS.register("dime_stack", () -> new CurrencyStackBlock(false));
    public static final DeferredBlock<CurrencyStackBlock> QUARTER_STACK_BLOCK = BLOCKS.register("quarter_stack", () -> new CurrencyStackBlock(false));
    public static final DeferredBlock<CurrencyStackBlock> DOLLAR_BILL_STACK_BLOCK = BLOCKS.register("dollar_bill_stack", () -> new CurrencyStackBlock(true));
    public static final DeferredBlock<CurrencyStackBlock> FIVE_DOLLAR_BILL_STACK_BLOCK = BLOCKS.register("five_dollar_bill_stack", () -> new CurrencyStackBlock(true));
    public static final DeferredBlock<CurrencyStackBlock> TEN_DOLLAR_BILL_STACK_BLOCK = BLOCKS.register("ten_dollar_bill_stack", () -> new CurrencyStackBlock(true));
    public static final DeferredBlock<CurrencyStackBlock> TWENTY_DOLLAR_BILL_STACK_BLOCK = BLOCKS.register("twenty_dollar_bill_stack", () -> new CurrencyStackBlock(true));
    public static final DeferredBlock<CurrencyStackBlock> FIFTY_DOLLAR_BILL_STACK_BLOCK = BLOCKS.register("fifty_dollar_bill_stack", () -> new CurrencyStackBlock(true));
    public static final DeferredBlock<CurrencyStackBlock> HUNDRED_DOLLAR_BILL_STACK_BLOCK = BLOCKS.register("hundred_dollar_bill_stack", () -> new CurrencyStackBlock(true));
    public static final DeferredBlock<CurrencyStackBlock> THOUSAND_DOLLAR_BILL_STACK_BLOCK = BLOCKS.register("thousand_dollar_bill_stack", () -> new CurrencyStackBlock(true));

    public static final DeferredItem<WalletItem> BASIC_WALLET_ITEM = ITEMS.register("basic_wallet", () -> new WalletItem(() -> Config.basicWalletCapacity));
    public static final DeferredItem<WalletItem> INTERMEDIATE_WALLET_ITEM = ITEMS.register("intermediate_wallet", () -> new WalletItem(() -> Config.intermediateWalletCapacity));
    public static final DeferredItem<WalletItem> ADVANCED_WALLET_ITEM = ITEMS.register("advanced_wallet", () -> new WalletItem(() -> Config.advancedWalletCapacity));
    public static final DeferredItem<WalletItem> THE_PHAT_WALLET_ITEM = ITEMS.register("the_phat_wallet", () -> new WalletItem(() -> Config.thePhatWalletCapacity));
    public static final DeferredItem<InfiniteWalletItem> INFINITE_WALLET_ITEM = ITEMS.register("infinite_wallet", InfiniteWalletItem::new);
    public static final DeferredItem<OIMWalletItem> WALLET_ITEM = ITEMS.register("wallet", OIMWalletItem::new);

    public static final DeferredItem<CheckItem> CHECK_ITEM = ITEMS.register("check", CheckItem::new);
    public static final DeferredItem<ImporterTicketItem> IMPORTER_TICKET_ITEM = ITEMS.register("importer_manifest", ImporterTicketItem::new);
    public static final DeferredItem<ExporterTicketItem> EXPORTER_TICKET_ITEM = ITEMS.register("exporter_manifest", () -> new ExporterTicketItem());
    public static final DeferredItem<FluidImporterTicketItem> FLUID_IMPORTER_TICKET_ITEM = ITEMS.register("fluid_importer_manifest", FluidImporterTicketItem::new);
    public static final DeferredItem<FluidExporterTicketItem> FLUID_EXPORTER_TICKET_ITEM = ITEMS.register("fluid_exporter_manifest", () -> new FluidExporterTicketItem());
    public static final DeferredItem<GoldenExporterTicketItem> GOLDEN_EXPORTER_TICKET_ITEM = ITEMS.register("golden_exporter_manifest", GoldenExporterTicketItem::new);
    public static final DeferredItem<GoldenFluidExporterTicketItem> GOLDEN_FLUID_EXPORTER_TICKET_ITEM = ITEMS.register("golden_fluid_exporter_manifest", GoldenFluidExporterTicketItem::new);
    public static final DeferredItem<EmptyTicketItem> EMPTY_IMPORTER_TICKET_ITEM = ITEMS.register("empty_importer_manifest", () -> new EmptyTicketItem(EmptyTicketItem.Type.IMPORTER));
    public static final DeferredItem<EmptyTicketItem> EMPTY_EXPORTER_TICKET_ITEM = ITEMS.register("empty_exporter_manifest", () -> new EmptyTicketItem(EmptyTicketItem.Type.EXPORTER));
    public static final DeferredItem<EmptyTicketItem> EMPTY_FLUID_IMPORTER_TICKET_ITEM = ITEMS.register("empty_fluid_importer_manifest", () -> new EmptyTicketItem(EmptyTicketItem.Type.FLUID_IMPORTER));
    public static final DeferredItem<EmptyTicketItem> EMPTY_FLUID_EXPORTER_TICKET_ITEM = ITEMS.register("empty_fluid_exporter_manifest", () -> new EmptyTicketItem(EmptyTicketItem.Type.FLUID_EXPORTER));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> tab = CREATIVE_MODE_TABS.register("jackseconomy", () -> CreativeModeTab.builder().icon(() -> new ItemStack(ItemBlockReg.EXPORTER_ITEM.get())).title(Component.translatable("itemGroup.jackseconomy")).displayItems((params, output) -> {
        output.accept(ItemBlockReg.EXPORTER_ITEM.get());
        output.accept(ItemBlockReg.IMPORTER_ITEM.get());
        output.accept(ItemBlockReg.FLUID_EXPORTER_ITEM.get());
        output.accept(ItemBlockReg.FLUID_IMPORTER_ITEM.get());
        if (CreateCheck.isInstalled()) {
            output.accept(ItemBlockReg.MECHANICAL_EXPORTER_ITEM.get());
            output.accept(ItemBlockReg.MECHANICAL_IMPORTER_ITEM.get());
            output.accept(ItemBlockReg.MECHANICAL_FLUID_EXPORTER_ITEM.get());
            output.accept(ItemBlockReg.MECHANICAL_FLUID_IMPORTER_ITEM.get());
        }
        output.accept(ItemBlockReg.CURRENCY_CONVERTER_ITEM.get());
        output.accept(ItemBlockReg.ADMIN_SHOP_ITEM.get());
        output.accept(ItemBlockReg.PENNY_ITEM.get());
        output.accept(ItemBlockReg.NICKEL_ITEM.get());
        output.accept(ItemBlockReg.DIME_ITEM.get());
        output.accept(ItemBlockReg.QUARTER_ITEM.get());
        output.accept(ItemBlockReg.DOLLAR_BILL_ITEM.get());
        output.accept(ItemBlockReg.FIVE_DOLLAR_BILL_ITEM.get());
        output.accept(ItemBlockReg.TEN_DOLLAR_BILL_ITEM.get());
        output.accept(ItemBlockReg.TWENTY_DOLLAR_BILL_ITEM.get());
        output.accept(ItemBlockReg.FIFTY_DOLLAR_BILL_ITEM.get());
        output.accept(ItemBlockReg.HUNDRED_DOLLAR_BILL_ITEM.get());
        output.accept(ItemBlockReg.THOUSAND_DOLLAR_BILL_ITEM.get());
        output.accept(ItemBlockReg.PENNY_STACK_ITEM.get());
        output.accept(ItemBlockReg.NICKEL_STACK_ITEM.get());
        output.accept(ItemBlockReg.DIME_STACK_ITEM.get());
        output.accept(ItemBlockReg.QUARTER_STACK_ITEM.get());
        output.accept(ItemBlockReg.DOLLAR_BILL_STACK_ITEM.get());
        output.accept(ItemBlockReg.FIVE_DOLLAR_BILL_STACK_ITEM.get());
        output.accept(ItemBlockReg.TEN_DOLLAR_BILL_STACK_ITEM.get());
        output.accept(ItemBlockReg.TWENTY_DOLLAR_BILL_STACK_ITEM.get());
        output.accept(ItemBlockReg.FIFTY_DOLLAR_BILL_STACK_ITEM.get());
        output.accept(ItemBlockReg.HUNDRED_DOLLAR_BILL_STACK_ITEM.get());
        output.accept(ItemBlockReg.THOUSAND_DOLLAR_BILL_STACK_ITEM.get());
        output.accept(ItemBlockReg.BASIC_WALLET_ITEM.get());
        output.accept(ItemBlockReg.INTERMEDIATE_WALLET_ITEM.get());
        output.accept(ItemBlockReg.ADVANCED_WALLET_ITEM.get());
        output.accept(ItemBlockReg.THE_PHAT_WALLET_ITEM.get());
        output.accept(ItemBlockReg.INFINITE_WALLET_ITEM.get());
        output.accept(ItemBlockReg.WALLET_ITEM.get());
        output.accept(ItemBlockReg.GOLDEN_EXPORTER_TICKET_ITEM.get());
        output.accept(ItemBlockReg.GOLDEN_FLUID_EXPORTER_TICKET_ITEM.get());
        output.accept(ItemBlockReg.EMPTY_EXPORTER_TICKET_ITEM.get());
        output.accept(ItemBlockReg.EMPTY_IMPORTER_TICKET_ITEM.get());
        output.accept(ItemBlockReg.EMPTY_FLUID_EXPORTER_TICKET_ITEM.get());
        output.accept(ItemBlockReg.EMPTY_FLUID_IMPORTER_TICKET_ITEM.get());
    }).build());

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }
}