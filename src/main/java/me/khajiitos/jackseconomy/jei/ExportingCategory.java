package me.khajiitos.jackseconomy.jei;

import me.khajiitos.jackseconomy.JacksEconomy;
import me.khajiitos.jackseconomy.JacksEconomyClient;
import me.khajiitos.jackseconomy.data.price.ItemDescription;
import me.khajiitos.jackseconomy.init.ItemBlockReg;
import me.khajiitos.jackseconomy.item.CurrencyItem;
import me.khajiitos.jackseconomy.util.CurrencyHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExportingCategory implements IRecipeCategory<ExportingCategory.Details> {
	public static final RecipeType<Details> RECIPE_TYPE = RecipeType.create(JacksEconomy.MOD_ID, "exporting", Details.class);
	private static final ResourceLocation BACKGROUND = new ResourceLocation(JacksEconomy.MOD_ID, "textures/gui/jei.png");
	protected final IJeiHelpers helpers;

	ExportingCategory(IJeiHelpers helpers) {
		this.helpers = helpers;
	}

	@Override
	public @NotNull RecipeType<Details> getRecipeType() {
		return RECIPE_TYPE;
	}

	@Override
	public @NotNull Component getTitle() {
		return Component.literal("Exporting");
	}

	@Override
	public @Nullable IDrawable getIcon() {
		return helpers.getGuiHelper().createDrawableItemLike(ItemBlockReg.EXPORTER.get());
	}

	@Override
	public int getWidth() {
		return 150;
	}

	@Override
	public int getHeight() {
		return 50;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull Details details, @NotNull IFocusGroup focuses) {
		if (details.description == null || Objects.equals(details.price, BigDecimal.ZERO)) return;

		ItemStack moneyStack = new ItemStack(ItemBlockReg.DOLLAR_BILL_ITEM.get());

		builder.addSlot(RecipeIngredientRole.CATALYST, 42, 5).addItemLike(ItemBlockReg.EXPORTER.get());
		builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 100, 29).addItemStack(moneyStack).addRichTooltipCallback((recipeSlotView, tooltip) -> {
			tooltip.add(Component.literal(CurrencyHelper.format(details.price)).withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.YELLOW)));
		});
		builder.addOutputSlot(34, 29).addItemStack(details.description().createItemStack());
	}

	@Override
	public void draw(Details details, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		if (details.description == null || Objects.equals(details.price, BigDecimal.ZERO)) {
			Font font = Minecraft.getInstance().font;
			guiGraphics.drawCenteredString(font, Component.translatable("jackseconomy.jei_placeholder_manifest_required"), getWidth() / 2, getHeight() / 2 - (font.lineHeight / 2), -1);
			return;
		}
		guiGraphics.blit(BACKGROUND, 25, 0, 0, 0, 100, 50, 100, 50);
	}

	public record Details(BigDecimal price, ItemDescription description) {}

	public static class RecipeManager implements ISimpleRecipeManagerPlugin<Details> {
		@Override
		public boolean isHandledInput(@NotNull ITypedIngredient<?> input) {
			return !getRecipesForInput(input).isEmpty();
		}

		@Override
		public boolean isHandledOutput(@NotNull ITypedIngredient<?> output) {
			return !getRecipesForOutput(output).isEmpty();
		}

		@Override
		public @NotNull List<Details> getRecipesForInput(ITypedIngredient<?> input) {
			Optional<ItemStack> stack = input.getItemStack();
			if (stack.isEmpty() || stack.get().isEmpty()) return List.of();

			return getAllRecipes().stream().filter(details -> ItemStack.isSameItemSameTags(stack.get(), details.description.createItemStack())).collect(Collectors.toList());
		}

		@Override
		public @NotNull List<Details> getRecipesForOutput(ITypedIngredient<?> output) {
			return output.getItemStack().filter(stack -> stack.getItem() instanceof CurrencyItem).isPresent() ? getAllRecipes() : List.of();
		}

		@Override
		public @NotNull List<Details> getAllRecipes() {
			List<Details> list = new ArrayList<>();
			JacksEconomyClient.priceInfos.forEach((itemDescription, itemPriceInfo) -> {
				if (itemPriceInfo.sellPrice > 0) list.add(new Details(BigDecimal.valueOf(itemPriceInfo.sellPrice), itemDescription));
			});
			return list;
		}
	}
}
