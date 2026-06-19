package me.khajiitos.jackseconomy.init;

import com.google.common.collect.ImmutableSet;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoiTypeReg {
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(
            Registries.POINT_OF_INTEREST_TYPE, JacksEconomy.MOD_ID);

    public static final DeferredHolder<PoiType, PoiType> ADMIN_SHOP = POI_TYPES.register("admin_shop", () ->
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
