package me.khajiitos.jackseconomy.jei;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.create.CreateCheck;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


// FIXME: Need to sync data BEFORE clients join the game
//  Not sure how
@JeiPlugin
public class JeiIntegration implements IModPlugin {
	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return new ResourceLocation(JacksEconomy.MOD_ID, "jei_plugin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new AdminShopBuyingCategory(registration.getJeiHelpers()));
		registration.addRecipeCategories(new AdminShopSellingCategory(registration.getJeiHelpers()));
		registration.addRecipeCategories(new ImportingCategory(registration.getJeiHelpers()));
		registration.addRecipeCategories(new ExportingCategory(registration.getJeiHelpers()));
		registration.addRecipeCategories(new FluidImportingCategory(registration.getJeiHelpers()));
		registration.addRecipeCategories(new FluidExportingCategory(registration.getJeiHelpers()));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(ItemBlockReg.IMPORTER.get(), ImportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.EXPORTER.get(), ExportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.FLUID_IMPORTER.get(), FluidImportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.FLUID_EXPORTER.get(), FluidExportingCategory.RECIPE_TYPE);

		if (!CreateCheck.isAnyInstalled()) return;

		registration.addRecipeCatalyst(ItemBlockReg.MECHANICAL_IMPORTER.get(), ImportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.MECHANICAL_EXPORTER.get(), ExportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.MECHANICAL_FLUID_IMPORTER.get(), FluidImportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.MECHANICAL_FLUID_EXPORTER.get(), FluidExportingCategory.RECIPE_TYPE);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(ItemBlockReg.ADMIN_SHOP.get().asItem(), (ingredient, context) -> {
			CompoundTag tag = ingredient.getOrCreateTag();
			return tag.contains("adminShopName") ? tag.getString("adminShopName") : "";
		});
	}

	@Override
	public void registerAdvanced(IAdvancedRegistration registration) {
		registration.addTypedRecipeManagerPlugin(AdminShopBuyingCategory.RECIPE_TYPE, new AdminShopBuyingCategory.RecipeManager());
		registration.addTypedRecipeManagerPlugin(AdminShopSellingCategory.RECIPE_TYPE, new AdminShopSellingCategory.RecipeManager());
		registration.addTypedRecipeManagerPlugin(ExportingCategory.RECIPE_TYPE, new ExportingCategory.RecipeManager());
		registration.addTypedRecipeManagerPlugin(ImportingCategory.RECIPE_TYPE, new ImportingCategory.RecipeManager());
		registration.addTypedRecipeManagerPlugin(FluidExportingCategory.RECIPE_TYPE, new FluidExportingCategory.RecipeManager());
		registration.addTypedRecipeManagerPlugin(FluidImportingCategory.RECIPE_TYPE, new FluidImportingCategory.RecipeManager());
	}
}
