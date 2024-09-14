package dev.oxydien.ui;

import dev.oxydien.SimpleModSync;
import dev.oxydien.config.Config;
import dev.oxydien.data.ContentSyncProgress;
import dev.oxydien.data.ProgressCallback;
import dev.oxydien.data.SyncData;
import dev.oxydien.enums.CallbackReason;
import dev.oxydien.ui.widget.ContentSyncProgressWidget;
import dev.oxydien.ui.widget.SimpleBackgroundWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SyncFullViewScreen extends Screen implements ProgressCallback {
    private HashMap<Integer, ContentSyncProgressWidget> progress;
    private int page;
    private int pageSize;
    private final Screen parent;
    private SimpleBackgroundWidget progressBar;

    public SyncFullViewScreen(Text title, @Nullable Screen parent) {
        super(title);
        this.progress = new HashMap<>();
        this.page = 0;
        this.pageSize = 4;
        this.parent = parent;
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();

        SimpleModSync.worker.subscribe(this);
    }

    @Override
    public void init() {
        super.init();
        this.progress = new HashMap<>();
        this.pageSize = (this.height - 100) / 35;

        // Back button
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.sync_full_view.back_button"),
                (buttonWidget) -> MinecraftClient.getInstance().setScreen(this.parent)).position(3, 5).size(60, 20).build());

        this.progressBar = new SimpleBackgroundWidget(0, 0, this.width, 2, 0xFFFFFFFF);
        this.addDrawableChild(this.progressBar);

        // Title
        Text titleText = Text.translatable("simple_mod_sync.ui.sync_full_view.title");
        this.addDrawableChild(
                new MultilineTextWidget(this.width / 2 - titleText.getString().length() - 30, 10, titleText, this.textRenderer)
                        .setTextColor(0xFF3DF6B4));

        // Url field
        TextFieldWidget urlField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, 24,
                300, 20, Text.literal(""));
        urlField.setText(Config.instance.getDownloadUrl());
        this.addDrawableChild(urlField);

        // Save Url button
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.sync_full_view.save_url_button"), (buttonWidget) -> {
            String url = urlField.getText();
            if (!url.isEmpty()) {
                Config.instance.setDownloadUrl(url);
            }
        }).position(this.width / 2 - 150, 45).size(95, 20).build());

        // Sync button
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.sync_full_view.sync_button"),
                (buttonWidget) -> SimpleModSync.StartWorker()).position(this.width / 2 - 48, 45).size(95, 20).build());

        // Auto download toggle button widget
        AtomicBoolean autoDownload = new AtomicBoolean(Config.instance.getAutoDownload());
        Text autoDownloadTextTrue = Text.translatable("simple_mod_sync.ui.sync_full_view.auto_download_true");
        Text autoDownloadTextFalse = Text.translatable("simple_mod_sync.ui.sync_full_view.auto_download_false");
        ButtonWidget auto_download = new ButtonWidget.Builder(autoDownload.get() ? autoDownloadTextTrue : autoDownloadTextFalse, (buttonWidget) -> {
            autoDownload.set(!autoDownload.get());
            buttonWidget.setMessage(autoDownload.get() ? autoDownloadTextTrue : autoDownloadTextFalse);
        }).position(this.width / 2 + 55, 45).size(95, 20).build();
        this.addDrawableChild(auto_download);

        // Previous and next page buttons
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.sync_full_view.previous_page"), (buttonWidget) -> {
            if (this.page > 0) {
                this.page--;
            }
        }).size(20, 20).position(this.width / 2 - 175, this.height - 40).build());
        this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("simple_mod_sync.ui.sync_full_view.next_page"), (buttonWidget) -> {
            SyncData data = SimpleModSync.worker.GetSyncData();
            if (data != null && this.page < data.getContent().size() / this.pageSize) {
                this.page++;
            }
        }).size(20, 20).position(this.width / 2 + 155, this.height - 40).build());

        this.onProgress(CallbackReason.NONE);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int index = 0;
        for (ContentSyncProgressWidget widget : progress.values().stream()
                .sorted(Comparator.comparingInt(w -> w.getProgress() == 0 ? Integer.MAX_VALUE : w.getProgress())).toList()) {
            if (index >= page * pageSize && index < (page + 1) * pageSize) {
                widget.setPosition(this.width / 2 - widget.getWidth() / 2,
                        70 + (index - page * pageSize) * widget.getHeight() + (index - page * pageSize) * 3);
                widget.render(context, mouseX, mouseY, delta);
            }

            index++;
        }
    }

    @Override
    public void onProgress(CallbackReason reason) {
        //SimpleModSync.LOGGER.info("Sync progress: {}", reason);

        this.progressBar.setSize((int)(this.width * ((float)SimpleModSync.worker.GetProgress() / 100f)), 2);

        List<ContentSyncProgress> progresses = SimpleModSync.worker.GetModProgress();
        SyncData data = SimpleModSync.worker.GetSyncData();
        if (data == null) return;
        for (SyncData.Content content : data.getContent()) {
            ContentSyncProgress modProgress = progresses.stream().filter(p -> p.getIndex() == content.getIndex()).findFirst().orElse(null);
            if (!progress.containsKey(content.getIndex())) {
                progress.put(content.getIndex(), new ContentSyncProgressWidget(0, 0, 300, 30, this.textRenderer, content, modProgress));
            }

            ContentSyncProgressWidget widget = progress.get(content.getIndex());
            widget.setProgress(modProgress);
        }
    }
}
