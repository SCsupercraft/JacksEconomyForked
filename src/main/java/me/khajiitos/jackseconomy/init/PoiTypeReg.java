package me.khajiitos.jackseconomy.init;

import com.google.common.collect.ImmutableSet;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PoiTypeReg {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(
            ForgeRegistries.POI_TYPES, JacksEconomy.MOD_ID);

    public static final RegistryObject<PoiType> ADMIN_SHOP = POI_TYPES.register("admin_shop", () ->
           new PoiType(
                   ImmutableSet.copyOf(ItemBlockReg.ADMIN_SHOP.get().getStateDefinition().getPossibleStates()),
                   1,
                   1
           )
    );

    public static void init(IEventBus bus) {
        POI_TYPES.register(bus);
    }
}
