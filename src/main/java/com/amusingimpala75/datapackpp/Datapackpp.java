package com.amusingimpala75.datapackpp;

import com.amusingimpala75.datapackpp.api.ItemCodecs;
import com.amusingimpala75.datapackpp.impl.DuckItem;
import com.amusingimpala75.datapackpp.impl.ReloadListener;
import com.amusingimpala75.datapackpp.mixin.accessor.PlayerInventoryAccessor;
import com.amusingimpala75.datapackpp.mixin.accessor.TACSAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

public class Datapackpp implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("Datapack++");

    public static final String MOD_ID = "dpp";


    @Override
    public void onInitialize() {
        ItemCodecs.register();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ReloadListener<>(Registry.ITEM, (worlds, modified, removed) -> {
            for (ServerWorld instance : worlds) {
                for (Entity entity : instance.iterateEntities()) {
                    if (entity instanceof ItemEntity ie) {
                        if (processStack(ie.getStack(), modified, removed)) {
                            ie.kill();
                        }
                    } else if (entity instanceof PlayerEntity p) {
                        List<DefaultedList<ItemStack>> inventory = ((PlayerInventoryAccessor) p.getInventory()).dpp$accessor$combinedInventory();
                        for (DefaultedList<ItemStack> list : inventory) {
                            for (int i = 0; i < list.size(); i++) {
                                if (processStack(list.get(i), modified, removed)) {
                                    list.set(i, ItemStack.EMPTY);
                                }
                            }
                        }
                    }
                }
                ServerChunkManager chunks = instance.getChunkManager();
                for (ChunkHolder holder : ((TACSAccessor) chunks.threadedAnvilChunkStorage).dpp$invoker$entryIterator()) {
                    Chunk chunk = holder.getCurrentChunk();
                    if (chunk == null) {
                        continue;
                    }
                    chunk.getBlockEntityPositions().stream().map(chunk::getBlockEntity).forEach(entity -> {
                        if (entity instanceof Inventory inv) {
                            for (int i = 0; i < inv.size(); i++) {
                                if (processStack(inv.getStack(i), modified, removed)) {
                                    inv.removeStack(i);
                                }
                            }
                        }
                    });
                }
            }
        }));
    }

    /**
     * @return returns true if the stack should be destroyed
     * */
    private static boolean processStack(ItemStack stack, List<Item> modified, List<Item> removed) {
        Item item = stack.getItem();
        if (modified.contains(item)) {
            try {
                Item nw = Registry.ITEM.get(((DuckItem)item).dpp$getId());
                stack$item.set(stack, nw);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else return removed.contains(item);
        return false;
    }

    private static final Field stack$item;

    static {
        try {
            stack$item = ItemStack.class.getDeclaredField("item");
            stack$item.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Identifier rl(String path) {
        return new Identifier(MOD_ID, path);
    }

    @SuppressWarnings("unchecked")
    public static <T, V> V cast(T t) {
        return (V) t;
    }
}