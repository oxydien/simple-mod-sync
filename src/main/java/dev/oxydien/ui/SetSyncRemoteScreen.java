package dev.oxydien.ui;

import dev.oxydien.SimpleModSync;
import dev.oxydien.config.Config;
import dev.oxydien.ui.widget.SimpleBackgroundWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

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
        Text titleText = Text.translatable("simple_mod_sync.ui.set_sync_remote_screen_title");
        this.addDrawableChild(
                new MultilineTextWidget(this.width / 2 - titleText.getString().length() - 30, this.height / 2 - 40, titleText , this.textRenderer)
                        .setTextColor(0xFF3DF6B4));

        // Subtitle widget
        Text subtitleText = Text.translatable("simple_mod_sync.ui.set_sync_remote_screen_subtitle");
        this.addDrawableChild(
                new MultilineTextWidget(this.width / 2 - subtitleText.getString().length() - 30, this.height / 2 - 20, subtitleText , this.textRenderer)
                        .setMaxWidth(this.width - 80).setMaxRows(2));

        // URL field widget
        TextFieldWidget remote_url = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 + 20, 200, 20, Text.literal(""));
        this.addDrawableChild(remote_url);

        // Cancel button widget
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.cancel_sync_button"), (buttonWidget) -> {
            Config.instance.setDownloadUrl("-");
            MinecraftClient.getInstance().setScreen(parent);
            SimpleModSync.StartWorker();
        }).position(this.width / 2 - 105, this.height / 2 + 60).size(100, 20).build());

        // Set button widget
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.set_sync_remote_button"), (buttonWidget) -> {
            String url = remote_url.getText();
            if (!url.isEmpty()) {
                Config.instance.setDownloadUrl(url);
                MinecraftClient.getInstance().setScreen(parent);
                SimpleModSync.StartWorker();
            }
        }).position(this.width / 2 + 5, this.height / 2 + 60).size(100, 20).build());
    }
}
