package me.khajiitos.jackseconomy.create;

import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.blockentity.MechanicalFluidImporterBlockEntity;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CreatePonder implements PonderPlugin {
	public static void init() {
		PonderIndex.addPlugin(new CreatePonder());
	}

	/**
	 * @return the modID of the mod that added this plugin
	 */
	@Override
	public @NotNull String getModId() {
		return JacksEconomy.MOD_ID;
	}

	/**
	 * Register all the Ponder Scenes added by your Mod
	 *
	 * @param HELPER The ponder scene registration helper
	 */
	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> HELPER) {
		HELPER.addStoryBoard(ItemBlockReg.EXPORTER.getId(), "exporting", CreatePonder::exportingPonder);
		HELPER.addStoryBoard(ItemBlockReg.MECHANICAL_EXPORTER.getId(), "exporting", CreatePonder::exportingPonder);

		HELPER.addStoryBoard(ItemBlockReg.IMPORTER.getId(), "importing", CreatePonder::importingPonder);
		HELPER.addStoryBoard(ItemBlockReg.MECHANICAL_IMPORTER.getId(), "importing", CreatePonder::importingPonder);

		HELPER.addStoryBoard(ItemBlockReg.FLUID_EXPORTER.getId(), "fluid_exporting", CreatePonder::fluidExportingPonder);
		HELPER.addStoryBoard(ItemBlockReg.MECHANICAL_FLUID_EXPORTER.getId(), "fluid_exporting", CreatePonder::fluidExportingPonder);

		HELPER.addStoryBoard(ItemBlockReg.FLUID_IMPORTER.getId(), "fluid_importing", CreatePonder::fluidImportingPonder);
		HELPER.addStoryBoard(ItemBlockReg.MECHANICAL_FLUID_IMPORTER.getId(), "fluid_importing", CreatePonder::fluidImportingPonder);

		HELPER.addStoryBoard(ItemBlockReg.CURRENCY_CONVERTER.getId(), "converting_currency", CreatePonder::convertingCurrency);
	}

	private static void exportingPonder(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("exporting", "Exporting Items");
		scene.showBasePlate();
		scene.world().showSection(util.select().layersFrom(1), Direction.UP);
		scene.idle(20);

		scene.overlay().showText(100)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Exporters can be used to automate the process of selling items.");
		scene.idle(120);
		scene.addKeyframe();

		scene.overlay().showText(100)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Exporters need manifests to function, without them they are useless.");
		scene.idle(120);
		scene.addKeyframe();

		scene.world().createItemOnBelt(util.grid().at(6, 1, 3), Direction.DOWN, new ItemStack(Items.DIAMOND_BLOCK, 1));
		scene.idle(60);
		scene.world().removeItemsFromBelt(util.grid().at(4, 1, 3));

		scene.addKeyframe();
		scene.overlay().showText(130)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Exporters take a while to sell items, so make sure you have enough of them to avoid overflows.");

		scene.idle(150);

		scene.effects().emitParticles(util.vector().of(3.5, 3.25, 3.5), scene.effects().simpleParticleEmitter(ParticleTypes.HAPPY_VILLAGER, util.vector().of(0.2, 0.15, 0.2)), 3, 1);
		scene.world().createItemOnBelt(util.grid().at(2, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.HUNDRED_DOLLAR_BILL_ITEM.get(),1));
		scene.idle(40);

		scene.markAsFinished();
	}
	private static void importingPonder(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("importing", "Importing Items");
		scene.showBasePlate();
		scene.world().showSection(util.select().layersFrom(1), Direction.UP);
		scene.idle(20);

		scene.overlay().showText(100)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Importers can be used to automate the process of buying items.");
		scene.idle(120);
		scene.addKeyframe();

		scene.overlay().showText(100)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Importers need manifests to function, without them they are useless.");
		scene.idle(120);
		scene.addKeyframe();

		scene.world().createItemOnBelt(util.grid().at(6, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.FIVE_DOLLAR_BILL_ITEM.get(), 3));
		scene.idle(50);
		scene.world().createItemOnBelt(util.grid().at(6, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.DOLLAR_BILL_ITEM.get(), 14));
		scene.idle(20);
		scene.world().removeItemsFromBelt(util.grid().at(4, 1, 3));
		scene.idle(60);
		scene.world().removeItemsFromBelt(util.grid().at(4, 1, 3));

		scene.addKeyframe();
		scene.overlay().showText(130)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Importers take a while to buy items, so make sure you have enough of them to sustain your needs.");

		scene.idle(150);

		scene.effects().emitParticles(util.vector().of(3.5, 3.25, 3.5), scene.effects().simpleParticleEmitter(ParticleTypes.HAPPY_VILLAGER, util.vector().of(0.2, 0.15, 0.2)), 3, 1);
		scene.world().createItemOnBelt(util.grid().at(2, 1, 3), Direction.DOWN, new ItemStack(Items.DIAMOND,1));
		scene.idle(40);

		scene.markAsFinished();
	}
	private static void fluidExportingPonder(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("fluid_exporting", "Exporting Fluids");
		scene.showBasePlate();
		scene.world().showSection(util.select().layersFrom(1), Direction.UP);
		scene.idle(20);

		scene.overlay().showText(100)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Fluid exporters can be used to automate the process of selling fluids.");
		scene.idle(120);
		scene.addKeyframe();

		scene.overlay().showText(100)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Fluid exporters need manifests to function, without them they are useless.");
		scene.idle(120);
		scene.addKeyframe();

		scene.world().modifyBlock(util.grid().at(5, 2, 3), state -> state.setValue(FluidValveBlock.ENABLED, true), false);
		scene.idle(120);

		scene.addKeyframe();
		scene.overlay().showText(130)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Fluid exporters take a while to sell fluids, so make sure you have enough of them to avoid overflows.");

		scene.idle(150);

		scene.effects().emitParticles(util.vector().of(3.5, 3.25, 3.5), scene.effects().simpleParticleEmitter(ParticleTypes.HAPPY_VILLAGER, util.vector().of(0.2, 0.15, 0.2)), 3, 1);
		scene.world().createItemOnBelt(util.grid().at(2, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.TWENTY_DOLLAR_BILL_ITEM.get(),1));
		scene.idle(40);

		scene.markAsFinished();
	}
	private static void fluidImportingPonder(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("fluid_importing", "Importing Fluids");
		scene.showBasePlate();
		scene.world().showSection(util.select().layersFrom(1), Direction.UP);
		scene.idle(20);

		scene.overlay().showText(100)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Fluid importers can be used to automate the process of buying fluids.");
		scene.idle(120);
		scene.addKeyframe();

		scene.overlay().showText(100)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Fluid importers need manifests to function, without them they are useless.");
		scene.idle(120);
		scene.addKeyframe();

		scene.world().createItemOnBelt(util.grid().at(6, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.FIVE_DOLLAR_BILL_ITEM.get(), 3));
		scene.idle(50);
		scene.world().createItemOnBelt(util.grid().at(6, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.DOLLAR_BILL_ITEM.get(), 14));
		scene.idle(20);
		scene.world().removeItemsFromBelt(util.grid().at(4, 1, 3));
		scene.idle(60);
		scene.world().removeItemsFromBelt(util.grid().at(4, 1, 3));

		scene.addKeyframe();
		scene.overlay().showText(130)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Fluid importers take a while to buy fluids, so make sure you have enough of them to sustain your needs.");

		scene.idle(150);

		scene.effects().emitParticles(util.vector().of(3.5, 3.25, 3.5), scene.effects().simpleParticleEmitter(ParticleTypes.HAPPY_VILLAGER, util.vector().of(0.2, 0.15, 0.2)), 3, 1);
		scene.world().modifyBlockEntity(util.grid().at(3, 2, 3), MechanicalFluidImporterBlockEntity.class, (entity -> {
			entity.getFluidStorage().setValidator(fluidStack -> true);
			entity.getFluidStorage().fill(new FluidStack(Fluids.WATER.getFlowing(), 11000), IFluidHandler.FluidAction.EXECUTE);
		}));
		scene.idle(40);

		scene.markAsFinished();
	}
	private static void convertingCurrency(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);

		scene.title("converting_currency", "Converting Currency");
		scene.showBasePlate();
		scene.world().showSection(util.select().layersFrom(1), Direction.UP);
		scene.idle(20);

		scene.overlay().showText(140)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Currency converters can be used to convert money into other forms, such as dollar bills to ten dollar bills.");
		scene.idle(160);
		scene.addKeyframe();

		scene.overlay().showText(90)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("Currency converters don't use power, unlike other machines.");
		scene.idle(110);
		scene.addKeyframe();

		scene.world().createItemOnBelt(util.grid().at(6, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.DOLLAR_BILL_ITEM.get(), 20));
		scene.idle(20);
		scene.overlay().showText(100)
				.pointAt(util.vector().of(3, 2.5, 3))
				.text("They convert currencies instantly, so no need to wait.");
		scene.idle(40);
		scene.world().removeItemsFromBelt(util.grid().at(4, 1, 3));

		scene.effects().emitParticles(util.vector().of(3.5, 3.25, 3.5), scene.effects().simpleParticleEmitter(ParticleTypes.HAPPY_VILLAGER, util.vector().of(0.2, 0.15, 0.2)), 3, 1);
		scene.world().createItemOnBelt(util.grid().at(2, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.TEN_DOLLAR_BILL_ITEM.get(),1));
		scene.idle(40);

		scene.markAsFinished();
	}
}
