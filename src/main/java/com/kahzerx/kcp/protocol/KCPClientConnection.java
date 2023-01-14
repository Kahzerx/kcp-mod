package com.kahzerx.kcp.protocol;

import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpClientChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.*;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

import static net.minecraft.network.ClientConnection.CLIENT_IO_GROUP;

public class KCPClientConnection {
    public static Protocols actualProtocol;

    public static ClientConnection connect(InetSocketAddress address) {
        ClientConnection clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);
        Bootstrap kcpClient = new Bootstrap();
        kcpClient = ChannelOptionHelper.nodelay(kcpClient, true, 10, 2, true).
                option(UkcpChannelOption.UKCP_MTU, 2048).
                option(UkcpChannelOption.UKCP_AUTO_SET_CONV, true);
        kcpClient.group(CLIENT_IO_GROUP.get()).
                channel(UkcpClientChannel.class).
                handler(new ChannelInitializer<UkcpChannel>() {
                    @Override
                    protected void initChannel(@NotNull UkcpChannel channel) {
                        channel.config().setOption(UkcpChannelOption.UKCP_NODELAY, true);
                        channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).
                                addLast("splitter", new SplitterHandler()).
                                addLast("decoder", new DecoderHandler(NetworkSide.CLIENTBOUND)).
                                addLast("prepender", new SizePrepender()).
                                addLast("encoder", new PacketEncoder(NetworkSide.SERVERBOUND)).
                                addLast("packet_handler", clientConnection);
                    }
                });
        kcpClient.connect(address.getAddress(), address.getPort()).syncUninterruptibly();
        return clientConnection;
    }
}
