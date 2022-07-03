package com.amusingimpala75.datapackpp.mixin.client;

import com.amusingimpala75.datapackpp.client.DatapackppClient;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldListWidget.WorldEntry.class)
public class WorldEntryMixin {
    @Shadow @Final private LevelSummary level;

    @Inject(method = "start", at=@At("HEAD"))
    public void dpp$inject$start(CallbackInfo ci) {
        DatapackppClient.setCurrentWorld(this.level.getName());
    }
}
