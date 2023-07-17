package com.kahzerx.kcp.mixins.client;

import com.kahzerx.kcp.protocol.ServerInfoInterface;
import com.kahzerx.kcp.protocol.Protocols;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerInfo.class)
public class ServerInfoMixin implements ServerInfoInterface {
    private Protocols protocol;
    @Override
    public Protocols getProtocol() {
        if (this.protocol == null) {
            return Protocols.TCP;
        }
        return this.protocol;
    }

    @Override
    public void setProtocol(Protocols protocol) {
        this.protocol = protocol;
    }

    @Redirect(method = "toNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;putString(Ljava/lang/String;Ljava/lang/String;)V", ordinal = 0))
    private void setProto(NbtCompound instance, String key, String value) {
        instance.putString("protocol", this.protocol.toString());
        instance.putString(key, value);
    }

    @Redirect(method = "fromNbt", at = @At(value = "NEW", target = "(Ljava/lang/String;Ljava/lang/String;Z)Lnet/minecraft/client/network/ServerInfo;"))
    private static ServerInfo onNew(String name, String address, boolean local, NbtCompound root) {
        Protocols p = Protocols.TCP;
        if (root.contains("protocol")) {
            String proto = root.getString("protocol");
            if (proto.equalsIgnoreCase("kcp")) {
                p = Protocols.KCP;
            }
        }
        ServerInfo server = new ServerInfo(name, address, local);
        ((ServerInfoInterface) server).setProtocol(p);
        return server;
    }
}
