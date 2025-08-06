package me.khajiitos.jackseconomy.create;

import me.khajiitos.jackseconomy.create.renderer.MechanicalFluidTransactionMachineRenderer;
import me.khajiitos.jackseconomy.create.renderer.MechanicalTransactionMachineRenderer;
import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.screen.MechanicalExporterScreen;
import me.khajiitos.jackseconomy.screen.MechanicalFluidExporterScreen;
import me.khajiitos.jackseconomy.screen.MechanicalFluidImporterScreen;
import me.khajiitos.jackseconomy.screen.MechanicalImporterScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@OnlyIn(Dist.CLIENT)
public class CreateClient {
	public static void onClientSetup(FMLClientSetupEvent e) {
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

	public static void registerScreens(RegisterMenuScreensEvent e) {
		e.register(ContainerReg.MECHANICAL_EXPORTER_MENU.get(), MechanicalExporterScreen::new);
		e.register(ContainerReg.MECHANICAL_IMPORTER_MENU.get(), MechanicalImporterScreen::new);
		e.register(ContainerReg.MECHANICAL_FLUID_EXPORTER_MENU.get(), MechanicalFluidExporterScreen::new);
		e.register(ContainerReg.MECHANICAL_FLUID_IMPORTER_MENU.get(), MechanicalFluidImporterScreen::new);
	}

	public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers e) {
		e.registerBlockEntityRenderer(BlockEntityReg.MECHANICAL_EXPORTER.get(), MechanicalTransactionMachineRenderer::new);
		e.registerBlockEntityRenderer(BlockEntityReg.MECHANICAL_IMPORTER.get(), MechanicalTransactionMachineRenderer::new);
		e.registerBlockEntityRenderer(BlockEntityReg.MECHANICAL_FLUID_EXPORTER.get(), MechanicalFluidTransactionMachineRenderer::new);
		e.registerBlockEntityRenderer(BlockEntityReg.MECHANICAL_FLUID_IMPORTER.get(), MechanicalFluidTransactionMachineRenderer::new);
	}
}
