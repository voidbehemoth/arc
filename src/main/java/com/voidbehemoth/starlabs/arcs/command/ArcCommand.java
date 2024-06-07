package com.voidbehemoth.starlabs.arcs.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.voidbehemoth.starlabs.arcs.ModInventory;
import com.voidbehemoth.starlabs.arcs.ModPlayer;
import com.voidbehemoth.starlabs.arcs.StateSaverAndLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Iterator;
import java.util.Objects;

public class ArcCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                CommandManager.literal("arc")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("name", StringArgumentType.string()).executes(ArcCommand::addNamedArc))
                                .executes(ArcCommand::addArc))
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("removed", StringArgumentType.string()).executes(ArcCommand::removeArcName))
                                .executes(ArcCommand::removeArc))
        );
        dispatcher.register(
                CommandManager.literal("arcs").executes(ArcCommand::openBoard)
        );
    }

    private static int openBoard(CommandContext<ServerCommandSource> ctx) {
        if (ctx.getSource().getWorld().isClient) return Command.SINGLE_SUCCESS;

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
        ServerWorld world = source.getWorld();
        if (world.isClient) return Command.SINGLE_SUCCESS;

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        ItemStack hand = player.getMainHandStack().copy();

        StateSaverAndLoader state = StateSaverAndLoader.getServerState(source.getServer());

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

        source.sendFeedback(() -> Text.of("Successfully added arc " + hand.getName()), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int addArc(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerWorld world = source.getWorld();
        if (world.isClient) return Command.SINGLE_SUCCESS;

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        ItemStack hand = player.getMainHandStack().copy();

        StateSaverAndLoader state = StateSaverAndLoader.getServerState(source.getServer());

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
        hand.setSubNbt("arc", NbtByte.ZERO);

        state.arcs.add(hand);
        state.markDirty();

        source.sendFeedback(() -> Text.of("Successfully added arc " + hand.getName()), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int removeArcName(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "removed");
        ServerCommandSource source = ctx.getSource();
        ServerWorld world = source.getWorld();
        if (world.isClient) return Command.SINGLE_SUCCESS;

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        StateSaverAndLoader state = StateSaverAndLoader.getServerState(source.getServer());

        ItemStack toRemove = null;
        String s = null;
        Iterator<ItemStack> it = state.arcs.descendingIterator();
        while (it.hasNext()) {
            toRemove = it.next();
            s = toRemove.getName().getLiteralString();
            if (s != null && s.equals(name)) break;
        }

        boolean removed = state.arcs.remove(toRemove);
        state.markDirty();

        source.sendFeedback(() -> Text.of(removed ? "Successfully removed arc" : "Arc not present"), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int removeArc(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        ServerWorld world = source.getWorld();
        if (world.isClient) return Command.SINGLE_SUCCESS;

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;


        StateSaverAndLoader state = StateSaverAndLoader.getServerState(source.getServer());

        ItemStack toRemove = null;
        ItemStack hand = player.getMainHandStack().copy();
        NbtCompound nbt = hand.getNbt();
        if (!(nbt != null && nbt.getBoolean("arc"))) {
            ctx.getSource().sendFeedback(() -> Text.of("Item held in hand is not an Arc"), true);
            return Command.SINGLE_SUCCESS;
        }

        Iterator<ItemStack> it = state.arcs.descendingIterator();
        while (it.hasNext()) {
            toRemove = it.next();
            if (toRemove.getName().equals(hand.getName())) break;
        }

        boolean removed = state.arcs.remove(toRemove);
        state.markDirty();

        source.sendFeedback(() -> Text.of(removed ? "Successfully removed arc" : "Arc not present"), true);

        return Command.SINGLE_SUCCESS;
    }
}
