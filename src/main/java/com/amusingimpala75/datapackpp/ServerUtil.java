package com.amusingimpala75.datapackpp;

import net.minecraft.server.MinecraftServer;

public class ServerUtil {

    private static MinecraftServer currentServer = null;

    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }

    public static void setCurrentServer(MinecraftServer server) {
        currentServer = server;
    }
}
