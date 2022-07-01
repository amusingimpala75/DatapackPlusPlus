package com.amusingimpala75.datapackpp.impl;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public class SupplierItem extends Item implements Supplier<Item> {

    private static final Item.Settings DEFAULT = new Item.Settings();

    private final Identifier id;

    private SupplierItem(Identifier id) {
        super(DEFAULT);
        this.id = id;
    }

    public static SupplierItem get(Identifier id) {
        return new SupplierItem(id);
    }

    @Override
    public Item get() {
        return Registry.ITEM.get(id);
    }
}
