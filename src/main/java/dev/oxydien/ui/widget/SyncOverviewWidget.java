package dev.oxydien.ui.widget;

import dev.oxydien.SimpleModSync;
import dev.oxydien.enums.SyncErrorType;
import dev.oxydien.enums.SyncState;
import dev.oxydien.ui.SyncFullViewScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;

public class SyncOverviewWidget extends PressableWidget {
    private final TextRenderer textRenderer;

    public SyncOverviewWidget(TextRenderer textRenderer) {
        super(0 ,0, 100, 28, Text.literal(""));
        this.textRenderer = textRenderer;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        SyncState state = SimpleModSync.worker.GetState();
        SyncErrorType errorType = SimpleModSync.worker.GetErrorType();
        int progress = SimpleModSync.worker.GetProgress();

        int foregroundColor = state == SyncState.ERROR ? 0xFFFF0000 : 0xFFFFFFFF;
        int updateColor = state == SyncState.ERROR ? 0xA3FF0000 : 0xA3FFFFFF;

        // Background
        context.fill(RenderLayer.getGui(), this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xa0000000);

        // Progress bar
        context.fill(RenderLayer.getGuiOverlay(), this.getX() + 4, this.getY() + 2,
                this.getX() + (int)(this.getWidth() * (float)(progress) / (float)100) - 2, this.getY() + 5, updateColor);

        // Text
        Text text;
        if (state != SyncState.ERROR) text = Text.translatable(String.format("simple_mod_sync.ui.%s", state.name().toLowerCase()), progress);
        else text = Text.translatable(String.format("simple_mod_sync.ui.error.%s", errorType.name().toLowerCase()));

        int i = 0;
        for (String line : text.getString().split("\n")) {
            context.drawText(this.textRenderer, line, this.getX() + 5, this.getY() + 6 + (this.textRenderer.fontHeight + 2) * i++, foregroundColor, false);
        }
    }

    @Override
    public void onPress() {
        MinecraftClient.getInstance().setScreen(new SyncFullViewScreen(Text.translatable("simple_mod_sync.ui.sync_full_view.title"), null));
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
