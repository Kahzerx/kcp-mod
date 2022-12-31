package com.kahzerx.kcp.mixins.server;

import io.jpower.kcp.netty.UkcpChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoop;
import net.minecraft.network.LegacyQueryHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LegacyQueryHandler.class)
public class LegacyQueryHandlerMixin extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel() instanceof UkcpChannel kcpChannel) {
            System.out.println("KCP channel detected! setting conv to 10!");
            kcpChannel.conv(10);
        }
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("===EXCPT===");
        System.out.println(cause);
        System.out.println("===EXCPT===");
    }
}
