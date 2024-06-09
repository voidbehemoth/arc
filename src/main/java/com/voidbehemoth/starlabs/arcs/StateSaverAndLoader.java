package com.voidbehemoth.starlabs.arcs;

import com.voidbehemoth.starlabs.arcs.util.PlayerData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {
    public LinkedList<ItemStack> arcs = new LinkedList<>();
    public HashMap<UUID, PlayerData> players = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        arcs.forEach((ItemStack stack) -> {
            NbtCompound itemNbt = new NbtCompound();
            stack.writeNbt(itemNbt);
            list.add(list.size(), itemNbt);
        });
        nbt.put("arcs", list);
        NbtCompound playersCompound = new NbtCompound();
        players.forEach((UUID uuid, PlayerData data) -> {
            NbtCompound compound = new NbtCompound();
            compound.putInt("arc", data.arc);
            playersCompound.put(uuid.toString(), compound);
        });
        nbt.put("players", playersCompound);
        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        NbtElement element = tag.get("arcs");
        if (element instanceof NbtList list) {
            list.forEach((NbtElement nbt) -> {
                if (!(nbt instanceof NbtCompound compound)) return;
                ItemStack stack = ItemStack.fromNbt(compound);
                stack.setSubNbt("arc", NbtByte.ZERO);
                state.arcs.add(stack);
            });
        }
        NbtCompound compound = tag.getCompound("players");
        if (compound != null) {
            compound.getKeys().forEach(key ->  {
                PlayerData data = new PlayerData();
                data.arc = compound.getCompound(key).getInt("arc");

                UUID uuid = UUID.fromString(key);
                state.players.put(uuid, data);
            });
        }
        return state;
    }

    private static final Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new,
            StateSaverAndLoader::createFromNbt,
            null
    );

    public static StateSaverAndLoader getServerState() {
        if (ArcSMP.SERVER == null) {
            return null;
        }

        PersistentStateManager stateManager = ArcSMP.SERVER.getOverworld().getPersistentStateManager();

        return stateManager.getOrCreate(type, ArcSMP.MOD_ID);
    }

    public static PlayerData getPlayerState(UUID uuid) {
        StateSaverAndLoader state = getServerState();

        if (state == null) {
            ArcSMP.LOGGER.error("Server is not initialized, somehow.");
            return new PlayerData();
        }

        return state.players.computeIfAbsent(uuid, id -> new PlayerData());
    }
}
