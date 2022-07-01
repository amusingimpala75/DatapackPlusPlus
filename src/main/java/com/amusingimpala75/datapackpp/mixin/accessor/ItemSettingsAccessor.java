package com.amusingimpala75.datapackpp.mixin.accessor;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.Settings.class)
public interface ItemSettingsAccessor {
    @Accessor("maxCount")
    int dpp$accessor$maxCount();
    @Accessor("maxDamage")
    int dpp$accessor$maxDamage();
    @Accessor("recipeRemainder")
    Item dpp$accessor$recipeRemainder();
    @Accessor("group")
    ItemGroup dpp$accessor$group();
    @Accessor("rarity")
    Rarity dpp$accessor$rarity();
    @Accessor("fireproof")
    boolean dpp$accessor$fireproof();
    @Accessor("foodComponent")
    FoodComponent dpp$accessor$foodComponent();
}
