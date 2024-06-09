package com.voidbehemoth.starlabs.arcs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

public class CustomChestMenuScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PlayerInventory playerInventory;

    public CustomChestMenuScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ScreenHandlerType.GENERIC_9X6, syncId);
        checkSize(inventory, 54);
        this.inventory = inventory;
        this.playerInventory = playerInventory;
        int rows = 6;
        inventory.onOpen(playerInventory.player);
        int i = 36;

        int j;
        int k;
        for(j = 0; j < rows; ++j) {
            for(k = 0; k < 9; ++k) {
                this.addSlot(new Slot(inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        for(j = 0; j < 3; ++j) {
            for(k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i));
            }
        }

        for(j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 161 + i));
        }

    }

    @Override
    public void setCursorStack(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (!stack.isEmpty() && nbt != null && nbt.getBoolean("arc")) {
            ItemStack offhand = playerInventory.offHand.get(0);
            if (!offhand.isEmpty()) {
                nbt = offhand.getNbt();
                if (nbt != null && nbt.getBoolean("arc")) {
                    playerInventory.offHand.set(0, ItemStack.EMPTY);

                }
            }
            DefaultedList<ItemStack> main = playerInventory.main;
            ItemStack stack1;
            for(int i = 0; i < main.size(); i++) {
                stack1 = main.get(i);
                nbt = stack1.getNbt();
                if (nbt != null && nbt.getBoolean("arc")) {
                    playerInventory.main.set(i, ItemStack.EMPTY);
                }
            }
        }
        super.setCursorStack(stack);
    }

    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }
}
