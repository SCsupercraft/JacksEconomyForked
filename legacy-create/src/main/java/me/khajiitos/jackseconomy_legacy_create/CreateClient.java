package me.khajiitos.jackseconomy_legacy_create;

import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy_legacy_create.renderer.*;
import me.khajiitos.jackseconomy.screen.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class CreateClient {
	public static void onClientSetup(FMLClientSetupEvent e) {
		MenuScreens.register(ContainerReg.MECHANICAL_EXPORTER_MENU.get(), MechanicalExporterScreen::new);
		MenuScreens.register(ContainerReg.MECHANICAL_IMPORTER_MENU.get(), MechanicalImporterScreen::new);
		MenuScreens.register(ContainerReg.MECHANICAL_FLUID_EXPORTER_MENU.get(), MechanicalFluidExporterScreen::new);
		MenuScreens.register(ContainerReg.MECHANICAL_FLUID_IMPORTER_MENU.get(), MechanicalFluidImporterScreen::new);

        /*
        InstancedRenderRegistry.configure(BlockEntityReg.MECHANICAL_IMPORTER.get())
                .factory(HorizontalHalfShaftInstance::new)
                //.skipRender(be -> false)
                .apply();

        InstancedRenderRegistry.configure(BlockEntityReg.MECHANICAL_EXPORTER.get())
                .factory(HorizontalHalfShaftInstance::new)
                //.skipRender(be -> false)
                .apply();*/
	}
	public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers e) {
		e.registerBlockEntityRenderer(BlockEntityReg.MECHANICAL_EXPORTER.get(), MechanicalTransactionMachineRenderer::new);
		e.registerBlockEntityRenderer(BlockEntityReg.MECHANICAL_IMPORTER.get(), MechanicalTransactionMachineRenderer::new);
		e.registerBlockEntityRenderer(BlockEntityReg.MECHANICAL_FLUID_EXPORTER.get(), MechanicalFluidTransactionMachineRenderer::new);
		e.registerBlockEntityRenderer(BlockEntityReg.MECHANICAL_FLUID_IMPORTER.get(), MechanicalFluidTransactionMachineRenderer::new);
	}
}
