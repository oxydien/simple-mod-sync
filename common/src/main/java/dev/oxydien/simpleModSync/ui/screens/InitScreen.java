package dev.oxydien.simpleModSync.ui.screens;

import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.config.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class InitScreen extends Screen {
    private static final int CONTENT_WIDTH = 300;
    private static final int SPACING = 10;
    private static final int TEXT_FIELD_HEIGHT = 20;
    private static final int BUTTON_HEIGHT = 20;

    private EditBox urlField;
    private Checkbox autoUpdateCheckbox;
    private int contentStartY;

    public InitScreen() {
        super(Component.translatable("simple_mod_sync.ui.init_screen.title"));
    }

    @Override
    protected void init() {
        super.init();

        int totalHeight = calculateTotalHeight();
        contentStartY = (this.height - totalHeight) / 2;

        int centerX = this.width / 2;
        int currentY = contentStartY;

        // Skip title
        currentY += 20 + SPACING;

        // Skip sub-header
        currentY += 10 + SPACING;

        // Skip disclaimer (3 lines)
        currentY += 30 + SPACING * 2;

        // URL text field
        urlField = new EditBox(this.font, centerX - CONTENT_WIDTH / 2, currentY,
                CONTENT_WIDTH, TEXT_FIELD_HEIGHT, Component.literal("URL"));
        urlField.setHint(Component.literal("https://example.com/sync.json"));
        urlField.setMaxLength(500);

        // Load current URL if exists
        String currentUrl = Config.instance.getSchemaFileUrl();
        if (currentUrl != null && !currentUrl.isEmpty()) {
            urlField.setValue(currentUrl);
        }

        this.addRenderableWidget(urlField);
        currentY += TEXT_FIELD_HEIGHT + SPACING * 2;

        // Auto-update checkbox
        autoUpdateCheckbox = Checkbox.builder(
                        Component.translatable("simple_mod_sync.ui.init_screen.auto_update"),
                        this.font)
                .pos(centerX - CONTENT_WIDTH / 2, currentY)
                .selected(Config.instance.getSyncOnStartup())
                .build();

        this.addRenderableWidget(autoUpdateCheckbox);
        currentY += BUTTON_HEIGHT + SPACING * 2;

        // Save button
        Button saveButton = Button.builder(
                        Component.translatable("simple_mod_sync.ui.init_screen.save"),
                        button -> this.onSave())
                .bounds(centerX - CONTENT_WIDTH / 2, currentY, CONTENT_WIDTH, BUTTON_HEIGHT)
                .build();

        this.addRenderableWidget(saveButton);
    }

    private int calculateTotalHeight() {
        int height = 0;
        height += 20; // Title
        height += SPACING;
        height += 10; // Sub-header
        height += SPACING;
        height += 30; // Disclaimer (3 lines)
        height += SPACING * 2;
        height += TEXT_FIELD_HEIGHT; // URL field
        height += SPACING * 2;
        height += BUTTON_HEIGHT; // Checkbox
        height += SPACING * 2;
        height += BUTTON_HEIGHT; // Save button
        return height;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int currentY = contentStartY;

        // Title
        guiGraphics.drawCenteredString(this.font, this.title, centerX, currentY, 0xFFFFFF);
        currentY += 20 + SPACING;

        // Sub-header
        Component subHeader = Component.translatable("simple_mod_sync.ui.init_screen.sub_header");
        guiGraphics.drawCenteredString(this.font, subHeader, centerX, currentY, 0xAAAAAA);
        currentY += 10 + SPACING;

        // Disclaimer
        String disclaimer = "§7By entering a URL below, you acknowledge that you are";
        String disclaimer2 = "§7responsible for any content synced from that source.";
        String disclaimer3 = "§7Use trusted sources only.";

        guiGraphics.drawCenteredString(this.font, disclaimer, centerX, currentY, 0xFFFFFF);
        currentY += 10;
        guiGraphics.drawCenteredString(this.font, disclaimer2, centerX, currentY, 0xFFFFFF);
        currentY += 10;
        guiGraphics.drawCenteredString(this.font, disclaimer3, centerX, currentY, 0xFFFFFF);
    }

    private void onSave() {
        String url = urlField.getValue().trim();

        // Validate URL
        if (!url.isEmpty()) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                urlField.setSuggestion(" Invalid URL. Try again.");
                return;
            }
            Config.instance.setSchemaFileUrl(url);
        }

        Config.instance.setHasVisitedInitScreen(true);
        Config.instance.setSyncOnStartup(autoUpdateCheckbox.selected());

        if (autoUpdateCheckbox.selected()) {
            SimpleModSync.getInstance().start();
        }

        this.minecraft.setScreen(new TitleScreen());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new TitleScreen());
    }
}