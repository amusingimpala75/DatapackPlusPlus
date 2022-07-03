package com.amusingimpala75.datapackpp.client.screen.widget;

import com.amusingimpala75.datapackpp.client.screen.EditFileScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.nio.file.Path;

//todo: open editing screen
public class FileWidget extends CustomButtonWidget {
    private final Path file;
    private final Path packFolder;
    public FileWidget(int y, FolderWidget parent, Path file) {
        super(parent.x + INDENT, y, parent.getWidth() - INDENT, 20, Text.of(file.getFileName().toString()), button -> {});
        this.file = file;
        FolderWidget root = parent;
        while (root.parent != null) root = root.parent;
        packFolder = root.folder;
    }

    @Override
    public void onPress() {
        super.onPress();
        MinecraftClient.getInstance().setScreen(new EditFileScreen(packFolder, file));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }
}
