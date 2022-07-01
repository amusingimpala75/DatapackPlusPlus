package com.amusingimpala75.datapackpp.mixin.accessor;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface TACSAccessor {
    @Invoker("entryIterator")
    Iterable<ChunkHolder> dpp$invoker$entryIterator();
}
