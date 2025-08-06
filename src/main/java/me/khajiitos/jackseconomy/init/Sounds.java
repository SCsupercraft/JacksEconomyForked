package me.khajiitos.jackseconomy.init;

import me.khajiitos.jackseconomy.JacksEconomy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Sounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, JacksEconomy.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> COIN = SOUND_EVENTS.register("coin", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "coin")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CASH = SOUND_EVENTS.register("cash", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "cash")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CHECKOUT = SOUND_EVENTS.register("checkout", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "checkout")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
