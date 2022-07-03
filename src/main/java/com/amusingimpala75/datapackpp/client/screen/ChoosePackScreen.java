package com.amusingimpala75.datapackpp.client.screen;

import com.amusingimpala75.datapackpp.client.DatapackppClient;
import com.amusingimpala75.datapackpp.client.screen.widget.CustomButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ChoosePackScreen extends Screen {
    private static final String RESET = "§r";
    private static final int MARGIN = 9;
    private static final int SPACING = 2;
    private static final int HEIGHT = 20;
    private final Map<String, DatapackType> datapacks = new HashMap<>();
    @Nullable
    private String selectedPack = null;
    private Path datapacksPath;
    public ChoosePackScreen() {
        super(Text.translatable("screen.dpp.choose_pack"));
        setupDatapackList();
    }

    private void setupDatapackList() {
        //noinspection ConstantConditions
        Path save = DatapackppClient.SAVES.resolve(DatapackppClient.getCurrentWorld());
        datapacksPath = save.resolve("datapacks");
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(datapacksPath)) {
            for (Path file : directory) {
                String fileName = file.getFileName().toString();
                if (Files.isDirectory(file) && Files.exists(file.resolve("pack.mcmeta"))) {
                    this.datapacks.put(fileName, DatapackType.DIRECTORY);
                } else if (fileName.endsWith("zip")) {
                    this.datapacks.put(fileName, DatapackType.ZIP);
                } else {
                    this.datapacks.put(fileName, DatapackType.INVALID);
                }
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Error reading datapacks folder", ioe);
        }
    }

    @Override
    public void init() {
        super.init();
        initWidgets();
    }

    private void initWidgets() {
        int margin = 9;

        int height = 20;
        int y = margin;
        int spacing = 2;
        int width = this.width - margin * 2;
        for (Map.Entry<String, DatapackType> pack : this.datapacks.entrySet()) {
            String msg = pack.getValue().color + pack.getKey() + RESET;
            this.addDrawableChild(new CustomButtonWidget(margin, y, width, height, Text.of(msg), button ->
                    this.selectedPack = pack.getKey()
            ));
            y += height + spacing;
        }
        this.addDrawableChild(new CustomButtonWidget(margin, y, width, height, Text.translatable("screen.dpp.new_pack"), button ->
                MinecraftClient.getInstance().setScreen(new CreateNewPackScreen(this.datapacksPath))
        ));
        this.addDrawableChild(new CustomButtonWidget(margin, this.height - (margin + height), width, height, Text.translatable("screen.dpp.edit_pack"), button -> {
            if (selectedPack != null) {
                MinecraftClient.getInstance().setScreen(new EditPackScreen(this.datapacksPath.resolve(selectedPack)));
            }
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        if (this.selectedPack != null) {
            DrawableHelper.drawCenteredText(matrices, this.textRenderer, Text.translatable("screen.dpp.selected_pack"), this.width / 2, this.height - ((MARGIN + HEIGHT) + (SPACING + HEIGHT / 2) * 2), 0xFFFFFFFF);
            DrawableHelper.drawCenteredText(matrices, this.textRenderer, Text.of(this.selectedPack), this.width / 2, this.height - ((MARGIN + HEIGHT) + (SPACING + HEIGHT / 2)), 0xFFFFFFFF);
        }
    }

    private enum DatapackType {
        DIRECTORY("§a"),  //white
        ZIP("§7"),    //grey
        INVALID("§c");      //red
        private final String color;
        DatapackType(String color) {
            this.color = color;
        }
    }
}
