package dev.oxydien.ui;

import dev.oxydien.SimpleModSync;
import dev.oxydien.config.Config;
import dev.oxydien.ui.widget.SimpleBackgroundWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
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
        TextFieldWidget remote_url = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 + 20, 200, 20, Text.literal(""));

        this.addDrawableChild(new SimpleBackgroundWidget(20, 20, this.width - 40, this.height - 40, 0));

        this.addDrawableChild(new MultilineTextWidget(this.width / 2 - 30, this.height / 2 - 20, Text.translatable("simple_mod_sync.ui.set_sync_remote"), this.textRenderer).setMaxWidth(this.width - 80).setMaxRows(2));
        this.addDrawableChild(remote_url);
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.set_sync_remote_button"), (buttonWidget) -> {
            Config.instance.setDownloadUrl(remote_url.getText());
            MinecraftClient.getInstance().setScreen(parent);
            SimpleModSync.StartWorker();
        }).position(this.width / 2 + 20, this.height / 2 + 60).size(80, 20).build());

        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.cancel_sync_button"), (buttonWidget) -> {
            Config.instance.setDownloadUrl("-");
            MinecraftClient.getInstance().setScreen(parent);
            SimpleModSync.StartWorker();
        }).position(this.width / 2 - 100, this.height / 2 + 60).size(80, 20).build());
    }
}
