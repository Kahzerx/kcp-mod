package com.kahzerx.kcp.mixins.client;

import com.kahzerx.kcp.protocol.KCPClientConnection;
import com.kahzerx.kcp.protocol.Protocols;
import com.kahzerx.kcp.protocol.ServerInfoInterface;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.InetSocketAddress;

@Mixin(MultiplayerServerListPinger.class)
public class MultiplayerServerListPingerMixin {
    @Redirect(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/ClientConnection;"))
    private ClientConnection onServerPing(InetSocketAddress address, boolean useEpoll, final ServerInfo entry, final Runnable saver) {
        Protocols protocol = ((ServerInfoInterface) entry).getProtocol();
        if (protocol == Protocols.KCP) {
            return KCPClientConnection.connectClient(address);
        } else {
            return ClientConnection.connect(address, useEpoll);
        }
    }
}
