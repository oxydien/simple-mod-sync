package dev.oxydien.simpleModSync.ui.screens;

import dev.oxydien.simpleModSync.SimpleModSync;
import dev.oxydien.simpleModSync.config.Config;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
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

        // Title widget
        this.addRenderableWidget(new StringWidget(this.getCenteredXFor(this.title), currentY,
                CONTENT_WIDTH, 20, this.title, this.font));
        currentY += 20 + SPACING;

        // Sub-header widget
        Component subHeader = Component.translatable("simple_mod_sync.ui.init_screen.sub_header").withColor(0xAAAAAA);
        StringWidget subHeaderWidget = new StringWidget(this.getCenteredXFor(subHeader), currentY,
                CONTENT_WIDTH, 10, subHeader, this.font);
        this.addRenderableWidget(subHeaderWidget);
        currentY += 10 + SPACING;

        // Disclaimer line 1
        Component dis1 = Component.literal("By entering a URL below, you acknowledge that you are").withColor(0xAAAAAA);
        StringWidget disclaimer1 = new StringWidget(this.getCenteredXFor(dis1), currentY,
                CONTENT_WIDTH, 10, dis1, this.font);
        this.addRenderableWidget(disclaimer1);
        currentY += 10;

        // Disclaimer line 2
        Component dis2 = Component.literal("responsible for any content synced from that source.").withColor(0xAAAAAA);
        StringWidget disclaimer2 = new StringWidget(this.getCenteredXFor(dis2), currentY,
                CONTENT_WIDTH, 10, dis2, this.font);
        this.addRenderableWidget(disclaimer2);
        currentY += 10;

        // Disclaimer line 3
        Component dis3 = Component.literal("Use trusted sources only.").withColor(0xAAAAAA);
        StringWidget disclaimer3 = new StringWidget(this.getCenteredXFor(dis3), currentY,
                CONTENT_WIDTH, 10, dis3, this.font);
        this.addRenderableWidget(disclaimer3);
        currentY += 10 + SPACING * 2;

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
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
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

        this.minecraft.setScreenAndShow(new TitleScreen());
    }

    private int getCenteredXFor(Component component) {
        int centerX = this.width / 2;
        int textWidth = this.font.width(component.getString());
        return centerX - CONTENT_WIDTH / 2 + (CONTENT_WIDTH - textWidth) / 2;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(new TitleScreen());
    }
}