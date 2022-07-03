package com.amusingimpala75.datapackpp.client.screen;

import com.amusingimpala75.datapackpp.client.screen.widget.FolderWidget;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

//todo: scrolling, file/folder creation/removal
public class FileTreeScreen extends Screen {
    private final Path packFolder;
    private static final int MARGIN = 9 / 2;
    private final String packName;
    private TextFieldWidget fileName;
    public FileTreeScreen(Path packFolder) {
        super(Text.translatable("screen.dpp.file_tree"));
        this.packFolder = packFolder;
        this.packName = packFolder.getFileName().toString();
    }

    @Override
    public void init() {
        super.init();
        this.addDrawableChild(new FolderWidget(MARGIN, MARGIN + 20, this.width - MARGIN * 2, packFolder, this, null));
        int buttonHeight = 20;
        int buttonWidth = (this.width / 3) - MARGIN;
        int buttonY = this.height - (MARGIN + buttonHeight);
        this.addDrawableChild(new ButtonWidget(MARGIN, buttonY, buttonWidth, buttonHeight, Text.translatable("screen.dpp.create_file"), button -> this.create(false)));
        this.addDrawableChild(new ButtonWidget(this.width / 3, buttonY, buttonWidth, buttonHeight, Text.translatable("screen.dpp.create_folder"), button -> this.create(true)));
        this.addDrawableChild(new ButtonWidget((this.width / 3) * 2, buttonY, buttonWidth, buttonHeight, Text.translatable("screen.dpp.delete"), button -> this.delete()));
        this.fileName = new TextFieldWidget(this.textRenderer, MARGIN, this.height - (MARGIN + 20 + (20 + 2)), this.width - 2 * MARGIN, 20, Text.empty());
        this.fileName.setMaxLength(128);
        this.addSelectableChild(fileName);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.textRenderer.draw(matrices, Text.of(packName), MARGIN, MARGIN, 0xFFFFFFFF);
        this.fileName.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        super.tick();
        this.fileName.tick();
    }

    @Override
    public void remove(Element child) {
        super.remove(child);
    }

    @Override
    public <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
        return super.addDrawableChild(drawableElement);
    }

    private void create(boolean isFolder) {
        String path = fileName.getText();
        if (invalidPath(path)) return;    //todo: error message
        Path p = packFolder.resolve(path);
        if (Files.exists(p)) {
            return; //todo: error message
        }
        try {
            if (isFolder) {
                Files.createDirectories(p);
            } else {
                Files.createDirectories(p.getParent());
                Files.createFile(p);
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private void delete() {
        String path = fileName.getText();
        if (invalidPath(path)) return;
        Path p = packFolder.resolve(path);
        if (!Files.exists(p)) {
            return; //todo error message
        }
        try {   //todo: confirmation
            if (Files.isDirectory(p)) {
                FileUtils.deleteDirectory(new File(p.toString()));
            } else {
                Files.delete(p);
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean invalidPath(String p) {
        if (p.contains("..")) return true;
        return false;   //todo: there's probably more
    }
}
