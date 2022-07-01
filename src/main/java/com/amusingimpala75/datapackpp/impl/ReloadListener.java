package com.amusingimpala75.datapackpp.impl;

import com.amusingimpala75.datapackpp.Datapackpp;
import com.amusingimpala75.datapackpp.ServerUtil;
import com.amusingimpala75.datapackpp.api.Registries;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

public class ReloadListener<T> implements SimpleSynchronousResourceReloadListener {

    private Map<Identifier, String> entryHashes;
    private int currentlyLoaded = 0;

    private final String folder;
    private final int skipSubstring;
    private final Registry<T> registry;
    private final Logger logger;
    private final Identifier id;
    private final TriConsumer<Iterable<ServerWorld>, List<T>, List<T>> afterReload;

    /**
     * @param afterReload worlds to process, modified items, removed items
     * */
    public ReloadListener(Registry<T> registry, TriConsumer<Iterable<ServerWorld>, List<T>, List<T>> afterReload) {
        this.registry = registry;
        this.folder = registry.getKey().getValue().getPath();
        this.skipSubstring = this.folder.length() + 5;
        this.logger = LoggerFactory.getLogger("Datapack++/" + capitalAndPlural(this.folder));
        this.id = Datapackpp.rl(this.folder);
        this.afterReload = afterReload;
    }

    @Override
    public void reload(ResourceManager manager) {
        logger.info(manager.getClass().getSimpleName());
        DuckRegistry<T> duck = Datapackpp.cast(this.registry);
        duck.beginReload();
        int counterNew = 0;
        int counterRetained = 0;
        int counterSkipped = 0;
        int counterModified = 0;
        List<T> modifiedEntries = new ArrayList<>();
        Map<Identifier, String> newRegisteredEntries = new HashMap<>();
        for (Map.Entry<Identifier, Resource> r : manager.findResources("dpp/" + this.folder, id -> id.getPath().endsWith(".json")).entrySet()) {
            Identifier fileId = r.getKey();
            String fileIdPath = fileId.getPath();
            Identifier id = new Identifier(fileId.getNamespace(), fileIdPath.substring(this.skipSubstring, fileIdPath.length() - 5));
            Resource res = r.getValue();
            try (Reader reader = res.getReader();
                 InputStream is = res.getInputStream()) {
                String hash = DigestUtils.sha256Hex(is);
                if (this.entryHashes == null || !this.entryHashes.containsKey(id) || !this.entryHashes.get(id).equals(hash)) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    Set<String> keys = root.keySet();
                    if (keys.size() < 1) {
                        logger.error("Empty json object in json " + id + ", skipping");
                        counterSkipped++;
                    } else if (keys.size() > 1) {
                        logger.error("Multiple base entries in json " + id + ", skipping");
                        counterSkipped++;
                    } else {
                        String codecName = keys.stream().findAny().get();
                        Codec<? extends T> codec = Registries.getCodecFromRegistry(registry).get(new Identifier(codecName));
                        if (codec == null) {
                            logger.error("No codec found for name " + codecName + ", skipping");
                            counterSkipped++;
                        } else {
                            T entry = null;
                            try {
                                entry = codec.decode(JsonOps.INSTANCE, root.get(codecName)).getOrThrow(false, Datapackpp.LOGGER::error).getFirst();
                            } catch (Throwable t) {
                                logger.error("Error proceessing codec for entry: " + id, t);
                            }
                            if (entry != null) {
                                duck.registerToDatapackRegistry(id, entry);
                                newRegisteredEntries.put(id, hash);
                                if (this.entryHashes != null && this.entryHashes.containsKey(id)) {
                                    modifiedEntries.add(entry);
                                    this.entryHashes.remove(id);
                                    counterModified++;
                                } else {
                                    counterNew++;
                                }
                                if (entry instanceof Identifiable ident) {
                                    ident.dpp$setId(id);
                                }
                            } else {
                                counterSkipped++;
                            }
                        }
                    }
                } else {
                    duck.copyOver(id);
                    newRegisteredEntries.put(id, this.entryHashes.remove(id));
                    counterRetained++;
                }
            } catch (IOException ioe) {
                logger.error("Error opening item json: " + id, ioe);
                counterSkipped++;
            }
        }

        duck.endReload();

        MinecraftServer server = ServerUtil.getCurrentServer();
        if (server != null) {
            List<T> removedEntries = new ArrayList<>();
            if (this.entryHashes != null) {
                for (Identifier id : this.entryHashes.keySet()) {
                    removedEntries.add(duck.getOldEntry(id));
                }
            }
            this.afterReload.accept(server.getWorlds(), modifiedEntries, removedEntries);
        } else {
            logger.warn("Server was null, ignore this if this is for world load and not pack reload");
        }

        entryHashes = newRegisteredEntries;
        printInfo(counterNew, counterRetained, counterModified, counterSkipped);
    }

    private void printInfo(int nw, int retained, int changed, int skipped) {
        StringBuilder msg = new StringBuilder();
        if (nw > 0) {
            msg.append("Loaded ").append(nw);
        }
        if (changed > 0) {
            if (!msg.isEmpty()) {
                msg.append(", ");
            }
            msg.append("Changed ").append(changed);
        }
        if (retained > 0) {
            if (!msg.isEmpty()) {
                msg.append(", ");
            }
            msg.append("Retained ").append(retained);
        }
        if (this.currentlyLoaded > retained + nw) {
            if (!msg.isEmpty()) {
                msg.append(", ");
            }
            msg.append("Removed ").append(this.currentlyLoaded - (nw + retained));
        }
        this.currentlyLoaded = nw + retained;
        logger.info(msg.toString());
        if (skipped > 0) {
            logger.warn("Skipped " + skipped);
        }
    }

    private static String capitalAndPlural(String folder) {
        return folder.substring(0, 1).toUpperCase(Locale.ROOT) + folder.substring(1);
    }

    @Override
    public Identifier getFabricId() {
        return this.id;
    }
}
