package com.amusingimpala75.datapackpp.client.screen;

import com.amusingimpala75.datapackpp.client.screen.widget.CustomButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.nio.file.Path;

public class EditPackScreen extends Screen {
    private final Path datapack;
    private static final int MARGIN = 9;
    private static final int SPACING = 2;
    private static final int HEIGHT = 20;
    public EditPackScreen(Path folder) {
        super(Text.translatable("screen.dpp.edit_pack"));
        this.datapack = folder;
    }

    @Override
    public void init() {
        newButton(Text.translatable("screen.dpp.file_tree"), button -> MinecraftClient.getInstance().setScreen(new FileTreeScreen(datapack)));
    }

    private int buttonY = MARGIN;
    public void newButton(Text text, ButtonWidget.PressAction action) {
        this.addDrawableChild(new CustomButtonWidget(MARGIN, buttonY, this.width - MARGIN * 2, HEIGHT, text, action));
        buttonY += HEIGHT + SPACING;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
