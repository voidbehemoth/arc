package com.voidbehemoth.starlabs.arcs.mixin;

import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFrameEntity.class)
public class ItemFrameEntityMixin {
    @Redirect(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private boolean redirect(ItemStack instance) {
        NbtCompound nbt = instance.getNbt();

        return instance.isEmpty() || (nbt != null && nbt.getBoolean("arc"));
    }
}
