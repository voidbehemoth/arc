package com.voidbehemoth.starlabs.arcs.mixin;

import com.voidbehemoth.starlabs.arcs.CustomChestMenuScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Inject(method = "internalOnSlotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 2), cancellable = true)
    private void injectEmpty(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (((ScreenHandler)(Object)this) instanceof CustomChestMenuScreenHandler) {
            if (slotIndex <= 53) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "internalOnSlotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;", ordinal = 2), cancellable = true)
    private void injectGet(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (((ScreenHandler)(Object)this) instanceof CustomChestMenuScreenHandler) {
            if (!((ScreenHandler)(Object)this).getCursorStack().isEmpty() && slotIndex <= 53)
                ci.cancel();
        }
    }

    @Inject(method = "internalOnSlotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getStack(I)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void injected(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (((ScreenHandler)(Object)this) instanceof CustomChestMenuScreenHandler) {
            if (slotIndex <= 53) {
                ci.cancel();
            }
        }
    }
}
