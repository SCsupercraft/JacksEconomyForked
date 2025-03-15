package me.khajiitos.jackseconomy.create.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import me.khajiitos.jackseconomy.block.KineticTransactionMachineBlock;
import me.khajiitos.jackseconomy.blockentity.TransactionKineticMachineBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MechanicalTransactionMachineRenderer<T extends TransactionKineticMachineBlockEntity> extends KineticBlockEntityRenderer<T> {

    public MechanicalTransactionMachineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = getRenderedBlockState(be);
        RenderType type = getRenderType(be, state);
        if (type != null)
            renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(TransactionKineticMachineBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state
                .getValue(KineticTransactionMachineBlock.HORIZONTAL_FACING)
                .getOpposite());
    }
}
