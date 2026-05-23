package me.khajiitos.jackseconomy.mixin;

import me.khajiitos.jackseconomy.blockentity.AdminShopBlockEntity;
import me.khajiitos.jackseconomy.config.Config;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import me.khajiitos.jackseconomy.init.VillagerProfessionReg;
import me.khajiitos.jackseconomy.menu.AdminShopMenu;
import me.khajiitos.jackseconomy.packet.AdminShopSchemaPacket;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {
    @Shadow
    public abstract VillagerData getVillagerData();

    @Shadow
    protected abstract void setUnhappy();

    @Shadow
    public abstract void setTradingPlayer(@Nullable Player pPlayer);

    public VillagerMixin(EntityType<? extends AbstractVillager> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void onMobInteract(Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (getVillagerData().getProfession() != VillagerProfessionReg.SHOPKEEPER.get()) return;

        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (itemstack.getItem() != Items.VILLAGER_SPAWN_EGG && this.isAlive() && !this.isTrading() && !this.isSleeping() && !pPlayer.isSecondaryUseActive()) {
            if (this.isBaby()) {
                this.setUnhappy();
                cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
            } else {
                if (pHand == InteractionHand.MAIN_HAND) {
                    pPlayer.awardStat(Stats.TALKED_TO_VILLAGER);
                }

                if (!this.level().isClientSide) {
                    GlobalPos pos = this.brain.getMemory(MemoryModuleType.JOB_SITE).orElse(null);
                    if (pos == null) return;

                    MinecraftServer server = getServer();
                    if (server == null) return;

                    ServerLevel level = server.getLevel(pos.dimension());
                    if (level == null) return;

                    BlockEntity blockEntity = level.getBlockEntity(pos.pos());
                    if (!(blockEntity instanceof AdminShopBlockEntity be) || !(pPlayer instanceof ServerPlayer player)) return;

                    String name = be.getName();

                    setTradingPlayer(player);

                    CompoundTag compoundTag = PriceManager.toAdminShopSchemaCompound(player, name);
                    player.openMenu(new SimpleMenuProvider(
                            (pContainerId, pPlayerInventory, pPlayer1) ->
                                    new AdminShopMenu(pContainerId, pPlayerInventory, this), Component.empty()));
                    PacketDistributor.sendToPlayer(player,
                            new AdminShopSchemaPacket(compoundTag, Optional.ofNullable(name), Config.oneItemCurrencyMode.get()));
                }
                cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
            }
        } else {
            cir.setReturnValue(super.mobInteract(pPlayer, pHand));
        }
    }
}
