package com.kahzerx.kcp.mixins.client;

import com.kahzerx.kcp.kcp.KCPExecutor;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class DisconnectedScreenMixin {
    @Inject(method = "init", at = @At(value = "RETURN"))
    private void onDisconnected(final CallbackInfo ci) {
        KCPExecutor.stop();
    }
}
