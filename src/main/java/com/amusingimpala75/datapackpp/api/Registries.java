package com.amusingimpala75.datapackpp.api;

import com.amusingimpala75.datapackpp.Datapackpp;
import com.mojang.serialization.Codec;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.HashMap;
import java.util.Map;

public class Registries {
    public static final Registry<Codec<? extends Item>> ITEM_CODEC = fromRegistry(Registry.ITEM);

    public static <T> Registry<Codec<? extends T>> getCodecFromRegistry(Registry<T> registry) {
        return Datapackpp.cast(REGISTRY2CODEC.get(registry));
    }

    private static final Map<Registry<?>, Registry<Codec<? extends Item>>> REGISTRY2CODEC = new HashMap<>();
    static {
        REGISTRY2CODEC.put(Registry.ITEM, ITEM_CODEC);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> Registry<Codec<? extends Item>> fromRegistry(Registry<T> registry) {
        RegistryKey<? extends Registry<Codec<? extends Item>>> key = RegistryKey.ofRegistry(Datapackpp.rl(registry.getKey().getValue().getPath() + "_codec"));
        return new SimpleRegistry<>(key, registry.getLifecycle(), null);
    }
}
