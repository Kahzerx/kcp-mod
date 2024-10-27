package com.kahzerx.kcp.mixins.client;

import com.kahzerx.kcp.kcp.KCPExecutor;
import com.kahzerx.kcp.protocol.Protocols;
import com.kahzerx.kcp.protocol.ServerInfoInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.ServerSocket;


@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    @Inject(method = "connect(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/ConnectScreen;connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;)V"))
    private static void captureServerInfo(Screen screen, MinecraftClient client, ServerAddress address, ServerInfo info, boolean quickPlay, CallbackInfo ci) {
        Protocols actualProtocol = ((ServerInfoInterface) info).getProtocol();
        if (actualProtocol != Protocols.KCP) {
            return;
        }
        KCPExecutor.stop();
        int localPort;
        try {
            ServerSocket sock = new ServerSocket(0);
            localPort = sock.getLocalPort();
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        new KCPExecutor().runClient(address.getAddress(), address.getPort(), localPort);
        KCPExecutor.waitForKCP();
    }
}

