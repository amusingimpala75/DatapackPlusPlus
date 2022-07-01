package com.amusingimpala75.datapackpp.mixin;

import com.amusingimpala75.datapackpp.impl.DuckRegistry;
import com.amusingimpala75.datapackpp.mixin.accessor.SimpleRegistryAccessor;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("CommentedOutCode")
@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> extends MutableRegistry<T> implements DuckRegistry<T> {
    @Shadow @Final @Nullable private Function<T, RegistryEntry.Reference<T>> valueToEntryFunction;

    @Shadow public abstract int size();

    @Shadow private boolean frozen;

    @Shadow public abstract Optional<RegistryKey<T>> getKey(T entry);

    @Shadow public abstract Lifecycle getLifecycle();

    @Shadow public abstract RegistryEntry<T> replace(OptionalInt rawId, RegistryKey<T> key, T newEntry, Lifecycle lifecycle);

    public SimpleRegistryMixin(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        super(key, lifecycle);
        throw new IllegalStateException("Whoop-dee-doo, mixin failed");
    }

    @Override
    public void registerToDatapackRegistry(Identifier id, T entry) {
        Registry.register(this.datapackRegistryNew, id, entry);
    }

    @Override
    public void copyOver(Identifier id) {
        if (this.datapackRegistry == null) {
            throw new IllegalStateException("Tried to copy over entry when reloading for the first time!");
        }
        T entry = this.datapackRegistry.get(id);
        if (entry == null) {
            throw new IllegalStateException("Tried to copy over entry but it was not previously present!");
        }
        Registry.register(this.datapackRegistryNew, id, entry);
    }

    @Unique
    private SimpleRegistry<T> datapackRegistry;

    @Unique
    private SimpleRegistry<T> datapackRegistryNew;

    @Unique
    private SimpleRegistry<T> datapackRegistryOld;

    @Override
    public void beginReload() {
        this.datapackRegistryNew = new SimpleRegistry<>(this.getKey(), this.getLifecycle(), this.valueToEntryFunction);
    }

    @Override
    public void endReload() {
        this.datapackRegistryOld = this.datapackRegistry;
        this.datapackRegistry = this.datapackRegistryNew;
        this.datapackRegistryNew = null;
    }

    @Override
    public T getOldEntry(Identifier id) {
        return this.datapackRegistryOld.get(id);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "getEntries", at=@At("RETURN"), cancellable = true)
    private void dpp$inject$getEntries(CallbackInfoReturnable<List<RegistryEntry.Reference<T>>> cir) {
        if (this.datapackRegistry != null) {
            List<RegistryEntry.Reference<T>> list = new ArrayList<>();
            list.addAll(cir.getReturnValue());
            list.addAll(((SimpleRegistryAccessor<T>)this.datapackRegistry).dpp$invoker$holderInOrder());
            cir.setReturnValue(list);
        }
    }

    /**
     * @author AmusingImpala75
     * @reason I figured this was more acceptable than silent Inject-and-cancel-always
     */
    @Overwrite
    private void assertNotFrozen(RegistryKey<T> resourceKey) {
        //no-op
    }

    @Inject(method = "set(ILnet/minecraft/util/registry/RegistryKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/util/registry/RegistryEntry;", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$registerMapping(int rawId, RegistryKey<T> key, T value, Lifecycle lifecycle, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        this.dpp$onlyInternalSize = true;
        if (this.datapackRegistry != null && rawId >= this.size()) {
            cir.setReturnValue(this.datapackRegistry.set(rawId - this.size(), key, value, lifecycle));
        }
        this.dpp$onlyInternalSize = false;
    }

    @Inject(method = "set(ILnet/minecraft/util/registry/RegistryKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;Z)Lnet/minecraft/util/registry/RegistryEntry;", at=@At("HEAD"), cancellable = true)
    private void dpp$inject$registerMapping(int rawId, RegistryKey<T> key, T value, Lifecycle lifecycle, boolean checkDuplicateKeys, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        this.dpp$onlyInternalSize = true;
        if (this.datapackRegistry != null && rawId >= this.size()) {
            cir.setReturnValue(this.datapackRegistry.set(rawId - this.size(), key, value, lifecycle));
        }
        this.dpp$onlyInternalSize = false;
    }

//    public Holder<T> register(ResourceKey<T> key, T entry, Lifecycle lifecycle);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Inject(method = "replace", at=@At("TAIL"), cancellable = true)
    public void dpp$inject$replace(OptionalInt rawId, RegistryKey<T> key, T newEntry, Lifecycle lifecycle, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        if (this.datapackRegistry != null) {
            this.dpp$onlyInternalSize = true;
            if ((rawId.isPresent() && rawId.getAsInt() >= this.size()) || this.datapackRegistry.contains(key)) {
                if (rawId.isPresent()) {
                    rawId = OptionalInt.of(rawId.getAsInt() - this.size());
                }
                cir.setReturnValue(this.datapackRegistry.replace(rawId, key, newEntry, lifecycle));
            }
            this.dpp$onlyInternalSize = false;
        }
    }

    @Inject(method = "getId", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$getId(T value, CallbackInfoReturnable<Identifier> cir) {
        if (this.datapackRegistry != null && cir.getReturnValue() == null) {
            cir.setReturnValue(this.datapackRegistry.getId(value));
        }
    }

    @Inject(method = "getKey", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$$getKey(T entry, CallbackInfoReturnable<Optional<RegistryKey<T>>> cir) {
        if (this.datapackRegistry != null && cir.getReturnValue().isEmpty()) {
            cir.setReturnValue(this.datapackRegistry.getKey(entry));
        }
    }

    @Inject(method = "getRawId", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$getRawId(T value, CallbackInfoReturnable<Integer> cir) {
        if (this.datapackRegistry != null && cir.getReturnValue() == -1) {
            this.dpp$onlyInternalSize = true;
            cir.setReturnValue(this.size() + this.datapackRegistry.getRawId(value));
            this.dpp$onlyInternalSize = false;
        }
    }

    @Inject(method = "get(Lnet/minecraft/util/registry/RegistryKey;)Ljava/lang/Object;", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$get(RegistryKey<T> key, CallbackInfoReturnable<T> cir) {
        if (this.datapackRegistry != null && this.datapackRegistry.contains(key)) {
            cir.setReturnValue(this.datapackRegistry.get(key));
        }
    }

    @Inject(method = "get(I)Ljava/lang/Object;", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$get(int index, CallbackInfoReturnable<T> cir) {
        this.dpp$onlyInternalSize = true;
        if (this.datapackRegistry != null && index >= this.size()) {
            cir.setReturnValue(this.datapackRegistry.get(index - this.size()));
        }
        this.dpp$onlyInternalSize = false;
    }

    @Inject(method = "getEntry(I)Ljava/util/Optional;", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$getEntry(int rawId, CallbackInfoReturnable<Optional<RegistryEntry<T>>> cir) {
        this.dpp$onlyInternalSize = true;
        if (this.datapackRegistry != null && rawId >= this.size()) {
            cir.setReturnValue(this.datapackRegistry.getEntry(rawId - this.size()));
        }
        this.dpp$onlyInternalSize = false;
    }

    @Inject(method = "getEntry(Lnet/minecraft/util/registry/RegistryKey;)Ljava/util/Optional;", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$getEntry(RegistryKey<T> key, CallbackInfoReturnable<Optional<RegistryEntry<T>>> cir) {
        if (this.datapackRegistry != null && this.datapackRegistry.contains(key)) {
            cir.setReturnValue(this.datapackRegistry.getEntry(key));
        }
    }

    @Inject(method = "getOrCreateEntry", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$getOrCreateEntry(RegistryKey<T> key, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        if (this.datapackRegistry != null && this.datapackRegistry.contains(key)) {
            cir.setReturnValue(this.datapackRegistry.getOrCreateEntry(key));
        }
    }

    @Inject(method = "getOrCreateEntryDataResult", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$getOrCreateEntryDataResult(RegistryKey<T> key, CallbackInfoReturnable<DataResult<RegistryEntry<T>>> cir) {
        if (this.datapackRegistry != null && this.datapackRegistry.contains(key)) {
            cir.setReturnValue(this.datapackRegistry.getOrCreateEntryDataResult(key));
        }
    }

    @SuppressWarnings("UnusedAssignment")
    private boolean dpp$onlyInternalSize = false;

    @Inject(method = "size", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$size(CallbackInfoReturnable<Integer> cir) {
        if (this.datapackRegistry != null) {
            if (!dpp$onlyInternalSize) {
                cir.setReturnValue(cir.getReturnValue() + this.datapackRegistry.size());
            }
        }
    }

    @Inject(method = "getEntryLifecycle", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$getEntryLifecycle(T entry, CallbackInfoReturnable<Lifecycle> cir) {
        if (this.datapackRegistry != null && cir.getReturnValue() == null) {
            cir.setReturnValue(this.datapackRegistry.getEntryLifecycle(entry));
        }
    }

//    public Lifecycle elementsLifecycle();

//    public void iterator(CallbackInfoReturnable<Iterator<T>> cir);

    @Inject(method = "get(Lnet/minecraft/util/Identifier;)Ljava/lang/Object;", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$get(Identifier id, CallbackInfoReturnable<T> cir) {
        if (this.datapackRegistry != null && this.datapackRegistry.containsId(id)) {
            cir.setReturnValue(this.datapackRegistry.get(id));
        }
    }

//    private static <T> void getValueFromNullable(Holder.Reference<T> entry, CallbackInfoReturnable<T> cir);

    @Inject(method = "getIds", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$getIds(CallbackInfoReturnable<Set<Identifier>> cir) {
        if (this.datapackRegistry != null) {
            cir.setReturnValue(Sets.union(cir.getReturnValue(), this.datapackRegistry.getIds()));
        }
    }

    @Inject(method = "getKeys", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$getKeys(CallbackInfoReturnable<Set<RegistryKey<T>>> cir) {
        if (this.datapackRegistry != null) {
            cir.setReturnValue(Sets.union(cir.getReturnValue(), this.datapackRegistry.getKeys()));
        }
    }

    @Inject(method = "getEntrySet", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$getEntrySet(CallbackInfoReturnable<Set<Map.Entry<RegistryKey<T>, T>>> cir) {
        if (this.datapackRegistry != null) {
            cir.setReturnValue(Sets.union(cir.getReturnValue(), this.datapackRegistry.getEntrySet()));
        }
    }

//    public void holders(CallbackInfoReturnable<Stream<Holder.Reference<T>>> cir);

    @Inject(method = "containsTag", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$containsTag(TagKey<T> tag, CallbackInfoReturnable<Boolean> cir) {
        if (this.datapackRegistry != null && !cir.getReturnValue()) {
            cir.setReturnValue(this.datapackRegistry.containsTag(tag));
        }
    }

    @Inject(method = "streamTagsAndEntries", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$streamTagsAndEntries(CallbackInfoReturnable<Stream<Pair<TagKey<T>, RegistryEntryList.Named<T>>>> cir) {
        if (this.datapackRegistry != null) {
            cir.setReturnValue(Streams.concat(cir.getReturnValue(), this.datapackRegistry.streamTagsAndEntries()));
        }
    }

    @Inject(method = "getOrCreateEntryList", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$getOrCreateEntryList(TagKey<T> tag, CallbackInfoReturnable<RegistryEntryList.Named<T>> cir) {
        if (this.datapackRegistry != null && this.datapackRegistry.containsTag(tag)) {
            cir.setReturnValue(this.datapackRegistry.getOrCreateEntryList(tag));
        }
    }

//    private void createTag(TagKey<T> tag, CallbackInfoReturnable<HolderSet.Named<T>> cir);

    @Inject(method = "streamTags", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$streamTags(CallbackInfoReturnable<Stream<TagKey<T>>> cir) {
        if (this.datapackRegistry != null) {
            cir.setReturnValue(Streams.concat(cir.getReturnValue(), this.datapackRegistry.streamTags()));
        }
    }

    @Inject(method = "isEmpty", at=@At("RETURN"), cancellable = true)
    public void dpp$inject$isEmpty(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && this.datapackRegistry != null) {
            cir.setReturnValue(this.datapackRegistry.isEmpty());
        }
    }

//    public void getRandom(RandomSource random, CallbackInfoReturnable<Optional<Holder<T>>> cir);

    @Inject(method = "containsId", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$containsKey(Identifier id, CallbackInfoReturnable<Boolean> cir) {
        if (this.datapackRegistry != null && this.datapackRegistry.containsId(id)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "contains", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$contains(RegistryKey<T> key, CallbackInfoReturnable<Boolean> cir) {
        if (this.datapackRegistry != null && this.datapackRegistry.contains(key)) {
            cir.setReturnValue(true);
        }
    }

//    public Registry<T> freeze();

    @Inject(method = "createEntry", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$createEntry(T value, CallbackInfoReturnable<RegistryEntry.Reference<T>> cir) {
        if (this.frozen && this.datapackRegistryNew != null) {
            cir.setReturnValue(this.datapackRegistryNew.createEntry(value));
        }
    }

    @Inject(method = "getEntryList", at=@At("HEAD"), cancellable = true)
    public void dpp$inject$getEntryList(TagKey<T> tag, CallbackInfoReturnable<Optional<RegistryEntryList.Named<T>>> cir) {
        if (this.datapackRegistry != null && this.datapackRegistry.containsTag(tag)) {
            cir.setReturnValue(this.datapackRegistry.getEntryList(tag));
        }
    }

//    public void bindTags(Map<TagKey<T>, List<Holder<T>>> tagEntries);

//    public void resetTags();
}
