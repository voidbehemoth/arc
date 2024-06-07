package com.voidbehemoth.starlabs.arcs.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Generic3x3ContainerScreenHandler.class)
public class Generic3x3ContainerScreenHandlerMixin {
    @Redirect(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;hasStack()Z"))
    private boolean redirect(Slot instance) {
        if (instance.hasStack()) {
            ItemStack stack = instance.getStack();
            NbtCompound nbt = stack.getNbt();
            if (nbt != null) {
                return !nbt.getBoolean("arc");
            }
            return true;
        }
        return false;
    }
}
