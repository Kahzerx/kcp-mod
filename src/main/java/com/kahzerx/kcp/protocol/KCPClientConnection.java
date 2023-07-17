package com.kahzerx.kcp.protocol;

import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpClientChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.*;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

public class KCPClientConnection {
    public static Protocols actualProtocol;

    public static ClientConnection connectClient(InetSocketAddress address) {
        ClientConnection clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);
        ChannelFuture channelFuture = connectCF(address, clientConnection);
        channelFuture.syncUninterruptibly();
        return clientConnection;
    }

    public static ChannelFuture connectCF(InetSocketAddress address, ClientConnection clientConnection) {
        Bootstrap kcpClient = new Bootstrap();
        kcpClient = ChannelOptionHelper.nodelay(kcpClient, true, 10, 2, true).
                option(UkcpChannelOption.UKCP_AUTO_SET_CONV, true);
        kcpClient.group(new NioEventLoopGroup()).
                channel(UkcpClientChannel.class).
                handler(new ChannelInitializer<UkcpChannel>() {
                    @Override
                    protected void initChannel(@NotNull UkcpChannel channel) {
                        channel.config().setOption(UkcpChannelOption.UKCP_NODELAY, true);
                        channel.pipeline().
                                addLast("timeout", new ReadTimeoutHandler(30)).
                                addLast("splitter", new SplitterHandler()).
                                addLast("decoder", new DecoderHandler(NetworkSide.CLIENTBOUND)).
                                addLast("prepender", new SizePrepender()).
                                addLast("encoder", new PacketEncoder(NetworkSide.SERVERBOUND)).
                                addLast("unbundler", new PacketUnbundler(NetworkSide.SERVERBOUND)).
                                addLast("bundler", new PacketBundler(NetworkSide.CLIENTBOUND)).
                                addLast("packet_handler", clientConnection);
                    }
                });
        return kcpClient.connect(address.getAddress(), address.getPort());
    }
}
