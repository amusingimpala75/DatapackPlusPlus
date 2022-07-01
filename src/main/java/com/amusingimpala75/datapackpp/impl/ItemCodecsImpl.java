package com.amusingimpala75.datapackpp.impl;

import com.amusingimpala75.datapackpp.api.CodecUtil;
import com.amusingimpala75.datapackpp.mixin.accessor.ItemGroupAccessor;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemCodecsImpl {
    public static final Identifier EMPTY = new Identifier("", "");
    public static final FoodComponent DEFAULT_PROPERTIES = (new FoodComponent.Builder()).build();
    public static final Codec<ItemGroup> ITEM_GROUP_CODEC = Codec.STRING.xmap(
            string -> {
                for (ItemGroup tab : ItemGroup.GROUPS) {
                    if (((ItemGroupAccessor)tab).dpp$accessor$id().equals(string)) {
                        return tab;
                    }
                }
                throw new IllegalStateException("Unrecognized Creative Mode Tab: " + string);
            },
            tab -> ((ItemGroupAccessor)tab).dpp$accessor$id()
    );
    //todo: what the heck is FactorData?
    //todo: allow some stacking
    private static final Codec<StatusEffectInstance> STATUS_EFFECT_INSTANCE_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            CodecUtil.registryCodec(Registry.STATUS_EFFECT).fieldOf("effect").forGetter(StatusEffectInstance::getEffectType),
            Codec.INT.fieldOf("duration").orElse(0).forGetter(StatusEffectInstance::getDuration),
            Codec.INT.fieldOf("amplifier").orElse(0).forGetter(StatusEffectInstance::getAmplifier),
            Codec.BOOL.fieldOf("ambient").orElse(false).forGetter(StatusEffectInstance::isAmbient),
            Codec.BOOL.fieldOf("show_particles").orElse(true).forGetter(StatusEffectInstance::shouldShowParticles),
            Codec.BOOL.fieldOf("show_icon").orElse(true).forGetter(StatusEffectInstance::shouldShowIcon)
    ).apply(inst, StatusEffectInstance::new));
    private static final Codec<Pair<StatusEffectInstance, Float>> EFFECT_INSTANCE_PAIR_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            STATUS_EFFECT_INSTANCE_CODEC.fieldOf("effect").forGetter(Pair::getFirst),
            Codec.FLOAT.fieldOf("chance").forGetter(Pair::getSecond)
    ).apply(inst, Pair::new));

    private static final List<Pair<StatusEffectInstance, Float>> EMPTY_LIST = new ArrayList<>();
    public static final Codec<FoodComponent> FOOD_PROPERTIES_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("hunger").forGetter(p -> Optional.of(p.getHunger())),
            Codec.FLOAT.optionalFieldOf("saturation").forGetter(p -> Optional.of(p.getSaturationModifier())),
            Codec.BOOL.fieldOf("meat").orElse(false).forGetter(FoodComponent::isMeat),
            Codec.BOOL.fieldOf("can_always_eat").orElse(false).forGetter(FoodComponent::isAlwaysEdible),
            Codec.BOOL.fieldOf("eat_quickly").orElse(false).forGetter(FoodComponent::isSnack),
            EFFECT_INSTANCE_PAIR_CODEC.listOf().fieldOf("effects").orElse(EMPTY_LIST).forGetter(FoodComponent::getStatusEffects)
    ).apply(inst, (nutrition, saturation, meat, canAlwaysEat, eatQuickly, effects) -> {
        FoodComponent.Builder builder = new FoodComponent.Builder();
        nutrition.ifPresent(builder::hunger);
        saturation.ifPresent(builder::saturationModifier);
        if (meat) {
            builder.meat();
        }
        if (canAlwaysEat) {
            builder.alwaysEdible();
        }
        if (eatQuickly) {
            builder.snack();
        }
        if (effects != null && effects.size() > 0) {
            for (Pair<StatusEffectInstance, Float> p : effects) {
                builder.statusEffect(p.getFirst(), p.getSecond());
            }
        }
        return builder.build();
    }));
}
