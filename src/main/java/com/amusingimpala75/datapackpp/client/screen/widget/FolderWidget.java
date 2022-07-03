package com.amusingimpala75.datapackpp.client.screen.widget;

import com.amusingimpala75.datapackpp.client.screen.FileTreeScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//todo file and folder creation
public class FolderWidget extends CustomButtonWidget {
    private static final int SPACING = 2;
    private boolean isOpen = false;
    final Path folder;
    private final List<FolderWidget> subFolders = new ArrayList<>();
    private final List<FileWidget> files = new ArrayList<>();
    private final FileTreeScreen root;
    @Nullable
    final FolderWidget parent;
    public FolderWidget(int x, int y, int width, Path folder, FileTreeScreen root, @Nullable FolderWidget parent) {
        super(x, y, width, 20, Text.empty(), button -> {});
        this.folder = folder;
        updateMessage();
        this.root = root;
        this.parent = parent;
    }

    public FolderWidget(int y, FolderWidget parent, Path folder) {
        this(parent.x + INDENT, y, parent.width - INDENT, folder, parent.root, parent);
    }

    @Override
    public void onPress() {
        super.onPress();
        isOpen = !isOpen;
        //todo: resize parent's children beneath this one
        if (isOpen) {
            loadFiles();
            subFolders.forEach(root::addDrawableChild);
            files.forEach(root::addDrawableChild);
            if (parent != null) parent.shiftBeneath(this, subFolders.size() + files.size());
        } else {
            removeFiles();
            if (parent != null) parent.shiftBeneath(this, 0);
        }
        updateMessage();
    }

    private void removeFiles() {
        subFolders.forEach(folder -> {
            folder.removeFiles();
            root.remove(folder);
        });
        subFolders.clear();
        files.forEach(root::remove);
        files.clear();
    }

    private void updateMessage() {
        this.setMessage(Text.of((isOpen ? "V " : "> ") + folder.getFileName().toString()));
    }

    private void loadFiles() {
        int yOffsetStep = 20;
        int yOffset = yOffsetStep + SPACING;
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> subFiles = Files.newDirectoryStream(folder)) {
            for (Path p : subFiles) {
                if (Files.isDirectory(p)) {
                    subFolders.add(new FolderWidget(this.y + yOffset, this, p));
                    yOffset += yOffsetStep + SPACING;
                } else files.add(p);
            }
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        for (Path p : files) {
            this.files.add(new FileWidget(this.y + yOffset, this, p));
            yOffset += yOffsetStep + SPACING;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        //draw sub folders
        if (isOpen) {
            this.subFolders.forEach(widget -> widget.render(matrices,  mouseX, mouseY, delta));
            this.files.forEach(widget -> widget.render(matrices, mouseX, mouseY, delta));
        }
    }

    private void shiftBeneath(FolderWidget child, int howManyExtra) {
        int initialHeight = child.y + (20 + SPACING);
        initialHeight += (20 + SPACING) * howManyExtra; //height of 20 to external field
        for (int i = this.subFolders.indexOf(child) + 1; i < this.subFolders.size(); i++) {
            child.y = initialHeight;
            initialHeight += 20 + SPACING;
        }
        for (FileWidget file : files) {
            file.y = initialHeight;
            initialHeight += 20 + SPACING;
        }
        if (this.parent != null) {
            this.parent.shiftBeneath(this, howManyExtra + subFolders.size() + files.size());
        }
    }
}
