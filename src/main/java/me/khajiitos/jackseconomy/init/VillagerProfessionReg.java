package me.khajiitos.jackseconomy.init;

import com.google.common.collect.ImmutableSet;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class VillagerProfessionReg {
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(
            Registries.VILLAGER_PROFESSION, JacksEconomy.MOD_ID);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> SHOPKEEPER =
            PROFESSIONS.register("shopkeeper", () -> new VillagerProfession(
                    "shopkeeper",
                    holder -> holder.is(PoiTypeReg.ADMIN_SHOP.getKey()),
                    holder -> holder.is(PoiTypeReg.ADMIN_SHOP.getKey()),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN
            ));

    public static void init(IEventBus bus) {
        PROFESSIONS.register(bus);
    }
}
