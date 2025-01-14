package me.khajiitos.jackseconomy.create;

import com.simibubi.create.foundation.ponder.*;
import com.simibubi.create.foundation.ponder.instruction.EmitParticlesInstruction;
import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CreatePonder {
	static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(JacksEconomy.MOD_ID);

	public static void register() {
		HELPER.addStoryBoard(ItemBlockReg.EXPORTER.getId(), "exporting", CreatePonder::exportingPonder);
		HELPER.addStoryBoard(ItemBlockReg.MECHANICAL_EXPORTER.getId(), "exporting", CreatePonder::exportingPonder);

		HELPER.addStoryBoard(ItemBlockReg.IMPORTER.getId(), "importing", CreatePonder::importingPonder);
		HELPER.addStoryBoard(ItemBlockReg.MECHANICAL_IMPORTER.getId(), "importing", CreatePonder::importingPonder);

		HELPER.addStoryBoard(ItemBlockReg.CURRENCY_CONVERTER.getId(), "converting_currency", CreatePonder::convertingCurrency);
	}

	private static void exportingPonder(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("exporting", "Exporting Items");
		scene.showBasePlate();
		scene.world.showSection(util.select.layersFrom(1), Direction.UP);
		scene.idle(20);

		scene.overlay.showText(100)
				.pointAt(util.vector.of(3, 2.5, 3))
				.text("Exporters can be used to automate the process of selling items.");
		scene.idle(120);
		scene.addKeyframe();

		scene.overlay.showText(100)
				.pointAt(util.vector.of(3, 2.5, 3))
				.text("Exporters need manifests to function, without them they are useless.");
		scene.idle(120);
		scene.addKeyframe();

		scene.world.createItemOnBelt(util.grid.at(6, 1, 3), Direction.DOWN, new ItemStack(Items.DIAMOND_BLOCK, 1));
		scene.idle(60);
		scene.world.removeItemsFromBelt(util.grid.at(4, 1, 3));

		scene.addKeyframe();
		scene.overlay.showText(130)
				.pointAt(util.vector.of(3, 2.5, 3))
				.text("Exporters take a while to sell items, so make sure you have enough of them to avoid overflows.");

		scene.idle(150);

		scene.effects.emitParticles(util.vector.of(3.5, 3.25, 3.5), EmitParticlesInstruction.Emitter.simple(ParticleTypes.HAPPY_VILLAGER, util.vector.of(0.2, 0.15, 0.2)), 3, 1);
		scene.world.createItemOnBelt(util.grid.at(2, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.HUNDRED_DOLLAR_BILL_ITEM.get(),1));
		scene.idle(40);

		scene.markAsFinished();
	}
	private static void importingPonder(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("importing", "Importing Items");
		scene.showBasePlate();
		scene.world.showSection(util.select.layersFrom(1), Direction.UP);
		scene.idle(20);

		scene.overlay.showText(100)
				.pointAt(util.vector.of(3, 2.5, 3))
				.text("Importers can be used to automate the process of buying items.");
		scene.idle(120);
		scene.addKeyframe();

		scene.overlay.showText(100)
				.pointAt(util.vector.of(3, 2.5, 3))
				.text("Importers need manifests to function, without them they are useless.");
		scene.idle(120);
		scene.addKeyframe();

		scene.world.createItemOnBelt(util.grid.at(6, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.FIVE_DOLLAR_BILL_ITEM.get(), 3));
		scene.idle(50);
		scene.world.createItemOnBelt(util.grid.at(6, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.DOLLAR_BILL_ITEM.get(), 14));
		scene.idle(20);
		scene.world.removeItemsFromBelt(util.grid.at(4, 1, 3));
		scene.idle(60);
		scene.world.removeItemsFromBelt(util.grid.at(4, 1, 3));

		scene.addKeyframe();
		scene.overlay.showText(130)
				.pointAt(util.vector.of(3, 2.5, 3))
				.text("Importers take a while to buy items, so make sure you have enough of them to sustain your needs.");

		scene.idle(150);

		scene.effects.emitParticles(util.vector.of(3.5, 3.25, 3.5), EmitParticlesInstruction.Emitter.simple(ParticleTypes.HAPPY_VILLAGER, util.vector.of(0.2, 0.15, 0.2)), 3, 1);
		scene.world.createItemOnBelt(util.grid.at(2, 1, 3), Direction.DOWN, new ItemStack(Items.DIAMOND,1));
		scene.idle(40);

		scene.markAsFinished();
	}
	private static void convertingCurrency(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("converting_currency", "Converting Currency");
		scene.showBasePlate();
		scene.world.showSection(util.select.layersFrom(1), Direction.UP);
		scene.idle(20);

		scene.overlay.showText(140)
				.pointAt(util.vector.of(3, 2.5, 3))
				.text("Currency converters can be used to convert money into other forms, such as dollar bills to ten dollar bills.");
		scene.idle(160);
		scene.addKeyframe();

		scene.overlay.showText(90)
				.pointAt(util.vector.of(3, 2.5, 3))
				.text("Currency converters don't use power, unlike other machines.");
		scene.idle(110);
		scene.addKeyframe();

		scene.world.createItemOnBelt(util.grid.at(6, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.DOLLAR_BILL_ITEM.get(), 20));
		scene.idle(20);
		scene.overlay.showText(100)
				.pointAt(util.vector.of(3, 2.5, 3))
				.text("They convert currencies instantly, so no need to wait.");
		scene.idle(40);
		scene.world.removeItemsFromBelt(util.grid.at(4, 1, 3));

		scene.effects.emitParticles(util.vector.of(3.5, 3.25, 3.5), EmitParticlesInstruction.Emitter.simple(ParticleTypes.HAPPY_VILLAGER, util.vector.of(0.2, 0.15, 0.2)), 3, 1);
		scene.world.createItemOnBelt(util.grid.at(2, 1, 3), Direction.DOWN, new ItemStack(ItemBlockReg.TEN_DOLLAR_BILL_ITEM.get(),1));
		scene.idle(40);

		scene.markAsFinished();
	}
}
