package me.khajiitos.jackseconomy_legacy_create.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import me.khajiitos.jackseconomy.block.KineticFluidTransactionMachineBlock;
import me.khajiitos.jackseconomy.blockentity.FluidTransactionKineticMachineBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MechanicalFluidTransactionMachineRenderer<T extends FluidTransactionKineticMachineBlockEntity> extends KineticBlockEntityRenderer<T> {

	public MechanicalFluidTransactionMachineRenderer(BlockEntityRendererProvider.Context context) {
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
	protected SuperByteBuffer getRotatedModel(FluidTransactionKineticMachineBlockEntity be, BlockState state) {
		return CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF, state, state
				.getValue(KineticFluidTransactionMachineBlock.HORIZONTAL_FACING)
				.getOpposite());
	}

}