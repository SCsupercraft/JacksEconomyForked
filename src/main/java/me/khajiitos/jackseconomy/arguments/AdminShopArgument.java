package me.khajiitos.jackseconomy.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.khajiitos.jackseconomy.data.price.PriceManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AdminShopArgument implements ArgumentType<String> {
	private final StringArgumentType.StringType type;

	private AdminShopArgument(final StringArgumentType.StringType type) {
		this.type = type;
	}

	public static AdminShopArgument create(StringArgumentType.StringType type) {
		return new AdminShopArgument(type);
	}

	public static AdminShopArgument word() {
		return new AdminShopArgument(StringArgumentType.StringType.SINGLE_WORD);
	}

	public static AdminShopArgument string() {
		return new AdminShopArgument(StringArgumentType.StringType.QUOTABLE_PHRASE);
	}

	public static AdminShopArgument greedyString() {
		return new AdminShopArgument(StringArgumentType.StringType.GREEDY_PHRASE);
	}

	public static String getName(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, String.class);
	}

	public StringArgumentType.StringType getType() {
		return type;
	}

	@Override
	public String parse(final StringReader reader) throws CommandSyntaxException {
		if (type == StringArgumentType.StringType.GREEDY_PHRASE) {
			final String text = reader.getRemaining();
			reader.setCursor(reader.getTotalLength());
			return text;
		} else if (type == StringArgumentType.StringType.SINGLE_WORD) {
			return reader.readUnquotedString();
		} else {
			return reader.readString();
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(
				PriceManager
						.getCategories()
						.keySet()
						.stream()
						.map(PriceManager.Category::adminShopName)
						.filter(Objects::nonNull),
				builder);
	}

	@Override
	public Collection<String> getExamples() {
		return type.getExamples();
	}

	public static class Info implements ArgumentTypeInfo<AdminShopArgument, Info.Template> {
		public Info() {
		}

		public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeEnum(template.type);
		}

		public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
			return new Template(friendlyByteBuf.readEnum(StringArgumentType.StringType.class));
		}

		@Override
		public void serializeToJson(Template pTemplate, JsonObject pJson) {
			pJson.addProperty("type", pTemplate.type.toString());
		}

		@Override
		public Template unpack(AdminShopArgument pArgument) {
			return new Template(pArgument.type);
		}

		public final class Template implements ArgumentTypeInfo.Template<AdminShopArgument> {
			private final StringArgumentType.StringType type;

			public Template(StringArgumentType.StringType type) {
				this.type = type;
			}

			public AdminShopArgument instantiate(CommandBuildContext commandBuildContext) {
				return AdminShopArgument.create(type);
			}

			public ArgumentTypeInfo<AdminShopArgument, ?> type() {
				return AdminShopArgument.Info.this;
			}
		}
	}
}
