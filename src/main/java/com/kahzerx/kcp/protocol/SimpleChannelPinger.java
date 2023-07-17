package com.kahzerx.kcp.protocol;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SimpleChannelPinger extends SimpleChannelInboundHandler<ByteBuf> {
    private final InetSocketAddress address;
    private final ServerInfo info;

    public SimpleChannelPinger(final InetSocketAddress address, final ServerInfo info) {
        this.address = address;
        this.info = info;
    }
    static final Splitter ZERO_SPLITTER = Splitter.on('\u0000').limit(6);

    @Override
    public void channelActive(@NotNull ChannelHandlerContext context) throws Exception {
        super.channelActive(context);
        ByteBuf byteBuf = Unpooled.buffer();

        try {
            byteBuf.writeByte(254);
            byteBuf.writeByte(1);
            byteBuf.writeByte(250);
            char[] cs = "MC|PingHost".toCharArray();
            byteBuf.writeShort(cs.length);
            char[] var4 = cs;
            int var5 = cs.length;

            int var6;
            char c;
            for(var6 = 0; var6 < var5; ++var6) {
                c = var4[var6];
                byteBuf.writeChar(c);
            }

            byteBuf.writeShort(7 + 2 * address.getHostName().length());
            byteBuf.writeByte(127);
            cs = address.getHostName().toCharArray();
            byteBuf.writeShort(cs.length);
            var4 = cs;
            var5 = cs.length;

            for(var6 = 0; var6 < var5; ++var6) {
                c = var4[var6];
                byteBuf.writeChar(c);
            }

            byteBuf.writeInt(address.getPort());
            context.channel().writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } finally {
            byteBuf.release();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
        short s = byteBuf.readUnsignedByte();
        if (s == 255) {
            String string = new String(byteBuf.readBytes(byteBuf.readShort() * 2).array(), StandardCharsets.UTF_16BE);
            String[] strings = Iterables.toArray(ZERO_SPLITTER.split(string), String.class);
            if ("§1".equals(strings[0])) {
                String string2 = strings[2];
                String string3 = strings[3];
                int j = MathHelper.parseInt(strings[4], -1);
                int k = MathHelper.parseInt(strings[5], -1);
                info.protocolVersion = -1;
                info.version = Text.literal(string2);
                info.label = Text.literal(string3);
                info.playerCountLabel = createPlayerCountText(j, k);
                info.players = new ServerMetadata.Players(k, j, List.of());
            }
        }

        channelHandlerContext.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable throwable) {
        context.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.channelRead0(ctx, (ByteBuf)msg);
    }

    static Text createPlayerCountText(int current, int max) {
        return Text.literal(Integer.toString(current)).append(Text.literal("/").formatted(Formatting.DARK_GRAY)).append(Integer.toString(max)).formatted(Formatting.GRAY);
    }
}
