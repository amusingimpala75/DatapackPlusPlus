package com.amusingimpala75.datapackpp.impl;

import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

public interface Identifiable {
    @Nullable
    Identifier dpp$getId();
    void dpp$setId(Identifier id);
}
