package com.amusingimpala75.datapackpp.api;

import com.amusingimpala75.datapackpp.impl.DuckItem;
import com.amusingimpala75.datapackpp.impl.ItemCodecsImpl;
import com.amusingimpala75.datapackpp.impl.SupplierItem;
import com.amusingimpala75.datapackpp.mixin.accessor.ItemSettingsAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class ItemCodecs {
    public static final Codec<Item.Settings> ITEM_SETTINGS_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("max_count").orElse(0).forGetter(p -> ((ItemSettingsAccessor)p).dpp$accessor$maxCount()),
            Codec.INT.fieldOf("max_uses").orElse(0).forGetter(p -> ((ItemSettingsAccessor)p).dpp$accessor$maxDamage()),
            Identifier.CODEC.fieldOf("recipe_remainder").orElse(ItemCodecsImpl.EMPTY).forGetter(p -> Registry.ITEM.getId(((ItemSettingsAccessor)p).dpp$accessor$recipeRemainder())),
            ItemCodecsImpl.ITEM_GROUP_CODEC.optionalFieldOf("group").forGetter(p -> Optional.ofNullable(((ItemSettingsAccessor)p).dpp$accessor$group())),
            CodecUtil.enumCodec(Rarity.class).fieldOf("rarity").orElse(Rarity.COMMON).forGetter(p -> ((ItemSettingsAccessor)p).dpp$accessor$rarity()),
            ItemCodecsImpl.FOOD_PROPERTIES_CODEC.fieldOf("food").orElse(ItemCodecsImpl.DEFAULT_PROPERTIES).forGetter(p -> ((ItemSettingsAccessor)p).dpp$accessor$foodComponent()),
            Codec.BOOL.fieldOf("fireproof").orElse(false).forGetter(p -> ((ItemSettingsAccessor)p).dpp$accessor$fireproof())
    ).apply(inst, (maxCount, maxUses, recipeRemainder, group, rarity, foodComponent, fireproof) -> {
        Item.Settings settings = new Item.Settings();
        if (maxCount != 0) {
            settings.maxCount(maxCount);
        }
        if (maxUses != 0) {
            settings.maxDamage(maxUses);
        }
        if (recipeRemainder != ItemCodecsImpl.EMPTY) {
            settings.recipeRemainder(SupplierItem.get(recipeRemainder));
        }
        group.ifPresent(settings::group);
        if (rarity != Rarity.COMMON) {
            settings.rarity(rarity);
        }
        if (foodComponent != ItemCodecsImpl.DEFAULT_PROPERTIES) {
            settings.food(foodComponent);
        }
        if (fireproof) {
            settings.fireproof();
        }
        return settings;
    }));

    public static final Codec<? extends Item> DEFAULT_ITEM_CODEC = RecordCodecBuilder.create(inst -> inst.group(
        ITEM_SETTINGS_CODEC.fieldOf("properties").orElse(new Item.Settings()).forGetter(i -> ((DuckItem)i).dpp$getProperties())
    ).apply(inst, Item::new));

    public static void register() {
        Registry.register(Registries.ITEM_CODEC, new Identifier("item"), DEFAULT_ITEM_CODEC);
    }
}
