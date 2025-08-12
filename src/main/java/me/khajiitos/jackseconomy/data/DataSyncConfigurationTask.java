package me.khajiitos.jackseconomy.data;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import me.khajiitos.jackseconomy.packet.PricesInfoPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record DataSyncConfigurationTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
	public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "data_sync"));

	@Override
	public void run(final Consumer<CustomPacketPayload> sender) {
		sender.accept(new PricesInfoPacket(PriceManager.toTag(false), PriceManager.toTag(true)));
		Set<String> names = PriceManager.getCategories().keySet().stream().map(PriceManager.Category::adminShopName).filter(Objects::nonNull).collect(Collectors.toSet());

		for (String name: names) {
			sender.accept(new AdminShopSchemaPacket(PriceManager.toAdminShopSchemaCompound(null, name), Optional.of(name), Config.oneItemCurrencyMode.get()));
		}
		sender.accept(new AdminShopSchemaPacket(PriceManager.toAdminShopSchemaCompound(null, null), Optional.empty(), Config.oneItemCurrencyMode.get()));

		sender.accept(AdminShopColorManager.toUpdatePacket());
		this.listener().finishCurrentTask(this.type());
	}

	@Override
	public ConfigurationTask.Type type() {
		return TYPE;
	}
}
