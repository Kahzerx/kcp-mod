package com.kahzerx.kcp.mixins.server;


import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpServerChannel;
import io.netty.bootstrap.UkcpServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.*;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import net.minecraft.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.util.List;

@Mixin(ServerNetworkIo.class)
public abstract class ServerNetworkIOMixin {
    @Shadow @Final private List<ChannelFuture> channels;

    @Shadow @Final public static Lazy<NioEventLoopGroup> DEFAULT_CHANNEL;

    @Inject(method = "bind", at = @At(value = "HEAD"))
    private void onBind(InetAddress address, int port, CallbackInfo ci) {
        List<ChannelFuture> list = this.channels;
        int PORT = 25566;
        synchronized (list) {
            UkcpServerBootstrap kcpServer = new UkcpServerBootstrap();
            ServerNetworkIo networkIo = (ServerNetworkIo) (Object) this;
            kcpServer.group(DEFAULT_CHANNEL.get()).
                    channel(UkcpServerChannel.class).
                    childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(@NotNull Channel channel) {
                            channel.config().setOption(UkcpChannelOption.UKCP_NODELAY, true);
                            channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).
                                    addLast("legacy_query", new LegacyQueryHandler(networkIo)).
                                    addLast("splitter", new SplitterHandler()).
                                    addLast("decoder", new DecoderHandler(NetworkSide.SERVERBOUND)).
                                    addLast("prepender", new SizePrepender()).
                                    addLast("encoder", new PacketEncoder(NetworkSide.CLIENTBOUND));
                            int i = networkIo.getServer().getRateLimit();
                            ClientConnection clientConnection = i > 0 ? new RateLimitedConnection(i) : new ClientConnection(NetworkSide.SERVERBOUND);
                            networkIo.getConnections().add(clientConnection);
                            channel.pipeline().addLast("packet_handler", clientConnection);
                            clientConnection.setPacketListener(new ServerHandshakeNetworkHandler(networkIo.getServer(), clientConnection));
                        }
                    });
            ChannelOptionHelper.nodelay(kcpServer, true, 20, 3, true).
                    childOption(UkcpChannelOption.UKCP_MTU, 512).
                    childOption(UkcpChannelOption.UKCP_AUTO_SET_CONV, true);
            ChannelFuture f = kcpServer.localAddress(address, PORT).bind().syncUninterruptibly();
            this.channels.add(f);
        }
    }
}
