package com.kahzerx.kcp.mixins.client;

import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpClientChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.*;
import net.minecraft.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetSocketAddress;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Shadow @Final public static Lazy<NioEventLoopGroup> CLIENT_IO_GROUP;

    @Inject(method = "connect", at = @At(value = "HEAD"), cancellable = true)
    private static void onConnect(InetSocketAddress address, boolean useEpoll, CallbackInfoReturnable<ClientConnection> cir) {
        ClientConnection clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);
        Bootstrap kcpClient = new Bootstrap();
        kcpClient.group(CLIENT_IO_GROUP.get()).
                channel(UkcpClientChannel.class).
                handler(new ChannelInitializer<UkcpChannel>() {
                    @Override
                    protected void initChannel(@NotNull UkcpChannel channel) {
                        channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).
                                addLast("splitter", new SplitterHandler()).
                                addLast("decoder", new DecoderHandler(NetworkSide.CLIENTBOUND)).
                                addLast("prepender", new SizePrepender()).
                                addLast("encoder", new PacketEncoder(NetworkSide.SERVERBOUND)).
                                addLast("packet_handler", clientConnection);
                    }
                });
        ChannelOptionHelper.nodelay(kcpClient, true, 20, 3, true).
                option(UkcpChannelOption.UKCP_MTU, 512).
                option(UkcpChannelOption.UKCP_AUTO_SET_CONV, true);
        kcpClient.connect(address.getAddress(), address.getPort()).syncUninterruptibly();
        cir.setReturnValue(clientConnection);
    }
}
