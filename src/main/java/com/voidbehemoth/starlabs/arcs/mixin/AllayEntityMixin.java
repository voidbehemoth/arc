package com.voidbehemoth.starlabs.arcs.mixin;

import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AllayEntity.class)
public class AllayEntityMixin {
    @Inject(method = "interactMob", at = @At(value = "HEAD"), cancellable = true)
    private void preventGiveArc(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.getBoolean("arc")) cir.setReturnValue(ActionResult.PASS);
    }
}
