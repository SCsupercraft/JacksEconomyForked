package me.khajiitos.jackseconomy.init;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.create.CreateCheck;
import me.khajiitos.jackseconomy.create.CreateContainerReg;
import me.khajiitos.jackseconomy.menu.*;
import me.khajiitos.jackseconomy.screen.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public class ContainerReg {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, JacksEconomy.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<ExporterMenu>> EXPORTER_MENU = MENU_TYPES.register("exporter", regBlockMenu(ExporterMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ImporterMenu>> IMPORTER_MENU = MENU_TYPES.register("importer", regBlockMenu(ImporterMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<FluidExporterMenu>> FLUID_EXPORTER_MENU = MENU_TYPES.register("fluid_exporter", regBlockMenu(FluidExporterMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<FluidImporterMenu>> FLUID_IMPORTER_MENU = MENU_TYPES.register("fluid_importer", regBlockMenu(FluidImporterMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<MechanicalExporterMenu>> MECHANICAL_EXPORTER_MENU = CreateCheck.isInstalled() ? CreateContainerReg.MECHANICAL_EXPORTER_MENU : null;
    public static final DeferredHolder<MenuType<?>, MenuType<MechanicalImporterMenu>> MECHANICAL_IMPORTER_MENU = CreateCheck.isInstalled() ? CreateContainerReg.MECHANICAL_IMPORTER_MENU : null;
    public static final DeferredHolder<MenuType<?>, MenuType<MechanicalFluidExporterMenu>> MECHANICAL_FLUID_EXPORTER_MENU = CreateCheck.isInstalled() ? CreateContainerReg.MECHANICAL_FLUID_EXPORTER_MENU : null;
    public static final DeferredHolder<MenuType<?>, MenuType<MechanicalFluidImporterMenu>> MECHANICAL_FLUID_IMPORTER_MENU = CreateCheck.isInstalled() ? CreateContainerReg.MECHANICAL_FLUID_IMPORTER_MENU : null;
    public static final DeferredHolder<MenuType<?>, MenuType<CurrencyConverterMenu>> CURRENCY_CONVERTER_MENU = MENU_TYPES.register("currency_converter", regBlockMenu(CurrencyConverterMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<WalletMenu>> WALLET_MENU = MENU_TYPES.register("wallet", regItemMenu(WalletMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<OIMWalletMenu>> OIM_WALLET_MENU = MENU_TYPES.register("oim_wallet", regItemMenu(OIMWalletMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AdminShopMenu>> ADMIN_SHOP_MENU = MENU_TYPES.register("admin_shop", () -> new MenuType<>(AdminShopMenu::new, FeatureFlagSet.of()));
    public static final DeferredHolder<MenuType<?>, MenuType<ExporterTicketCreatorMenu>> EXPORTER_TICKET_CREATOR_MENU = MENU_TYPES.register("exporter_ticket_creator", () -> new MenuType<>(ExporterTicketCreatorMenu::new, FeatureFlagSet.of()));
    public static final DeferredHolder<MenuType<?>, MenuType<ImporterTicketCreatorMenu>> IMPORTER_TICKET_CREATOR_MENU = MENU_TYPES.register("importer_ticket_creator", () -> new MenuType<>(ImporterTicketCreatorMenu::new, FeatureFlagSet.of()));
    public static final DeferredHolder<MenuType<?>, MenuType<FluidExporterTicketCreatorMenu>> FLUID_EXPORTER_TICKET_CREATOR_MENU = MENU_TYPES.register("fluid_exporter_ticket_creator", () -> new MenuType<>(FluidExporterTicketCreatorMenu::new, FeatureFlagSet.of()));
    public static final DeferredHolder<MenuType<?>, MenuType<FluidImporterTicketCreatorMenu>> FLUID_IMPORTER_TICKET_CREATOR_MENU = MENU_TYPES.register("fluid_importer_ticket_creator", () -> new MenuType<>(FluidImporterTicketCreatorMenu::new, FeatureFlagSet.of()));

    public static final DeferredHolder<MenuType<?>, MenuType<BulkFluidScreen.Menu>> BULK_FLUID_MENU = MENU_TYPES.register("bulk_fluid_menu", () -> new MenuType<>(BulkFluidScreen.Menu::new, FeatureFlagSet.of()));
    public static final DeferredHolder<MenuType<?>, MenuType<BulkItemScreen.Menu>> BULK_ITEM_MENU = MENU_TYPES.register("bulk_item_menu", () -> new MenuType<>(BulkItemScreen.Menu::new, FeatureFlagSet.of()));
    public static final DeferredHolder<MenuType<?>, MenuType<BulkAdminShopScreen.Menu>> BULK_ADMIN_SHOP_MENU = MENU_TYPES.register("bulk_admin_shop_menu", () -> new MenuType<>(BulkAdminShopScreen.Menu::new, FeatureFlagSet.of()));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }

    // Borrowed from Calemi's Economy
    public static <M extends AbstractContainerMenu> Supplier<MenuType<M>> regBlockMenu(BlockMenuFactory<M> factory) {
        return () -> new MenuType<>(factory, FeatureFlagSet.of());
    }

    public static <M extends AbstractContainerMenu> Supplier<MenuType<M>> regItemMenu(ItemMenuFactory<M> factory) {
        return () -> new MenuType<>(factory, FeatureFlagSet.of());
    }

    public interface BlockMenuFactory<M extends AbstractContainerMenu> extends IContainerFactory<M> {
        default M create(int windowId, Inventory inv, RegistryFriendlyByteBuf data) {
            return this.create(windowId, inv, data.readBlockPos());
        }

        M create(int var1, Inventory var2, BlockPos var3);
    }

    public interface ItemMenuFactory<M extends AbstractContainerMenu> extends IContainerFactory<M> {
        default M create(int windowId, Inventory inv, RegistryFriendlyByteBuf data) {
            CompoundTag tag = data.readNbt();
            Optional<ItemStack> stack = ItemStack.parse(JacksEconomy.server.registryAccess(), tag);
            if (stack.isEmpty()) {
                JacksEconomy.LOGGER.error("Error finding item stack in tag {}", tag);
                return null;
            }
            return create(windowId, inv, stack.get());
        }

        M create(int id, Inventory inventory, ItemStack stack);
    }

    public static void createBufferForBlockMenu(RegistryFriendlyByteBuf buf, BlockPos pos) {
        buf.writeBlockPos(pos);
    }
    public static void createBufferForItemMenu(RegistryFriendlyByteBuf buf, ItemStack stack) {
        buf.writeNbt(stack.save(buf.registryAccess(), new CompoundTag()));
    }
}
