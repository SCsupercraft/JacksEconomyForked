package me.khajiitos.jackseconomy.mixin;

import me.khajiitos.jackseconomy.init.VillagerProfessionReg;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.world.entity.npc.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VillagerProfessionLayer.class)
public class VillagerProfessionLayerMixin {
    @Desc(value = "render",args={com.mojang.blaze3d.vertex.PoseStack.class, net.minecraft.client.renderer.MultiBufferSource.class, int.class, net.minecraft.world.entity.LivingEntity.class, float.class, float.class, float.class, float.class, float.class, float.class})
    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/VillagerData;getLevel()I"))
    private int overrideProfessionLevel(VillagerData instance) {
        return instance.getProfession() == VillagerProfessionReg.SHOPKEEPER.get()
                ? 3 // Gold
                : instance.getLevel();
    }
}
