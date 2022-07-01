package com.amusingimpala75.datapackpp.mixin;

import com.amusingimpala75.datapackpp.impl.DuckItem;
import com.amusingimpala75.datapackpp.impl.SupplierItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin implements DuckItem, ItemConvertible {

    @Shadow @Final @Nullable private Item recipeRemainder;

    @Unique
    private boolean retrivedYet = false;
    @Unique
    private Item.Settings dpp$properties;

    @Unique
    private Identifier dpp$id = null;

    @Nullable
    @Override
    public Identifier dpp$getId() {
        return dpp$id;
    }

    @Override
    public void dpp$setId(Identifier id) {
        this.dpp$id = id;
    }

    @Override
    public Item.Settings dpp$getProperties() {
        return dpp$properties;
    }

    @Inject(method = "<init>", at=@At("TAIL"))
    public void dpp$inject$init(Item.Settings properties, CallbackInfo ci) {
        this.dpp$properties = properties;
    }

    @Inject(method = "getRecipeRemainder", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$getCraftingRemainingItem(CallbackInfoReturnable<Item> cir) {
        if (!this.retrivedYet) {
            if (this.recipeRemainder instanceof SupplierItem sup) {
                cir.setReturnValue(sup.get());
            }
            this.retrivedYet = true;
        }
    }
}
