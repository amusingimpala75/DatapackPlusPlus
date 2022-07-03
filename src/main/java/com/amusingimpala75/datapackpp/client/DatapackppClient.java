package com.amusingimpala75.datapackpp.client;

import com.amusingimpala75.datapackpp.client.screen.ChoosePackScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;

//todo render screen titles
@Environment(EnvType.CLIENT)
public class DatapackppClient implements ClientModInitializer {

    private static String currentWorld = null;
    public static void setCurrentWorld(@Nullable String world) {
        currentWorld = world;
    }
    @Nullable
    public static String getCurrentWorld() {
        return currentWorld;
    }

    private static final KeyBinding OPEN_DATAPACK_EDIT = new KeyBinding(
            "key.dpp.open_edit_pack_screen", InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P, "key.category.dpp"
    );
    public static final Path SAVES = FabricLoader.getInstance().getGameDir().resolve("saves");
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(OPEN_DATAPACK_EDIT);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen == null && currentWorld != null && OPEN_DATAPACK_EDIT.wasPressed()) {
                client.setScreen(new ChoosePackScreen());
            }
        });
    }
}
