package com.amusingimpala75.datapackpp.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//todo: syntax highlighting
public class EditFileScreen extends Screen {
    private final Path packFolder;
    private final Path file;
    private final List<String> lines = new ArrayList<>();
    private boolean dirty = true;
    private int ticksSinceLastSave = 0;
    private int cursorPosition = 0;
    private int cursorLine = 0;
    public EditFileScreen(Path packFolder, Path file) {
        super(Text.translatable("screen.dpp.edit_file"));
        this.packFolder = packFolder;
        this.file = file;
        String fileContents;
        try {
            fileContents = Files.readString(file);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        lines.addAll(Arrays.asList(fileContents.split("\n")));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> cursorPosition = Math.max(cursorPosition - 1, 0);
            case GLFW.GLFW_KEY_RIGHT -> cursorPosition = Math.min(cursorPosition + 1, lines.get(cursorLine).length());
            case GLFW.GLFW_KEY_UP -> {
                cursorLine = Math.max(cursorLine - 1, 0);
                cursorPosition = Math.min(lines.get(cursorLine).length(), cursorPosition);
            }
            case GLFW.GLFW_KEY_DOWN -> {
                cursorLine = Math.min(cursorLine + 1, lines.size() - 1);
                cursorPosition = Math.min(lines.get(cursorLine).length(), cursorPosition);
            }
            case GLFW.GLFW_KEY_DELETE, GLFW.GLFW_KEY_BACKSPACE -> {
                if (cursorPosition > 0) {
                    //remove character before cursor
                    String line = lines.get(cursorLine);
                    lines.set(cursorLine, line.substring(0, cursorPosition - 1) + line.substring(cursorPosition));
                    cursorPosition--;
                    dirty = true;
                } else if (cursorLine > 0) {
                    //take current line and append it to the one above
                    String line = lines.get(cursorLine);
                    String prev = lines.get(cursorLine - 1);
                    lines.remove(line);
                    lines.set(cursorLine - 1, prev + line);
                    cursorLine--;
                    cursorPosition = prev.length();
                    dirty = true;
                }
            }
            case GLFW.GLFW_KEY_ENTER -> {
                String line = lines.get(cursorLine);
                String line1 = line.substring(0, cursorPosition);
                String line2 = line.substring(cursorPosition);
                int index = lines.indexOf(line);
                lines.set(index, line1);
                lines.add(index + 1, line2);
                cursorLine++;
                cursorPosition = 0;
                dirty = true;
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean charTyped(char c, int mod) {
        String line = lines.get(cursorLine);
        lines.set(cursorLine, line.substring(0, cursorPosition) + c + line.substring(cursorPosition));
        cursorPosition++;
        dirty = true;
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (dirty) {
            ticksSinceLastSave++;
            if (ticksSinceLastSave > 100) { //auto save every 5 seconds? todo: configurable
                writeContents();
                ticksSinceLastSave = 0;
            }
        }
    }

    @Override
    public void close() {
        if (dirty) {
            writeContents();
        }
        MinecraftClient.getInstance().setScreen(new EditPackScreen(packFolder));
    }

    private void writeContents() {
        try {
            StringBuilder toWrite = new StringBuilder();
            lines.forEach(line -> {
                toWrite.append(line);
                toWrite.append("\n");
            });
            Files.writeString(file, toWrite.toString());
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        int yOffsetStep = 20;
        int yOffset = 9;
        int textHeight = this.textRenderer.fontHeight;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            this.textRenderer.draw(matrices, Text.of(line), 9, yOffset, 0xFFFFFFFF);
            if (cursorLine == i) {
                this.drawVerticalLine(matrices, 9 + this.textRenderer.getWidth(line.substring(0, cursorPosition)), yOffset - textHeight / 2, yOffset + textHeight, 0xFFFFFFFF);
            }
            yOffset += yOffsetStep;
        }
    }
}
