package com.voidbehemoth.starlabs.arcs.mixin;

import com.voidbehemoth.starlabs.arcs.ArcSMP;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "startServer", at = @At("TAIL"))
    private static <S extends MinecraftServer> void injected(Function<Thread, S> serverFactory, CallbackInfoReturnable<S> cir) {
        ArcSMP.SERVER = cir.getReturnValue();
    }
}
