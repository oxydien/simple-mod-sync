package dev.oxydien.ui;

import dev.oxydien.SimpleModSync;
import dev.oxydien.config.Config;
import dev.oxydien.ui.widget.SimpleBackgroundWidget;
import net.minecraft.client.MinecraftClient;
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
        this.addDrawableChild(new SimpleBackgroundWidget(20, 20, this.width - 40, this.height - 40, 0));

        // Title widget
        Text titleText = Text.translatable("simple_mod_sync.ui.set_sync_screen.title");
        this.addDrawableChild(
                new MultilineTextWidget(this.width / 2 - titleText.getString().length() - 30, this.height / 2 - 40, titleText , this.textRenderer)
                        .setTextColor(0xFF3DF6B4));

        // Subtitle widget
        Text subtitleText = Text.translatable("simple_mod_sync.ui.set_sync_screen.subtitle");
        this.addDrawableChild(
                new MultilineTextWidget(this.width / 2 - subtitleText.getString().length() - 30, this.height / 2 - 20, subtitleText , this.textRenderer)
                        .setMaxWidth(this.width - 80).setMaxRows(2));

        // URL field widget
        TextFieldWidget remote_url = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 + 20, 200, 20, Text.literal(""));
        remote_url.setMaxLength(368);
        this.addDrawableChild(remote_url);

        // Auto download toggle button widget
        AtomicBoolean autoDownload = new AtomicBoolean(Config.instance.getAutoDownload());
        Text autoDownloadTextTrue = Text.translatable("simple_mod_sync.ui.set_sync_screen.auto_download_true");
        Text autoDownloadTextFalse = Text.translatable("simple_mod_sync.ui.set_sync_screen.auto_download_false");
        ButtonWidget auto_download = new ButtonWidget.Builder(autoDownload.get() ? autoDownloadTextTrue : autoDownloadTextFalse, (buttonWidget) -> {
            autoDownload.set(!autoDownload.get());
            buttonWidget.setMessage(autoDownload.get() ? autoDownloadTextTrue : autoDownloadTextFalse);
        }).position(this.width / 2 - 70, this.height / 2 + 50).size(140, 20).build();
        this.addDrawableChild(auto_download);

        // Cancel button widget
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.set_sync_screen.cancel_button"), (buttonWidget) -> {
            Config.instance.setDownloadUrl("-");
            MinecraftClient.getInstance().setScreen(parent);
            SimpleModSync.StartWorker();
        }).position(this.width / 2 - 105, this.height / 2 + 80).size(100, 20).build());

        // Set button widget
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.set_sync_screen.confirm_button"), (buttonWidget) -> {
            String url = remote_url.getText();
            if (!url.isEmpty()) {
                Config.instance.setDownloadUrl(url);
                MinecraftClient.getInstance().setScreen(parent);
                SimpleModSync.StartWorker();
            }
        }).position(this.width / 2 + 5, this.height / 2 + 80).size(100, 20).build());
    }
}
