package com.kahzerx.kcp.mixins.client;

import com.kahzerx.kcp.protocol.ServerInfoInterface;
import com.kahzerx.kcp.protocol.Protocols;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin {
    @Shadow protected MultiplayerServerListWidget serverListWidget;

    @Shadow private ServerInfo selectedEntry;

    @Redirect(method = "method_19915", at = @At(value = "NEW", target = "net/minecraft/client/network/ServerInfo"))
    private ServerInfo onNewEditServerInfo(String name, String address, boolean local) {
        MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
        ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry)entry).getServer();

        ServerInfo server = new ServerInfo(name, address, local);
        ((ServerInfoInterface) server).setProtocol(((ServerInfoInterface) serverInfo).getProtocol());
        return server;
    }

    @Redirect(method = "editEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerServerListWidget$ServerEntry;getServer()Lnet/minecraft/client/network/ServerInfo;"))
    private ServerInfo onEdited(MultiplayerServerListWidget.ServerEntry instance) {
        ServerInfo serverInfo = instance.getServer();
        Protocols protocol = ((ServerInfoInterface) selectedEntry).getProtocol();
        ((ServerInfoInterface) serverInfo).setProtocol(protocol);
        return serverInfo;
    }
}
