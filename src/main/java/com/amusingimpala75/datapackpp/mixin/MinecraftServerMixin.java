package com.amusingimpala75.datapackpp.mixin;

import com.amusingimpala75.datapackpp.ServerUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "runServer", at=@At("HEAD"))
    public void dpp$inject$runServer$setCurrentServer(CallbackInfo ci) {
        ServerUtil.setCurrentServer((MinecraftServer) (Object) this);
    }

    @Inject(method = "runServer", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;exit()V"))
    public void dpp$inject$runServer$clearCurrentServer(CallbackInfo ci) {
        ServerUtil.setCurrentServer(null);
    }
}
