package com.voidbehemoth.starlabs.arcs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.*;

public class ModInventory implements Inventory, RecipeInputProvider {
    private final int size = 54;
    private int page;

    public ModInventory() {
//        this.size = items.length;
//        this.stacks = DefaultedList.copyOf(ItemStack.EMPTY, items);
        page = 0;
    }

    public ItemStack getStack(int slot) {
        ItemStack stack;
        StateSaverAndLoader state = StateSaverAndLoader.getServerState(Objects.requireNonNull(MinecraftClient.getInstance().getServer()));
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

    public List<ItemStack> clearToList() {
        return new ArrayList<ItemStack>();
    }

    public ItemStack removeStack(int slot, int amount) {
        return removeStack(slot);
    }

    public ItemStack removeItem(Item item, int count) {
        return ItemStack.EMPTY;
    }


    public boolean canInsert(ItemStack stack) {
        return false;
    }

    public ItemStack removeStack(int slot) {
        StateSaverAndLoader state = StateSaverAndLoader.getServerState(Objects.requireNonNull(MinecraftClient.getInstance().getServer()));
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
        return this.size;
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
        return "hehe";
    }

    private void transfer(ItemStack source, ItemStack target) {

    }


    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return false;
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {

    }
}
