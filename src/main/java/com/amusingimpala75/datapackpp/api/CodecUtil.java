package com.amusingimpala75.datapackpp.api;

import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Locale;

public class CodecUtil {


    public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> clazz) {
        return Codec.STRING.xmap(
                string -> {
                    for (T t : clazz.getEnumConstants()) {
                        if (t.name().toLowerCase(Locale.ROOT).equals(string)) {
                            return t;
                        }
                    }
                    throw new IllegalStateException("Unrecognized enum value: " + string + " for enum: " + clazz.getSimpleName());
                },
                t -> t.name().toLowerCase(Locale.ROOT)
        );
    }

    public static <T> Codec<T> registryCodec(Registry<T> registry) {
        return Identifier.CODEC.xmap(registry::get, registry::getId);
    }
}
