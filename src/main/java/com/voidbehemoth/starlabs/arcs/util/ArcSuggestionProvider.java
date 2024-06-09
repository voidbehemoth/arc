package com.voidbehemoth.starlabs.arcs.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.voidbehemoth.starlabs.arcs.StateSaverAndLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class ArcSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    public static final ArcSuggestionProvider INSTANCE;

    static {
        INSTANCE = new ArcSuggestionProvider();
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        StateSaverAndLoader state = StateSaverAndLoader.getServerState();

        if (state == null) {
            return builder.buildFuture();
        }

        for (ItemStack arc : state.arcs) {
            String name = arc.getName().getLiteralString();
            if (name != null && CommandSource.shouldSuggest(builder.getRemaining(), name)) {
                builder.suggest(name);
            }
        }

        return builder.buildFuture();
    }
}