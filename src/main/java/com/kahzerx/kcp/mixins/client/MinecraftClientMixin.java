package com.kahzerx.kcp.mixins.client;

import com.kahzerx.kcp.kcp.KCPExecutor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "run", at = @At(value = "RETURN"))
    private void onClientClose(CallbackInfo ci) {
        KCPExecutor.stop();
    }

    @Inject(method = "printCrashReport(Lnet/minecraft/client/MinecraftClient;Ljava/io/File;Lnet/minecraft/util/crash/CrashReport;)V", at = @At(value = "HEAD"))
    private static void onException(MinecraftClient client, File runDirectory, CrashReport crashReport, CallbackInfo ci) {
        KCPExecutor.stop();
    }
}
