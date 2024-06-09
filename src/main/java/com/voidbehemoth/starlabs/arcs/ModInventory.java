package com.voidbehemoth.starlabs.arcs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.text.Text;

import java.util.*;

public class ModInventory implements Inventory, RecipeInputProvider {
    private int page;

    public ModInventory() {
        page = 0;
    }

    public ItemStack getStack(int slot) {
        ItemStack stack;
        StateSaverAndLoader state = StateSaverAndLoader.getServerState();
        if (state == null) {
            ArcSMP.LOGGER.error("Server is not initialized, somehow.");
            return ItemStack.EMPTY;
        }
        if (slot == 47 && page != 0) { // back
            stack = new ItemStack(Items.RED_STAINED_GLASS_PANE);
            stack.setCustomName(Text.of("Previous Page"));
            return stack;
        } else if (slot == 49) {
            stack = new ItemStack(Items.PAPER);
            stack.setCustomName(Text.of("Page " + (page + 1) + "/" + (state.arcs.size() / 27 + 1)));
            return stack;
        } else if (slot == 51 && (page != state.arcs.size() / 27)) { // forward
            stack = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
            stack.setCustomName(Text.of("Next Page"));
            return stack;
        } else if ((slot <= 9) || (slot % 9 == 0) || (slot - 8) % 9 == 0 || (slot >= 45)) { // border
            return new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        } else {
            int index = slot - 9 - (slot / 9) - ((slot - 8) / 9) + (27 * page);
            if (index > -1 && index < state.arcs.size()) {
                return state.arcs.get(index);
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack removeStack(int slot, int amount) {
        return removeStack(slot);
    }

    public ItemStack removeStack(int slot) {
        StateSaverAndLoader state = StateSaverAndLoader.getServerState();
        if (state == null) {
            return ItemStack.EMPTY;
        }
        if (slot == 47) { // back
            if (page != 0) page--;
        } else if (slot == 51) { // forward
            if (page < state.arcs.size() / 27) page++;
        } else {
            int index = slot - 9 - (slot / 9) - ((slot - 8) / 9) + (27 * page);
            if (index > -1 && index < state.arcs.size()) {
                ItemStack stack = state.arcs.get(index).copy();
                stack.setSubNbt("arc", NbtByte.ONE);
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public void setStack(int slot, ItemStack stack) {

    }

    public int size() {
        return 54;
    }

    public boolean isEmpty() {
        return false;
    }

    public void markDirty() {

    }

    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    public void clear() {
    }

    public String toString() {
        return "inventory";
    }


    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return false;
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {

    }
}
