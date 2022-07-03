package com.amusingimpala75.datapackpp.client.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class CustomButtonWidget extends ButtonWidget {
    protected static final int INDENT = 5;
    public CustomButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!this.visible) {
            return;
        }
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        MinecraftClient.getInstance().textRenderer.draw(matrices, this.getMessage(), this.x + INDENT * 3, this.y + (this.height / 3f), 0xFFFFFFFF);
        //draw box
        int x1 = this.x, x2 = this.x + this.width;
        int y1 = this.y, y2 = this.y + this.height;
        this.drawHorizontalLine(matrices, x1, x2, y1, 0xFFFFFFFF); //top
        this.drawHorizontalLine(matrices, x1, x2, y2, 0xFFFFFFFF); //bottom
        this.drawVerticalLine(matrices, x1, y1, y2, 0xFFFFFFFF);  //left
        this.drawVerticalLine(matrices, x2, y1, y2, 0xFFFFFFFF); //right
    }
}
