package com.voidbehemoth.starlabs.arcs;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.LinkedList;

public class StateSaverAndLoader extends PersistentState {
    public LinkedList<ItemStack> arcs = new LinkedList<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        arcs.forEach((ItemStack stack) -> {
            NbtCompound itemNbt = new NbtCompound();
            stack.writeNbt(itemNbt);
            ArcSMP.LOGGER.info("writing");
            list.add(list.size(), itemNbt);
        });
        nbt.put("arcs", list);
        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag) {
        ArcSMP.LOGGER.info("test");
        StateSaverAndLoader state = new StateSaverAndLoader();
        NbtElement element = tag.get("arcs");
        if (element instanceof NbtList list) {
            ArcSMP.LOGGER.info("hi");
            list.forEach((NbtElement nbt) -> {
                ArcSMP.LOGGER.info("hello");
                if (!(nbt instanceof NbtCompound compound)) return;
                ItemStack stack = ItemStack.fromNbt(compound);
                stack.setSubNbt("arc", NbtByte.ZERO);
                ArcSMP.LOGGER.info("reading");
                state.arcs.add(stack);
            });
        }
        return state;
    }

    private static final Type<StateSaverAndLoader> type = new Type<StateSaverAndLoader>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();

        StateSaverAndLoader state = stateManager.getOrCreate(type, ArcSMP.MOD_ID);

        return state;
    }
}
