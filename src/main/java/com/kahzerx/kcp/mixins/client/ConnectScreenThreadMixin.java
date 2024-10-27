package com.kahzerx.kcp.mixins.client;

import com.kahzerx.kcp.kcp.KCPExecutor;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(targets = "net/minecraft/client/gui/screen/multiplayer/ConnectScreen$1")
public class ConnectScreenThreadMixin {
    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AllowedAddressResolver;resolve(Lnet/minecraft/client/network/ServerAddress;)Ljava/util/Optional;"))
    private Optional<Address> onResolve(AllowedAddressResolver instance, ServerAddress address) {
        if (KCPExecutor.localClientPort != 0) {
            int port = KCPExecutor.localClientPort;
            KCPExecutor.localClientPort = 0;
            return AllowedAddressResolver.DEFAULT.resolve(new ServerAddress("127.0.0.1", port));
        }
        return AllowedAddressResolver.DEFAULT.resolve(address);
    }
}
