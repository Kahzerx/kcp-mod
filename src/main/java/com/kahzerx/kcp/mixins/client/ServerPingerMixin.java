package com.kahzerx.kcp.mixins.client;

import com.kahzerx.kcp.protocol.Protocols;
import com.kahzerx.kcp.protocol.ServerInfoInterface;
import com.kahzerx.kcp.protocol.SimpleChannelPinger;
import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpClientChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;

@Mixin(MultiplayerServerListPinger.class)
public class ServerPingerMixin {
    @Inject(method = "ping", at = @At(value = "HEAD"), cancellable = true)
    private void onPing(InetSocketAddress address, ServerInfo info, CallbackInfo ci) {
        Protocols p = ((ServerInfoInterface) info).getProtocol();
        if (p == Protocols.KCP) {
            Bootstrap kcpClient = new Bootstrap();
            kcpClient = ChannelOptionHelper.nodelay(kcpClient, true, 10, 2, true).
                    option(UkcpChannelOption.UKCP_AUTO_SET_CONV, true);
            kcpClient.group(ClientConnection.CLIENT_IO_GROUP.get()).
                    channel(UkcpClientChannel.class).
                    handler(new ChannelInitializer<UkcpChannel>() {
                        @Override
                        protected void initChannel(@NotNull UkcpChannel channel) {
                            channel.config().setOption(UkcpChannelOption.UKCP_NODELAY, true);
                            channel.pipeline().addLast(new SimpleChannelPinger(address, info));
                        }
                    });
            kcpClient.connect(address.getAddress(), address.getPort());
            ci.cancel();
        }
    }
}
