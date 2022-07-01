package com.amusingimpala75.datapackpp.impl;

import net.minecraft.util.Identifier;

public interface DuckRegistry<T> {
    void registerToDatapackRegistry(Identifier id, T entry);
    void copyOver(Identifier id);
    void beginReload();
    void endReload();
    T getOldEntry(Identifier id);
}
