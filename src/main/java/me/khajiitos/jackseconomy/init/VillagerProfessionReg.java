package me.khajiitos.jackseconomy.init;

import com.google.common.collect.ImmutableSet;
import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VillagerProfessionReg {
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(
            ForgeRegistries.VILLAGER_PROFESSIONS, JacksEconomy.MOD_ID);

    public static final RegistryObject<VillagerProfession> SHOPKEEPER = PROFESSIONS.register("shopkeeper", () ->
            new VillagerProfession(
                    "shopkeeper",
                    holder -> holder.is(PoiTypeReg.ADMIN_SHOP.getKey()),
                    holder -> holder.is(PoiTypeReg.ADMIN_SHOP.getKey()),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN
            )
    );

    public static void init(IEventBus bus) {
        PROFESSIONS.register(bus);
    }
}
