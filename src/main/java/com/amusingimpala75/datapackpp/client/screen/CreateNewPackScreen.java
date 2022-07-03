package com.amusingimpala75.datapackpp.client.screen;

import com.amusingimpala75.datapackpp.Datapackpp;
import com.amusingimpala75.datapackpp.client.screen.widget.CustomButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

//Pack id:
//[field]
//Description:
//[field]
public class CreateNewPackScreen extends Screen {
    private TextFieldWidget getId;
    private TextFieldWidget description;
    private final Path datapackFolder;
    private static final int MARGIN = 9;
    private static final int SPACING = 2;
    private static final int HEIGHT = 20;
    private boolean invalidPackId = true;
    private boolean alreadyExistingPack = false;
    public CreateNewPackScreen(Path datapackFolder) {
        super(Text.translatable("screen.dpp.new_pack"));
        this.datapackFolder = datapackFolder;
    }

    @Override
    public void init() {
        super.init();
        initScreen();
    }

    private void initScreen() {
        int width = this.width - MARGIN * 2;
        getId = new TextFieldWidget(this.textRenderer, MARGIN, MARGIN, width, HEIGHT, Text.empty());
        description = new TextFieldWidget(this.textRenderer, MARGIN, MARGIN + (HEIGHT + SPACING) * 2, width, HEIGHT, Text.empty());
        description.setMaxLength(Integer.MAX_VALUE);
        this.addSelectableChild(getId);
        this.addSelectableChild(description);
        this.setInitialFocus(getId);
        this.addDrawableChild(new CustomButtonWidget(MARGIN, MARGIN + (HEIGHT + SPACING) * 4, width, HEIGHT, Text.translatable("screen.dpp.create_pack"), button -> {
            this.createPack();
        }));
    }

    @Override
    public void tick() {
        super.tick();
        getId.tick();
        description.tick();
        String packId = getId.getText();
        invalidPackId = !Identifier.isValid(packId + ":f");
        alreadyExistingPack = Files.exists(datapackFolder.resolve(packId));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.textRenderer.draw(matrices, Text.translatable("screen.dpp.pack_id_prompt"), MARGIN, MARGIN + HEIGHT + SPACING + 2, 0xFFFFFFFF);
        this.textRenderer.draw(matrices, Text.translatable("screen.dpp.pack_desc_prompt"), MARGIN, MARGIN + (HEIGHT + SPACING) * 3 + 2, 0xFFFFFFFF);
        getId.render(matrices, mouseX, mouseY, delta);
        description.render(matrices, mouseX, mouseY, delta);
        Text error = null;
        if (invalidPackId) {
            error = Text.translatable("screen.dpp.invalid_pack_id");
        } else if (alreadyExistingPack) {
            error = Text.translatable("screen.dpp.pack_already_exists");
        }
        if (error != null) {
            this.textRenderer.draw(matrices, error, MARGIN, MARGIN + (HEIGHT + SPACING) * 5, 0xFFFF0000);
        }
    }

    private void createPack() {
        if (invalidPackId || alreadyExistingPack) {
            return;
        }
        String packId = getId.getText();
        String description = this.description.getText();
        Path newPack = datapackFolder.resolve(packId);
        String packMcmeta = PACK_MCMETA_TEMPLATE
                .replace("\"current_pack_format\"", String.valueOf(Datapackpp.LATEST_PACK_VERSION))
                .replace("pack_description", description);
        try {
            Files.createDirectory(newPack);
            Files.writeString(newPack.resolve("pack.mcmeta"), packMcmeta);
        } catch (IOException ioe) {
            Datapackpp.LOGGER.error("Could not create new datapack", ioe);
            return;
        }
        MinecraftClient.getInstance().setScreen(new EditPackScreen(newPack));
    }

    @Language("JSON")
    private static final String PACK_MCMETA_TEMPLATE = """
            {
                "pack": {
                    "pack_format": "current_pack_format",
                    "description": "pack_description"
                }
            }""";
}
