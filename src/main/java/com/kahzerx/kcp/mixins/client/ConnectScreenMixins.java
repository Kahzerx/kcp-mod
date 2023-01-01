package com.kahzerx.kcp.mixins.client;

import com.kahzerx.kcp.protocol.ServerInfoInterface;
import com.kahzerx.kcp.protocol.KCPClientConnection;
import com.kahzerx.kcp.protocol.Protocols;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

import static com.kahzerx.kcp.protocol.KCPClientConnection.actualProtocol;


public class ConnectScreenMixins {
    @Mixin(ConnectScreen.class)
    public static class ConnectScreenMixin {
        @Inject(method = "connect(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;)V", at = @At(value = "HEAD"))
        private static void captureServerInfo(Screen screen, MinecraftClient client, ServerAddress address, ServerInfo info, CallbackInfo ci) {
            actualProtocol = ((ServerInfoInterface) info).getProtocol();
        }
    }

    @Mixin(targets = {"net/minecraft/client/gui/screen/ConnectScreen$1"})
    public static class ConnectScreenThreadMixin {
        @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/ClientConnection;"))
        private ClientConnection onClientConnection(InetSocketAddress address, boolean useEpoll) {
            ClientConnection connection;
            if (actualProtocol == Protocols.KCP) {
                connection = KCPClientConnection.connect(address);
            } else {
                connection = ClientConnection.connect(address, useEpoll);
            }
            actualProtocol = null;
            return connection;
        }
    }
}
