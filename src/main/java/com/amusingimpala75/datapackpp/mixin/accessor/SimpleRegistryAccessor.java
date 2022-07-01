package com.amusingimpala75.datapackpp.mixin.accessor;

import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(SimpleRegistry.class)
public interface SimpleRegistryAccessor<T> {
    @Invoker("getEntries")
    List<RegistryEntry.Reference<T>> dpp$invoker$holderInOrder();
}
