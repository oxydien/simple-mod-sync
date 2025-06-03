package dev.oxydien.ui;

import dev.oxydien.SimpleModSync;
import dev.oxydien.config.Config;
import dev.oxydien.ui.widget.SimpleBackgroundWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicBoolean;

public class SetSyncRemoteScreen extends Screen {
    private final Screen parent;

    public SetSyncRemoteScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    @Override
    public void init() {
        super.init();
        
        // Background widget
        this.addDrawableChild(new SimpleBackgroundWidget(0, 0, this.width, this.height, 0xF1000000));

        // Subtitle widget
        Text subtitleText = Text.translatable("simple_mod_sync.ui.set_sync_screen.subtitle");
        int x = this.width / 2 - subtitleText.getString().length() - 30;
        int y = this.getPosY(2) + 5;
        MultilineTextWidget subtitleWidget = new MultilineTextWidget(x, y, subtitleText, this.textRenderer);
        subtitleWidget.setMaxWidth(this.width - 80);
        subtitleWidget.setMaxRows(2);
        this.addDrawableChild(subtitleWidget);

        // URL field widget
        int urlFieldX = this.width / 2 - 100;
        int urlFieldY = this.getPosY(3);
        int urlFieldWidth = 200;
        int urlFieldHeight = 20;
        TextFieldWidget remoteUrl = new TextFieldWidget(this.textRenderer, urlFieldX, urlFieldY, urlFieldWidth, urlFieldHeight, Text.literal(""));
        remoteUrl.setMaxLength(368);
        this.addDrawableChild(remoteUrl);

        // Auto download toggle button widget
        Text autoDownloadTextTrue = Text.translatable("simple_mod_sync.ui.set_sync_screen.auto_download_true");
        Text autoDownloadTextFalse = Text.translatable("simple_mod_sync.ui.set_sync_screen.auto_download_false");
        AtomicBoolean autoDownload = new AtomicBoolean(Config.instance.getAutoDownload());
        int autoDownloadX = this.width / 2 - 70;
        int autoDownloadY = this.getPosY(4);
        ButtonWidget auto_download = new ButtonWidget.Builder(autoDownload.get() ? autoDownloadTextTrue : autoDownloadTextFalse, (buttonWidget) -> {
            autoDownload.set(!autoDownload.get());
            buttonWidget.setMessage(autoDownload.get() ? autoDownloadTextTrue : autoDownloadTextFalse);
        }).position(autoDownloadX, autoDownloadY).size(140, 20).build();
        this.addDrawableChild(auto_download);

        // Cancel button widget
        int cancelButtonX = this.width / 2 - 105;
        int cancelButtonY = this.getPosY(5);
        ButtonWidget.Builder cancelBuilder = new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.set_sync_screen.cancel_button"),
                (buttonWidget) -> {
                    Config.instance.setDownloadUrl("-");
                    MinecraftClient.getInstance().setScreen(parent);
                    SimpleModSync.StartWorker();
                });
        ButtonWidget cancelButton = cancelBuilder.position(cancelButtonX, cancelButtonY).size(100, 20).build();
        this.addDrawableChild(cancelButton);

        // Set button widget
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.set_sync_screen.confirm_button"), (buttonWidget) -> {
            String url = remoteUrl.getText();
            if (!url.isEmpty()) {
                Config.instance.setDownloadUrl(url);
                MinecraftClient.getInstance().setScreen(parent);
                SimpleModSync.StartWorker();
            }
        }).position(this.width / 2 + 5, this.getPosY(5)).size(100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        var matrices = context.getMatrices();

        // Title widget
        float scaleModifier = 1.7f;
        matrices.push();
        matrices.scale(scaleModifier, scaleModifier, scaleModifier); // Scale the text
        Text titleText = Text.translatable("simple_mod_sync.ui.set_sync_screen.title");
        int titleX = (int)(this.width * 0.6f) / 2 - titleText.getString().length() - 33;
        int titleY = 30;
        context.drawText(this.textRenderer, titleText, titleX, titleY, 0xFF3DF6B4, true);
        matrices.pop();
    }

    private int getPosY(int elementIndex) {
        return 50 + elementIndex * 25;
    }
}
