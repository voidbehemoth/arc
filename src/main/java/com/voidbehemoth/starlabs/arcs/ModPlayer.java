package com.voidbehemoth.starlabs.arcs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ModPlayer implements NamedScreenHandlerFactory {
    private final Inventory guiInventory;

    public ModPlayer(ServerPlayerEntity player, Inventory inventory) {
        this.guiInventory = inventory;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity player) {
        return new CustomChestMenuScreenHandler(i, playerInventory, guiInventory);
    }

    public Text getDisplayName() {
        return Text.of("Available Arcs");
    }
}
