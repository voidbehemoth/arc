package com.voidbehemoth.starlabs.arcs.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Redirect(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private boolean fix(ItemStack instance) {
        NbtCompound nbt = instance.getNbt();

        return instance.isEmpty() || (nbt != null && nbt.getBoolean("arc"));
    }

    @Redirect(method = "vanishCursedItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;hasVanishingCurse(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean vanishFix(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return EnchantmentHelper.hasVanishingCurse(stack) || (nbt != null &&  nbt.getBoolean("arc"));
    }
}
