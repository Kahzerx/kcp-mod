package com.kahzerx.kcp.mixins.server;


import com.kahzerx.kcp.KCPMod;
import com.kahzerx.kcp.config.KCPServerConfig;
import com.kahzerx.kcp.kcp.KCPDownloader;
import com.kahzerx.kcp.kcp.KCPExecutor;
import net.minecraft.server.ServerNetworkIo;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;

@Mixin(ServerNetworkIo.class)
public abstract class ServerNetworkIOMixin {
    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "bind", at = @At(value = "HEAD"))
    private void onBind(InetAddress address, int port, CallbackInfo ci) {
        if (!KCPMod.config.enabled()) {
            return;
        }
        new KCPServerConfig().createKCPConfig(port);
        boolean downloaded = new KCPDownloader().downloadServer();
        if (!downloaded) {
            return;
        }
        LOGGER.info("Starting KCP listener on port {}", KCPMod.config.port());
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void onStop(CallbackInfo ci) {
        LOGGER.info("Stopping KCP listener on port {}", KCPMod.config.port());
        KCPExecutor.stop();
    }
}
