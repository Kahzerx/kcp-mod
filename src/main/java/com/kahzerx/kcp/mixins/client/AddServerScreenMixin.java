package com.kahzerx.kcp.mixins.client;

import com.kahzerx.kcp.helpers.ServerInfoInterface;
import com.kahzerx.kcp.protocol.Protocols;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AddServerScreen.class)
public class AddServerScreenMixin extends Screen {
    @Final @Shadow private ServerInfo server;
    private ButtonWidget proto;

    protected AddServerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setText(Ljava/lang/String;)V", ordinal = 1))
    private void drawProtocolSelect(CallbackInfo ci) {
        Protocols p = ((ServerInfoInterface) server).getProtocol();
        this.proto = this.addDrawableChild(ButtonWidget.builder(Text.literal(p.toString()), button -> this.toggle()).dimensions(this.width / 2 - 100, this.height / 4 + 92, 200, 20).build());
    }

    private void toggle() {
        if (this.proto.getMessage().getString().equals(Protocols.TCP.toString())) {
            this.proto.setMessage(Text.literal(Protocols.KCP.toString()));
        } else {
            this.proto.setMessage(Text.literal(Protocols.TCP.toString()));
        }
    }

    @Inject(method = "addAndClose", at = @At(value = "HEAD"))
    private void onClose(CallbackInfo ci) {
        if (this.proto.getMessage().getString().equals(Protocols.TCP.toString())) {
            ((ServerInfoInterface) server).setProtocol(Protocols.TCP);
        } else {
            ((ServerInfoInterface) server).setProtocol(Protocols.KCP);
        }
    }
}
