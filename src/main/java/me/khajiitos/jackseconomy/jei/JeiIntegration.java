package me.khajiitos.jackseconomy.jei;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.create.CreateCheck;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.screen.FluidTicketCreatorScreen;
import me.khajiitos.jackseconomy.screen.TicketCreatorScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JeiIntegration implements IModPlugin {
	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath(JacksEconomy.MOD_ID, "jei_plugin");
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
		registration.addRecipeCatalyst(ItemBlockReg.IMPORTER, ImportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.EXPORTER, ExportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.FLUID_IMPORTER, FluidImportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.FLUID_EXPORTER, FluidExportingCategory.RECIPE_TYPE);

		if (!CreateCheck.isInstalled()) return;

		registration.addRecipeCatalyst(ItemBlockReg.MECHANICAL_IMPORTER, ImportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.MECHANICAL_EXPORTER, ExportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.MECHANICAL_FLUID_IMPORTER, FluidImportingCategory.RECIPE_TYPE);
		registration.addRecipeCatalyst(ItemBlockReg.MECHANICAL_FLUID_EXPORTER, FluidExportingCategory.RECIPE_TYPE);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(ItemBlockReg.ADMIN_SHOP.asItem(), new ISubtypeInterpreter<>() {
			@Override
			public @NotNull Object getSubtypeData(@NotNull ItemStack ingredient, @NotNull UidContext context) {
				return getLegacyStringSubtypeInfo(ingredient, context);
			}

			@Override
			public @NotNull String getLegacyStringSubtypeInfo(ItemStack ingredient, @NotNull UidContext context) {
				CompoundTag tag = ingredient.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
				return tag.contains("adminShopName") ? tag.getString("adminShopName") : "";
			}
		});
	}

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(TicketCreatorScreen.class, new TicketCreatorJei());
        registration.addGhostIngredientHandler(FluidTicketCreatorScreen.class, new FluidTicketCreatorJei());
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
