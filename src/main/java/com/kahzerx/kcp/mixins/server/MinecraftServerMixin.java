package com.kahzerx.kcp.mixins.server;

import com.kahzerx.kcp.kcp.KCPExecutor;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "shutdown", at = @At("TAIL"))
    private void onShutdown(final CallbackInfo ci) {
        KCPExecutor.stop();
    }
}
