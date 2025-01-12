package me.khajiitos.jackseconomy.create;

import me.khajiitos.jackseconomy.init.BlockEntityReg;
import me.khajiitos.jackseconomy.init.ContainerReg;
import me.khajiitos.jackseconomy.renderer.MechanicalTransactionMachineRenderer;
import me.khajiitos.jackseconomy.screen.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class CreateClient {
	public static void onClientSetup(FMLClientSetupEvent e) {
		MenuScreens.register(ContainerReg.MECHANICAL_EXPORTER_MENU.get(), MechanicalExporterScreen::new);
		MenuScreens.register(ContainerReg.MECHANICAL_IMPORTER_MENU.get(), MechanicalImporterScreen::new);

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
	}
}
