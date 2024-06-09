package com.voidbehemoth.starlabs.arcs.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.voidbehemoth.starlabs.arcs.ModInventory;
import com.voidbehemoth.starlabs.arcs.ModPlayer;
import com.voidbehemoth.starlabs.arcs.StateSaverAndLoader;
import com.voidbehemoth.starlabs.arcs.util.ArcSuggestionProvider;
import com.voidbehemoth.starlabs.arcs.util.PlayerData;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

public class ArcCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                CommandManager.literal("arc")
                        .then(CommandManager.literal("add")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.argument("name", StringArgumentType.string())
                                        .executes(ArcCommand::addNamedArc))
                                .executes(ArcCommand::addArc))
                        .then(CommandManager.literal("remove")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.argument("removed", StringArgumentType.string())
                                        .suggests(ArcSuggestionProvider.INSTANCE)
                                        .executes(ArcCommand::removeArcName))
                                .executes(ArcCommand::removeArc))
                        .then(CommandManager.literal("select")
                                .then(CommandManager.argument("selected", StringArgumentType.string())
                                        .suggests(ArcSuggestionProvider.INSTANCE).executes(ArcCommand::selectNamedArc))
                                .executes(ArcCommand::selectArc))
                        .then(CommandManager.literal("set")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.argument("target", GameProfileArgumentType.gameProfile())
                                        .suggests((context, builder) -> {
                                            PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
                                            return CommandSource.suggestMatching(playerManager.getPlayerList().stream().map((player) -> player.getGameProfile().getName()), builder);
                                        })
                                        .then(CommandManager.literal("none").executes(ArcCommand::setArcOfNone))
                                        .then(CommandManager.argument("name", StringArgumentType.string())
                                                .suggests(ArcSuggestionProvider.INSTANCE)
                                                .executes(ArcCommand::setArcOf))))
                        .then(CommandManager.literal("get")
                                .requires(source -> source.hasPermissionLevel(2)).then(CommandManager.argument("target", GameProfileArgumentType.gameProfile())
                                        .suggests((context, builder) -> {
                                            PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
                                            return CommandSource.suggestMatching(playerManager.getPlayerList().stream().filter((player) -> {
                                                PlayerData data = StateSaverAndLoader.getPlayerState(player.getGameProfile().getId());
                                                return data.arc != -1;
                                            }).map((player) -> player.getGameProfile().getName()), builder);
                                        })
                                        .executes(ArcCommand::getArcOf)))
        );
        dispatcher.register(
                CommandManager.literal("arcs").executes(ArcCommand::openBoard)
        );
    }

    private static int setArcOfNone(CommandContext<ServerCommandSource> ctx) {
        GameProfile profile;
        try {
            Optional<GameProfile> opProfile = GameProfileArgumentType.getProfileArgument(ctx, "target").stream().findFirst();
            if (opProfile.isPresent()) {
                profile = opProfile.get();
            } else {
                return 0;
            }
        } catch (Exception ex) {
            return 0;
        }


        PlayerData data = StateSaverAndLoader.getPlayerState(profile.getId());

        if (data.arc != -1) {
            data.arc = -1;
            StateSaverAndLoader state = StateSaverAndLoader.getServerState();
            if (state == null) {
                return 0;
            }
            state.markDirty();
            ctx.getSource().sendFeedback(() -> Text.of("Removed " + profile.getName() + "'s current arc."), true);
        } else {
            ctx.getSource().sendFeedback(() -> Text.of(profile.getName() + " has no current arc."), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setArcOf(CommandContext<ServerCommandSource> ctx) {

        String name = StringArgumentType.getString(ctx, "name");
        ServerCommandSource source = ctx.getSource();

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        StateSaverAndLoader state = StateSaverAndLoader.getServerState();
        if (state == null) {
            return 0;
        }

        Integer toSet = null;
        for(int i = 0; i < state.arcs.size(); i++) {
            if (name.equals(state.arcs.get(i).getName().getLiteralString())) {
                toSet = i;
            }
        }

        if (toSet == null) {
            source.sendFeedback(() -> Text.of(name + " does not exist."), false);
            return Command.SINGLE_SUCCESS;
        }

        GameProfile profile;
        try {
            Optional<GameProfile> opProfile = GameProfileArgumentType.getProfileArgument(ctx, "target").stream().findFirst();
            if (opProfile.isPresent()) {
                profile = opProfile.get();
            } else {
                return 0;
            }
        } catch (Exception ex) {
            return 0;
        }

        PlayerData data = StateSaverAndLoader.getPlayerState(profile.getId());

        if (data.arc != toSet) {
            data.arc = toSet;
            state.markDirty();
            ctx.getSource().sendFeedback(() -> Text.of("Set " + profile.getName() + "'s current arc to " + state.arcs.get(data.arc).getName().getLiteralString() + "."), true);
        } else {
            final String s = state.arcs.get(toSet).getName().getLiteralString();
            ctx.getSource().sendFeedback(() -> Text.of(profile.getName() + "'s arc is already " + s + "."), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int getArcOf(CommandContext<ServerCommandSource> ctx) {
        GameProfile profile;
        try {
            Optional<GameProfile> opProfile = GameProfileArgumentType.getProfileArgument(ctx, "target").stream().findFirst();
            if (opProfile.isPresent()) {
                profile = opProfile.get();
            } else {
                return 0;
            }
        } catch (Exception ex) {
            return 0;
        }

        PlayerData data = StateSaverAndLoader.getPlayerState(profile.getId());

        if (data.arc == -1) {
            ctx.getSource().sendFeedback(() -> Text.of(profile.getName() + " has no current arc."), false);
        } else {
            StateSaverAndLoader state = StateSaverAndLoader.getServerState();
            if (state == null) {
                return 0;
            }
            ItemStack stack = state.arcs.get(data.arc);
            ctx.getSource().sendFeedback(() -> Text.of(profile.getName() + " is currently on " + stack.getName().getLiteralString()), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int selectArc(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        PlayerData data = StateSaverAndLoader.getPlayerState(player.getGameProfile().getId());

        if (data.arc != -1) {
            source.sendFeedback(() -> Text.of("Your arc is already selected. Please contact a Technician if you wish to change it."), false);
            return Command.SINGLE_SUCCESS;
        }

        StateSaverAndLoader state = StateSaverAndLoader.getServerState();
        if (state == null) {
            return 0;
        }

        ItemStack hand = player.getMainHandStack().copy();
        NbtCompound nbt = hand.getNbt();
        if (!(nbt != null && nbt.getBoolean("arc"))) {
            source.sendFeedback(() -> Text.of("Item held in hand is not an Arc"), true);
            return Command.SINGLE_SUCCESS;
        }

        String name = hand.getName().getLiteralString();
        if (name == null) {
            return 0;
        }
        Integer toSet = null;
        for(int i = 0; i < state.arcs.size(); i++) {
            if (name.equals(state.arcs.get(i).getName().getLiteralString())) {
                toSet = i;
            }
        }

        if (toSet == null) {
            source.sendFeedback(() -> Text.of(name + " does not exist."), false);
            return Command.SINGLE_SUCCESS;
        }

        data.arc = toSet;
        state.markDirty();
        source.sendFeedback(() -> Text.of("Arc successfully selected!"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int selectNamedArc(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        PlayerData data = StateSaverAndLoader.getPlayerState(player.getGameProfile().getId());

        if (data.arc != -1) {
            source.sendFeedback(() -> Text.of("Your arc is already selected. Please contact a Technician if you wish to change it."), false);
            return Command.SINGLE_SUCCESS;
        }

        String name = StringArgumentType.getString(ctx, "name");

        StateSaverAndLoader state = StateSaverAndLoader.getServerState();
        if (state == null) {
            return 0;
        }

        Integer toSet = null;
        for(int i = 0; i < state.arcs.size(); i++) {
            if (name.equals(state.arcs.get(i).getName().getLiteralString())) {
                toSet = i;
            }
        }

        if (toSet == null) {
            source.sendFeedback(() -> Text.of(name + " does not exist."), false);
            return Command.SINGLE_SUCCESS;
        }

        data.arc = toSet;
        state.markDirty();
        source.sendFeedback(() -> Text.of("Arc successfully selected!"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int openBoard(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();

        ModInventory inv = new ModInventory();

        assert player != null;
        ModPlayer modPlayer = new ModPlayer(player, inv);

        player.openHandledScreen(modPlayer);

        return Command.SINGLE_SUCCESS;
    }

    private static int addNamedArc(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        ItemStack hand = player.getMainHandStack().copy();

        StateSaverAndLoader state = StateSaverAndLoader.getServerState();
        if (state == null) {
            return 0;
        }

        NbtCompound nbt = hand.getNbt();
        if ((nbt != null && nbt.getBoolean("arc")) || state.arcs.contains(hand)) {
            source.sendFeedback(() -> Text.of("Item is already an arc"), true);
            return Command.SINGLE_SUCCESS;
        }

        if (!hand.isOf(Items.WRITTEN_BOOK)) {
            source.sendFeedback(() -> Text.of("Item is not a written book"), true);
            return Command.SINGLE_SUCCESS;
        }

        hand.setCustomName(Text.of(StringArgumentType.getString(ctx, "name")));
        hand.setCount(1);
        hand.setSubNbt("arc", NbtByte.ZERO);

        state.arcs.add(hand);
        state.markDirty();

        source.sendFeedback(() -> Text.of("Successfully added arc " + hand.getName().getLiteralString()), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int addArc(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        ItemStack hand = player.getMainHandStack().copy();

        StateSaverAndLoader state = StateSaverAndLoader.getServerState();

        NbtCompound nbt = hand.getNbt();
        if ((nbt != null && nbt.getBoolean("arc")) || state.arcs.contains(hand)) {
            source.sendFeedback(() -> Text.of("Item is already an arc"), true);
            return Command.SINGLE_SUCCESS;
        }

        if (!hand.isOf(Items.WRITTEN_BOOK)) {
            source.sendFeedback(() -> Text.of("Item is not a written book"), true);
            return Command.SINGLE_SUCCESS;
        }

        hand.setCount(1);
        hand.setCustomName(hand.getName());
        hand.setSubNbt("arc", NbtByte.ZERO);

        state.arcs.add(hand);
        state.markDirty();

        source.sendFeedback(() -> Text.of("Successfully added arc " + hand.getName().getLiteralString()), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int removeArcName(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "removed");
        ServerCommandSource source = ctx.getSource();

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        StateSaverAndLoader state = StateSaverAndLoader.getServerState();
        if (state == null) {
            return 0;
        }

        int toRemove = -1;
        for(int i = 0; i < state.arcs.size(); i++) {
            if (name.equals(state.arcs.get(i).getName().getLiteralString())) {
                toRemove = i;
            }
        }

        if (toRemove == -1) {
            source.sendFeedback(() -> Text.of(name + " does not exist."), false);
            return Command.SINGLE_SUCCESS;
        }

        ItemStack removed = state.arcs.remove(toRemove);

        final int n = toRemove;
        state.players.forEach((UUID uuid, PlayerData data) -> {
            if (data.arc == n) {
                data.arc = -1;
            }
        });
        state.markDirty();

        source.sendFeedback(() -> Text.of("Successfully removed arc " + removed.getName().getLiteralString() + "."), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int removeArc(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;


        StateSaverAndLoader state = StateSaverAndLoader.getServerState();
        if (state == null) {
            return 0;
        }

        ItemStack hand = player.getMainHandStack().copy();
        NbtCompound nbt = hand.getNbt();
        if (!(nbt != null && nbt.getBoolean("arc"))) {
            ctx.getSource().sendFeedback(() -> Text.of("Item held in hand is not an Arc"), true);
            return Command.SINGLE_SUCCESS;
        }
        String name = hand.getName().getLiteralString();
        if (name == null) {
            return 0;
        }
        int toRemove = -1;
        for(int i = 0; i < state.arcs.size(); i++) {
            if (name.equals(state.arcs.get(i).getName().getLiteralString())) {
                toRemove = i;
            }
        }

        if (toRemove == -1) {
            source.sendFeedback(() -> Text.of(name + " does not exist."), false);
            return Command.SINGLE_SUCCESS;
        }

        ItemStack removed = state.arcs.remove(toRemove);

        final int n = toRemove;
        state.players.forEach((UUID uuid, PlayerData data) -> {
            if (data.arc == n) {
                data.arc = -1;
            }
        });
        state.markDirty();

        source.sendFeedback(() -> Text.of("Successfully removed arc " + removed.getName().getLiteralString() + "."), true);

        return Command.SINGLE_SUCCESS;
    }
}
